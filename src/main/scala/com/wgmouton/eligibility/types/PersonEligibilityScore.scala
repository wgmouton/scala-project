package com.wgmouton.eligibility.types

import spray.json.*


final case class PersonEligibilityScore(
                                         provider: Provider,
                                         name: String,
                                         apr: BigDecimal,
                                         cardScore: BigDecimal
                                       )