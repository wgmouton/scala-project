package com.wgmouton.eligibility.types

import cats.data.EitherT

import scala.concurrent.Future

sealed trait ProviderInterface(val provider: Provider)

final case class CSCardsScore(cardName: String, apr: BigDecimal, eligibility: BigDecimal)
  extends ProviderInterface(Provider.CSCards)

sealed trait CSCardsGatewayBehavior {

  def getScore(name: String, creditScore: Int): EitherT[Future, String, List[CSCardsScore]]
}


final case class ScoredCardsScore(card: String, apr: BigDecimal, approvalRating: BigDecimal)
  extends ProviderInterface(Provider.ScoredCards)

sealed trait ScoredCardsGatewayBehavior {

  def getScore(name: String, creditScore: Int, salary: Int): EitherT[Future, String, List[ScoredCardsScore]]
}
