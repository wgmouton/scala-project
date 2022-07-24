package com.wgmouton.eligibility.entities

import cats.data.EitherT
import com.wgmouton.eligibility.{CSCardsGatewayImplementationStub, CSCardsScore, ScoredCardsGatewayImplementationStub, ScoredCardsScore}

import scala.concurrent.Future

object CreditCard {

  def csCardsEligibility(fullName: String, creditScore: Int): EitherT[Future, String, List[CSCardsScore]] = {
    CSCardsGatewayImplementationStub.getScore(fullName, creditScore)
    //    EitherT.rightT[Future, String](List[CSCardsScore](CSCardsScore(cardName = "hi", apr = 10, eligibility = 10)))
  }

  def scoredCardsEligibility(fullName: String, creditScore: Int, salary: Int): EitherT[Future, String, List[ScoredCardsScore]] = {
    ScoredCardsGatewayImplementationStub.getScore(fullName, creditScore, salary)
  }

}
