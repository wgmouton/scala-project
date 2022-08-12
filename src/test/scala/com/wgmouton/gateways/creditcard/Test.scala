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


//object JsonFormats extends DefaultJsonProtocol {
//
//  implicit val getFromSCScoreJsonFormat: RootJsonFormat[List[CSCardsScore]] = new RootJsonFormat[List[CSCardsScore]] {
//    def write(getFromSCScore: GetFromSCScore): JsValue = JsString("hi")
//
//    def read(value: JsValue): List[CSCardsScore] = List.empty[CSCardsScore]
//  }
//
//}
object Test extends Gateway[CreditCardEntityGatewayCommand] with CreditCardEntityGateway {

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
      //      case res =>
      //        Unmarshal(res.entity).to[String].map(rawJson =>
      //          Left(rawJson)
      //
      //        )
    }
  }

  private def handleCommand(context: ActorContext[CreditCardEntityGatewayCommand]): CreditCardEntityGatewayCommand => Behavior[CreditCardEntityGatewayCommand] = {
    case GetFromSCScore(name, creditScore, replyTo) =>
      val json = JsObject(
        "name" -> name.toJson,
        "creditScore" -> creditScore.toJson
      )
      replyTo.tell(Right(
        List(
          CSCardsScore("SuperSaver Card", 19.4, 0.137),
          CSCardsScore("SuperSpender Card", 19.2, 0.135)
        )
      ))
      Behaviors.same

    case GetFromScoredCards(name, score, salary, replyTo) =>
      replyTo.tell(Right(
        List(
          ScoredCardsScore("Scored Card Builder", 19.4, 0.8)
        )
      ))
      Behaviors.same
  }

  override def apply(): Behavior[CreditCardEntityGatewayCommand] = Behaviors.setup { context =>
    Behaviors.receiveMessage[CreditCardEntityGatewayCommand](x => handleCommand(context)(x))
  }
}