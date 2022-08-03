package com.wgmouton.clients.httpserver

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, SupervisorStrategy}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpHeader, HttpRequest, HttpResponse, MediaTypes, ResponseEntity, StatusCode, StatusCodes}
import com.wgmouton.eligibility.boudaries.QueryPersonEligibilityUsingPersonDetails
import com.wgmouton.eligibility.boudaries.InteractorCommand as EligibilityCommand
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.AskPattern.*
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes.*
import akka.http.scaladsl.model.headers.`Content-Type`
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.{Route, StandardRoute}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import com.wgmouton.eligibility.types.{PersonEligibilityScore, Provider}
import netscape.javascript.JSException
import spray.json.{RootJsonFormat, *}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.*
import scala.util.{Failure, Success}

final case class Person(name: String, creditScore: Int, salary: Int)

object JsonFormats extends DefaultJsonProtocol {

  import DefaultJsonProtocol.*

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

class RouteHandlers(eligibilityActor: ActorRef[EligibilityCommand])(implicit val system: ActorSystem[_]) {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
  import JsonFormats.*

  implicit val timeout: Timeout = 20.seconds

  val routes: Route = {
    logRequestResult("akka-http-microservice") {
      concat(
        path("creditcards") {
          (post & entity(as[Person])) (lookupByPerson)
        },
        pathPrefix("providers") {
          (get & path(Segment)) (lookupByProvider)
        }
      )
    }
  }

  private def routeHandler(f: () => Future[(StatusCode, ResponseEntity)]): Route = {
    onComplete(f()) {
      case Failure(res) => complete(InternalServerError, res)
      case Success(res) => complete {
        HttpResponse(
          status = res._1,
          entity = res._2
        )
      }
    }
  }


  def lookupByPerson: Person => Route = { person =>
    routeHandler { () =>
      eligibilityActor
        .ask[Either[String, List[PersonEligibilityScore]]](QueryPersonEligibilityUsingPersonDetails(person.name, person.creditScore, person.salary, _))
        .map {
          case Left(error) => InternalServerError -> HttpEntity(ContentTypes.`text/plain(UTF-8)`, error)
          case Right(res) => OK -> HttpEntity(ContentTypes.`application/json`, res.toJson.compactPrint)
        }
    }
  }

  def lookupByProvider: String => Route = { provider =>
    complete {
      BadRequest -> "No provider set"
    }
  }
}

object HttpServer {

  def start(eligibilityActor: ActorRef[EligibilityCommand])(implicit system: ActorSystem[_]): Unit = {
    // Akka HTTP still needs a classic ActorSystem to start

    import system.executionContext

    val routeHandler = new RouteHandlers(eligibilityActor)

    val futureBinding = Http().newServerAt("localhost", 8080).bind(routeHandler.routes)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }
}