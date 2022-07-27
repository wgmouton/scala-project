package com.wgmouton.gateways.creditcard

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import cats.data.EitherT
import com.wgmouton.eligibility.types.*
import com.wgmouton.eligibility.boudaries.{Command, CreditCardEntityGateway, QueryPersonEligibilityUsingPersonDetails}
import com.wgmouton.eligibility.interactors.QueryPersonEligibility
import com.wgmouton.util.Gateway
import cats.implicits.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

sealed trait Command

final case class Test(
                                                           name: String,
                                                           creditScore: Int,
                                                           replyTo: ActorRef[Either[String, List[CSCardsScore]]]
                                                         ) extends Command


object Real extends Gateway[Any]
  with CreditCardEntityGateway {

  private def handleCommand(): Any => Behavior[Any] = {
    case _ =>
      Behaviors.same
  }


  override def apply(): Behavior[Any] = Behaviors.setup { context =>
    Behaviors.receiveMessage[Any](x => handleCommand() (x))
//    Behaviors.same[Command]
  }

//  override def getFromCSScore(name: String, creditScore: Int): EitherT[Future, String, List[CSCardsScore]] = {
//    EitherT.leftT[Future, List[CSCardsScore]]("error")
//  }
//
//  override def getFromScoredScore(name: String, creditScore: Int): EitherT[Future, String, List[ScoredCardsScore]] = {
//    EitherT.leftT[Future, List[ScoredCardsScore]]("error")
//
//  }
}