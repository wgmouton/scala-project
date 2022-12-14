package com.wgmouton.gateways.creditcard

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Post
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import spray.json.DefaultJsonProtocol.*
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import akka.http.scaladsl.model.*
import akka.http.scaladsl.unmarshalling.*
import akka.stream.{ActorMaterializer, Materializer}
import cats.data.EitherT
import cats.implicits.*
import com.wgmouton.eligibility.boudaries.*
import com.wgmouton.eligibility.interactors.QueryPersonEligibility
import com.wgmouton.eligibility.types.*
import com.wgmouton.util.Gateway
import com.wgmouton.util.errors.ServiceError
import spray.json.DefaultJsonProtocol.*
import spray.json.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.*


object Real extends Gateway[CreditCardEntityGatewayCommand] with CreditCardEntityGateway {

  private val ServiceUrlNotDefined: String => ServiceError = url => ServiceError(23, String.format("service url at environment variable (%s) not set", url))

  private def fetchData[R](context: ActorContext[CreditCardEntityGatewayCommand], url: String, data: JsValue, dataF: String => R): Future[Either[ServiceError, R]] = {
    implicit val materializer: ActorSystem[Nothing] = context.system

    val req = HttpRequest(
      method = HttpMethods.POST,
      uri = url,
      entity = HttpEntity(
        ContentTypes.`application/json`,
        data.compactPrint
      )
    )
    val res = Http()(context.system).singleRequest(req)
    res.flatMap {
      case response@HttpResponse(StatusCodes.OK, _, _, _) =>
        Unmarshal(response.entity).to[String].map(rawJson =>
          Right(dataF(rawJson))
        )
      case response@HttpResponse(StatusCodes.NotFound, _, _, _) =>
        Unmarshal(response.entity).to[String].map(rawJson =>
          Left(ServiceError(404, "Data not found", rawJson))
        )
      case res =>
        Unmarshal(res.entity).to[String].map(rawJson =>
          Left(ServiceError(21, "Failed to fetch data from server", rawJson))
        )
    }
  }

  private def handleCommand(context: ActorContext[CreditCardEntityGatewayCommand]): CreditCardEntityGatewayCommand => Behavior[CreditCardEntityGatewayCommand] = {
    case GetFromSCScore(name, creditScore, replyTo) =>
      val json = JsObject(
        "name" -> name.toJson,
        "creditScore" -> creditScore.toJson
      )

      EitherT.fromOption[Future](sys.env.get("CSCARDS_ENDPOINT"), ServiceUrlNotDefined("CSCARDS_ENDPOINT")).flatMapF { url =>
        fetchData(context, url, json, { rawJson =>
          println("hiiiii")
          println(url)
          rawJson.parseJson.convertTo[List[JsValue]].flatMap(_.asJsObject.getFields("cardName", "apr", "eligibility") match {
            case Seq(cardName, apr, eligibility) => List(
              CSCardsScore(
                cardName.convertTo[String],
                apr.convertTo[BigDecimal],
                eligibility.convertTo[BigDecimal]
              )
            )
            case _ => List.empty[CSCardsScore]
          })
        })
      }.value.foreach(replyTo.tell)
      Behaviors.same

    case GetFromScoredCards(name, score, salary, replyTo) =>
      val json = JsObject(
        "name" -> name.toJson,
        "score" -> score.toJson,
        "salary" -> salary.toJson,
      )
      EitherT.fromOption[Future](sys.env.get("SCOREDCARDS_ENDPOINT"), ServiceUrlNotDefined("SCOREDCARDS_ENDPOINT")).flatMapF { url =>
        println(url)
        fetchData(context, url, json, { rawJson =>
          rawJson.parseJson.convertTo[List[JsValue]].flatMap(_.asJsObject.getFields("card", "apr", "approvalRating") match {
            case Seq(card, apr, approvalRating) => List(
              ScoredCardsScore(
                card.convertTo[String],
                apr.convertTo[BigDecimal],
                approvalRating.convertTo[BigDecimal]
              )
            )
            case _ => List.empty[ScoredCardsScore]
          })
        })
      }.value.foreach(replyTo.tell)
      Behaviors.same
  }

  override def apply(): Behavior[CreditCardEntityGatewayCommand] = Behaviors.setup { context =>
    Behaviors.receiveMessage[CreditCardEntityGatewayCommand](x => handleCommand(context)(x))
  }
}