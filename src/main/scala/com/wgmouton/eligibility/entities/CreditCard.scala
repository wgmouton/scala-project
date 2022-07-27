package com.wgmouton.eligibility.entities

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.AskPattern.*
import com.wgmouton.eligibility.types.{CSCardsScore, ScoredCardsScore}
import com.wgmouton.eligibility.boudaries.*
import cats.implicits.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import cats.data.EitherT
//import com.wgmouton.eligibility.{CSCardsGatewayImplementationStub, CSCardsScore, ScoredCardsGatewayImplementationStub, ScoredCardsScore}

import scala.concurrent.Future

class CreditCard(creditCardEntityGatewayActor: ActorRef[Any]) {

  def csCardsEligibility(fullName: String, creditScore: Int): EitherT[Future, String, List[CSCardsScore]] = {
    EitherT(creditCardEntityGatewayActor.getFromCSScore(fullName, creditScore))

//    creditCardEntityGatewayActor.ask()

//    creditCardEntityGatewayActor.ask()

//    CSCardsGatewayImplementationStub.getScore(fullName, creditScore)

  }

  def scoredCardsEligibility(fullName: String, creditScore: Int, salary: Int): EitherT[Future, String, List[ScoredCardsScore]] = {
    EitherT(creditCardEntityGatewayActor.getFromScoredScore(fullName, creditScore))
  }

}
