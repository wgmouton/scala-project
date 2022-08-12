package com.wgmouton.clients.httpserver

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, SupervisorStrategy}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpHeader, HttpRequest, HttpResponse, MediaTypes, ResponseEntity, StatusCode, StatusCodes}
import com.wgmouton.eligibility.commands.{InteractorCommands, QueryPersonEligibilityUsingPersonDetails, InteractorCommand as EligibilityCommand}
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
import com.wgmouton.util.errors.ServiceError
import netscape.javascript.JSException
import spray.json.{RootJsonFormat, *}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.*
import scala.util.{Failure, Success}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import JsonFormats.*

final case class Person(name: String, creditScore: Int, salary: Int)

class HttpRouter(eligibilityActor: ActorRef[EligibilityCommand])(implicit val system: ActorSystem[_]) {

  implicit val timeout: Timeout = 10.seconds

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
      eligibilityActor.queryPersonEligibilityUsingPersonDetails(person.name, person.creditScore, person.salary).map {
        case Left(error) => InternalServerError -> HttpEntity(ContentTypes.`application/json`, error.json.compactPrint)
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