package com.wgmouton.eligibility.interactors

import cats.{Applicative, Functor, Monad}
import cats.data.EitherT
import com.wgmouton.eligibility.entities.CreditCard as CreditCardEntity
import com.wgmouton.eligibility.types.*
import cats.implicits.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class QueryPersonEligibility(implicit val creditCardEntity: CreditCardEntity) {

  private def ignoreSafeErrors[F[_], R](either: EitherT[F, String, R], default: => R)(implicit F: Monad[F]): EitherT[F, String, R] = {
    either.leftFlatMap {
      case "Not Found" => EitherT.rightT[F, String](default)
      case _ => either
    }
  }

  private def sortingScore(eligibility: BigDecimal, apr: BigDecimal): BigDecimal = eligibility * (1 / apr)

  def usingPersonDetails(name: String, creditScore: Int, salary: Int): EitherT[Future, String, List[PersonEligibilityScore]] = {
    val fetchCSCards = ignoreSafeErrors(creditCardEntity.csCardsEligibility(name, creditScore), List.empty)
    val fetchScoredCards = ignoreSafeErrors(creditCardEntity.scoredCardsEligibility(name, creditScore, salary), List.empty)

    for {
      csCards <- fetchCSCards
      scoredCards <- fetchScoredCards
    } yield {
      val y = csCards.map(csCard => PersonEligibilityScore(
        provider = csCard.provider,
        name = csCard.cardName,
        apr = csCard.apr,
        cardScore = sortingScore(csCard.eligibility, csCard.apr)
      ))

      val x = scoredCards.map(scoredCard => PersonEligibilityScore(
        provider = scoredCard.provider,
        name = scoredCard.card,
        apr = scoredCard.apr,
        cardScore = sortingScore(scoredCard.approvalRating, scoredCard.apr)
      ))

      List.concat(x, y).sortBy(_.cardScore)
    }
  }
}
