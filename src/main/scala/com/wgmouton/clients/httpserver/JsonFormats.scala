package com.wgmouton.clients.httpserver

import com.wgmouton.eligibility.types.{PersonEligibilityScore, Provider}
import spray.json.*

object JsonFormats extends DefaultJsonProtocol {

  implicit val personJsonFormat: RootJsonFormat[Person] = jsonFormat3(Person.apply)
  implicit val cardScoreJsonFormat: RootJsonFormat[PersonEligibilityScore] = new RootJsonFormat[PersonEligibilityScore] {
    def write(pes: PersonEligibilityScore): JsValue = {
      JsObject(
        "provider" -> pes.provider.toString.toJson,
        "name" -> pes.name.toJson,
        "apr" -> pes.apr.toJson,
        "cardScore" -> pes.cardScore.toJson
      )
    }

    def read(value: JsValue): PersonEligibilityScore = deserializationError("Not Implemented")
  }
  
  implicit val providerJsonFormat: RootJsonFormat[Provider] = new RootJsonFormat[Provider] {
    def write(provider: Provider): JsValue = JsString(provider.toString)

    def read(value: JsValue): Provider = Provider.valueOf(value.convertTo[String])
  }

}