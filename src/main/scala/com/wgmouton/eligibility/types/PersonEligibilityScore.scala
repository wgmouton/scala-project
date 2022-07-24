package com.wgmouton.eligibility.types

import com.wgmouton.eligibility.Provider

final case class PersonEligibilityScore(
                                         provider: Provider,
                                         name: String,
                                         apr: BigDecimal,
                                         cardScore: BigDecimal
                                       )
