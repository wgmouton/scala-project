package com.wgmouton.util.errors

implicit class ThrowableToError(throwable: Throwable) {
//  def toError()
}
trait ErrorContext(protected val contextCode: Int) {
  sealed trait ErrorCode(protected val errorCode: Int, description: String) {
    final val code: String = String.format("%d_%d", contextCode, errorCode)
  }
}

object AppErrors extends ErrorContext(0) {
  case object STARTUP_FAILED extends ErrorCode(1, "Failed to start the server")
  case object APP_SUPERVISOR_RESTARTED extends ErrorCode(2, "App supervisor restarted")
}


//object


