package com.wgmouton.gateways.creditcard

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Post
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
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
import spray.json.DefaultJsonProtocol.*
import spray.json.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.*


//object JsonFormats extends DefaultJsonProtocol {
//
//  implicit val getFromSCScoreJsonFormat: RootJsonFormat[List[CSCardsScore]] = new RootJsonFormat[List[CSCardsScore]] {
//    def write(getFromSCScore: GetFromSCScore): JsValue = JsString("hi")
//
//    def read(value: JsValue): List[CSCardsScore] = List.empty[CSCardsScore]
//  }
//
//}
object Real extends Gateway[CreditCardEntityGatewayCommand] with CreditCardEntityGateway {

  private def fetchData[R](context: ActorContext[CreditCardEntityGatewayCommand], url: String, data: JsValue, dataF: String => R): Future[Either[String, R]] = {
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
      case res =>
        Unmarshal(res.entity).to[String].map(rawJson =>
          println(rawJson)
          Left(rawJson)

        )
    }
  }

  private def handleCommand(context: ActorContext[CreditCardEntityGatewayCommand]): CreditCardEntityGatewayCommand => Behavior[CreditCardEntityGatewayCommand] = {
    case GetFromSCScore(name, creditScore, replyTo) =>
      val json = JsObject(
        "name" -> name.toJson,
        "creditScore" -> creditScore.toJson
      )
      fetchData(context, "https://app.clearscore.com/api/global/backend-tech-test/v1/cards", json, { rawJson =>
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
      }).foreach(replyTo.tell)
      Behaviors.same
    case GetFromScoredCards(name, score, salary, replyTo) =>
      val json = JsObject(
        "name" -> name.toJson,
        "score" -> score.toJson,
        "salary" -> salary.toJson,
      )
      println("hiiii")
      fetchData(context, "https://app.clearscore.com/api/global/backend-tech-test/v2/creditcards", json, { rawJson =>
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
      }).foreach(replyTo.tell)
      Behaviors.same
  }


  override def apply(): Behavior[CreditCardEntityGatewayCommand] = Behaviors.setup { context =>
    Behaviors.receiveMessage[CreditCardEntityGatewayCommand](x => handleCommand(context)(x))
  }
}