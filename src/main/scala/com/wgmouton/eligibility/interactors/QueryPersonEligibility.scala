package com.wgmouton.eligibility.interactors

import cats.data.EitherT
import com.wgmouton.eligibility.entities.CreditCard as CreditCardEntity
import com.wgmouton.eligibility.types.PersonEligibilityScore

import scala.concurrent.Future

object QueryPersonEligibility {
  private def sortingScore(eligibility: BigDecimal, apr: BigDecimal): BigDecimal = eligibility * (1 / apr)

  def usingPersonDetails(name: String, creditScore: Int, salary: Int): EitherT[Future, String, List[PersonEligibilityScore]] = {
    for {
      csCards <- CreditCardEntity.csCardsEligibility(name, creditScore)
      scoredCards <- CreditCardEntity.scoredCardsEligibility(name, creditScore, salary)
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
