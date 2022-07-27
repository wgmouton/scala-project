package com.wgmouton.eligibility.types

enum Provider(s: String):
  case CSCards extends Provider("CSCards")
  case ScoredCards extends Provider("ScoredCards")