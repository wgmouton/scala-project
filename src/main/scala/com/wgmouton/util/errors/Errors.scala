package com.wgmouton.util.errors

import spray.json.*

implicit class ThrowableToError(throwable: Throwable) {
  //  def toError()
}

case class ServiceError(errorCode: Int, ctx: String *) {
  val context: List[String] = ctx.toList
  val code: String = errorCode.toString
//  val description: String = ""
  val json: JsValue = JsObject {
    val base: Map[String, JsValue] = Map(
      "code" -> JsString(code),
//      "description" -> JsString(description),
    )
    if (context.nonEmpty) {
      base.updated("context", JsArray(context.map(JsString(_)): _*))
    } else {
      base
    }
  }
}

trait ErrorContext {
//  trait ErrorCode(protected val errorCode: Int, override val description: String) extends ServiceError {
//    override val context: List[String] = List.empty[String]
//    override val code: String = String.format("%04d-%05d-%s", contextCode, errorCode, description)
//  }
}

//object AppErrors extends ErrorContext(0) {
//  case object STARTUP_FAILED extends ErrorCode(1, "Failed to start the server")
//
//  case object APP_SUPERVISOR_RESTARTED extends ErrorCode(2, "App supervisor restarted")
//}