package com.wgmouton.eligibility

import cats.data.EitherT

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import cats.implicits._


object Interactor {

  def lookupPersonEligibility(name: String, creditScore: Int, salary: Int): EitherT[Future, String, List[PersonEligibilityScore]] = {
    val sortingScore: (BigDecimal, BigDecimal) => BigDecimal = { (eligibility, apr) =>
      eligibility * ((1 / apr))
    }

    for {
      csCards <- Entity.csCardsEligibility(name, creditScore)
      scoredCards <- Entity.scoredCardsEligibility(name, creditScore, salary)
    } yield {
      val x = scoredCards.map(scoredCard => PersonEligibilityScore(
        provider = scoredCard.provider,
        name = scoredCard.card,
        apr = scoredCard.apr,
        cardScore = sortingScore(scoredCard.approvalRating, scoredCard.apr)
      ))

      val y = csCards.map(csCard => PersonEligibilityScore(
        provider = csCard.provider,
        name = csCard.cardName,
        apr = csCard.apr,
        cardScore = sortingScore(csCard.eligibility, csCard.apr)
      ))

      List.concat(x, y).sortBy(_.cardScore)
    }
  }

  def lookupProvider(provider: String): Unit = {

  }

}
