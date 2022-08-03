package com.wgmouton.gateways.creditcard

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.http.scaladsl.Http
import cats.data.EitherT
import com.wgmouton.eligibility.types.*
import com.wgmouton.eligibility.boudaries.*
import com.wgmouton.eligibility.interactors.QueryPersonEligibility
import com.wgmouton.util.Gateway
import cats.implicits.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import akka.http.scaladsl.client.RequestBuilding.Post
import akka.http.scaladsl.model.{HttpResponse, ResponseEntity, StatusCodes}


object Real extends Gateway[CreditCardEntityGatewayCommand] with CreditCardEntityGateway {

  private def fetchData[R](context: ActorContext[CreditCardEntityGatewayCommand], url: String, data: String, dataF: ResponseEntity => R): Future[Either[String, R]] = {

    println("hiiiii")
    println(url)

    val req = Post(url, data)
    val res = Http()(context.system).singleRequest(req)
    res.map {
      case response@HttpResponse(StatusCodes.OK, _, _, _) =>
        println(response)

        Right(dataF(response.entity))

      case _ =>
        println("somet")
        Left("something wrong")
    }
  }

  private def handleCommand(context: ActorContext[CreditCardEntityGatewayCommand]): CreditCardEntityGatewayCommand => Behavior[CreditCardEntityGatewayCommand] = {
    case GetFromSCScore(_, _, replyTo) =>
      fetchData(context, "https://app.clearscore.com/api/global/backend-tech-test/v1/cards", "", { res =>
        println(res)
        List.empty[CSCardsScore]
      }).foreach(d => replyTo.tell(d))
      Behaviors.same
    case GetFromScoredCards(_, _, _, replyTo) =>
      fetchData(context, "https://app.clearscore.com/api/global/backend-tech-test/v2/creditcards", "", { res =>
        List.empty[ScoredCardsScore]
      }).foreach(replyTo.tell)
      Behaviors.same
  }


  override def apply(): Behavior[CreditCardEntityGatewayCommand] = Behaviors.setup { context =>
    Behaviors.receiveMessage[CreditCardEntityGatewayCommand](x => handleCommand(context)(x))
    //    Behaviors.same[Command]
  }
}