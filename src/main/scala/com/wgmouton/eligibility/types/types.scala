package com.wgmouton.eligibility.types

import cats.data.EitherT
import scala.concurrent.Future

import spray.json.*

enum Provider(s: String):
  case CSCards extends Provider("CSCards")
  case ScoredCards extends Provider("ScoredCards")

final case class PersonEligibilityScore(
                                         provider: Provider,
                                         name: String,
                                         apr: BigDecimal,
                                         cardScore: BigDecimal
                                       )


sealed trait ProviderInterface(val provider: Provider)

final case class CSCardsScore(cardName: String, apr: BigDecimal, eligibility: BigDecimal)
  extends ProviderInterface(Provider.CSCards)

final case class ScoredCardsScore(card: String, apr: BigDecimal, approvalRating: BigDecimal)
  extends ProviderInterface(Provider.ScoredCards)
