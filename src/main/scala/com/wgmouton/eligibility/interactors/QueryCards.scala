package com.wgmouton.eligibility.interactors

import cats.data.EitherT
import com.wgmouton.eligibility.entities.CreditCard as CreditCardEntity
import com.wgmouton.eligibility.types.*

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import cats.implicits.*

class QueryCards(implicit val creditCardEntity: CreditCardEntity) {

  def byPlatform(platform: String): EitherT[Future, String, List[PersonEligibilityScore]] = {
    EitherT.leftT("error")
  }

}
