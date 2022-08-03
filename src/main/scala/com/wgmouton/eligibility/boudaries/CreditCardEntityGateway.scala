package com.wgmouton.eligibility.boudaries

import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Scheduler}
import cats.data.EitherT
import com.wgmouton.util.Gateway
import com.wgmouton.eligibility.types.*
import akka.actor.typed.scaladsl.AskPattern.*
import akka.util.Timeout

import scala.concurrent.duration.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

sealed trait CreditCardEntityGatewayCommand

final case class GetFromSCScore(name: String, creditScore: Int,  replyTo: ActorRef[Either[String, List[CSCardsScore]]]) extends CreditCardEntityGatewayCommand
final case class GetFromScoredCards(name: String, creditScore: Int, salary: Int,  replyTo: ActorRef[Either[String, List[ScoredCardsScore]]]) extends CreditCardEntityGatewayCommand


trait CreditCardEntityGateway {

  def apply(): Behavior[CreditCardEntityGatewayCommand]


}

implicit class CreditCardEntityGatewayCommands(actorRef: ActorRef[CreditCardEntityGatewayCommand]) {
  implicit val timeout: Timeout = 1.minute


  def getFromCSScore(name: String, creditScore: Int)(implicit system: ActorSystem[_]): Future[Either[String, List[CSCardsScore]]] = {
    actorRef.ask(GetFromSCScore(name, creditScore, _))
  }

  def getFromScoredCards(name: String, creditScore: Int, salary: Int)(implicit system: ActorSystem[_]): Future[Either[String, List[ScoredCardsScore]]] = {
    actorRef.ask(GetFromScoredCards(name, creditScore, salary, _))
  }
}
