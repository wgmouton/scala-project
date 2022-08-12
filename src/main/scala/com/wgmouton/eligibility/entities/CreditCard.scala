package com.wgmouton.eligibility.entities

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.AskPattern.*
import com.wgmouton.eligibility.types.{CSCardsScore, ScoredCardsScore}
import com.wgmouton.eligibility.boudaries.*
import cats.implicits.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import cats.data.EitherT
import com.wgmouton.util.errors.ServiceError
//import com.wgmouton.eligibility.{CSCardsGatewayImplementationStub, CSCardsScore, ScoredCardsGatewayImplementationStub, ScoredCardsScore}

import scala.concurrent.Future

class CreditCard(creditCardEntityGatewayActor: ActorRef[CreditCardEntityGatewayCommand])(implicit val system: ActorSystem[_]) {

  /**
   * Query the list of csCardsEligibility for the csCards platform
   * @param fullName person's full name
   * @param creditScore person's credit score
   * @return
   */
  def csCardsEligibility(fullName: String, creditScore: Int): EitherT[Future, ServiceError, List[CSCardsScore]] = {
    EitherT(creditCardEntityGatewayActor.getFromCSScore(fullName, creditScore))
  }

  /**
   * Query the list of scoredCardsEligibility for the scoredCards platform
   * @param fullName person's full name
   * @param creditScore person's credit score
   * @param salary person's salary
   * @return
   */
  def scoredCardsEligibility(fullName: String, creditScore: Int, salary: Int): EitherT[Future, ServiceError, List[ScoredCardsScore]] = {
    EitherT(creditCardEntityGatewayActor.getFromScoredCards(fullName, creditScore, salary))
  }

}
