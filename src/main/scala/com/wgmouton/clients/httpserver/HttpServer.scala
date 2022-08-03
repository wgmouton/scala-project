package com.wgmouton.clients.httpserver

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, SupervisorStrategy}
import akka.http.scaladsl.model.StatusCode
import com.wgmouton.eligibility.boudaries.{QueryPersonEligibilityUsingPersonDetails}
import com.wgmouton.eligibility.boudaries.InteractorCommand as EligibilityCommand
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.AskPattern.*
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes.*
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.{Route, StandardRoute}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import spray.json.RootJsonFormat

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.*
import scala.util.{Failure, Success}

final case class Person(name: String, creditScore: Int, salary: Int)

import spray.json.DefaultJsonProtocol

object JsonFormats {
  // import the default encoders for primitive types (Int, String, Lists etc)

  import DefaultJsonProtocol._

  implicit val personJsonFormat: RootJsonFormat[Person] = jsonFormat3(Person.apply)
}

class RouteHandlers(eligibilityActor: ActorRef[EligibilityCommand])(implicit val system: ActorSystem[_]) {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
  import JsonFormats.*

  implicit val timeout: Timeout = 5.seconds

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

  private def routeHandler[R](f: () => Future[(StatusCode, R)]): Route = {
    onComplete(f()) {
      case Failure(res) => complete((OK, res))
//      case Success(res: String) => complete((OK, res))
      case Success(res) => complete((OK, "no string"))
    }
  }


  def lookupByPerson: Person => Route = { person =>
    routeHandler { () =>
      eligibilityActor
        .ask(QueryPersonEligibilityUsingPersonDetails(person.name, person.creditScore, person.salary, _))
        .map {
          case Left(error) =>
            println(error)
            InternalServerError -> error
          case Right(res) =>
            println(res)
            OK -> res
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