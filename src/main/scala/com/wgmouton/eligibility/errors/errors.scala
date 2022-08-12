package com.wgmouton.eligibility.errors

import com.wgmouton.util.errors.{ErrorContext, ServiceError}


object EligibilityErrors extends ErrorContext {
  def REQUEST_VALIDATION_FAILED(contexts: String*) = ServiceError(11, contexts: _*)

  //  case object APP_SUPERVISOR_RESTARTED extends ErrorCode(2, "App supervisor restarted")
}