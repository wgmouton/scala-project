package com.wgmouton.eligibility.boudaries

import akka.actor.typed.{ActorRef, Behavior}
import cats.data.EitherT
import com.wgmouton.util.Gateway
import com.wgmouton.eligibility.types.*
import akka.actor.typed.scaladsl.AskPattern.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait CreditCardEntityGateway extends Gateway[Any] {
}

implicit class CreditCardEntityGatewayCommands(actorRef: ActorRef[Any]) {
  def getFromCSScore(name: String, creditScore: Int): Future[Either[String, List[CSCardsScore]]] = {
    actorRef.tell(name)
    Future(Left("error"))
  }

  def getFromScoredScore(name: String, creditScore: Int): Future[Either[String, List[ScoredCardsScore]]] = {
    actorRef.tell(name)
    Future(Left("error"))
  }
}
