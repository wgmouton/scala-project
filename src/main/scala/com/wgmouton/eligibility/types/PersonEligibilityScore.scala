package com.wgmouton.eligibility.types


final case class PersonEligibilityScore(
                                         provider: Provider,
                                         name: String,
                                         apr: BigDecimal,
                                         cardScore: BigDecimal
                                       )
