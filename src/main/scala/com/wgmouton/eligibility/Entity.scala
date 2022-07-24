package com.wgmouton.eligibility

import cats.data.EitherT

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import cats.implicits._

object Entity {

  def csCardsEligibility(fullName: String, creditScore: Int): EitherT[Future, String, List[CSCardsScore]] = {
    CSCardsGatewayImplementationStub.getScore(fullName, creditScore)
//    EitherT.rightT[Future, String](List[CSCardsScore](CSCardsScore(cardName = "hi", apr = 10, eligibility = 10)))
  }

  def scoredCardsEligibility(fullName: String, creditScore: Int, salary: Int): EitherT[Future, String, List[ScoredCardsScore]] = {
    ScoredCardsGatewayImplementationStub.getScore(fullName, creditScore, salary)
  }

}
