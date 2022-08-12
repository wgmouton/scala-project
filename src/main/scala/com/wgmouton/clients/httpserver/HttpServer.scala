package com.wgmouton.clients.httpserver

import akka.actor.typed.scaladsl.AskPattern.*
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, SupervisorStrategy}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes.*
import akka.http.scaladsl.model.headers.`Content-Type`
import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.{Route, StandardRoute}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import com.wgmouton.clients.httpserver.JsonFormats.*
import com.wgmouton.eligibility.commands.{InteractorCommands, QueryPersonEligibilityUsingPersonDetails, InteractorCommand as EligibilityCommand}
import com.wgmouton.eligibility.types.{PersonEligibilityScore, Provider}
import com.wgmouton.util.errors.ServiceError
import netscape.javascript.JSException
import spray.json.{RootJsonFormat, *}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.*
import scala.util.{Failure, Success}


object HttpServer {

  def start(eligibilityActor: ActorRef[EligibilityCommand])(implicit system: ActorSystem[_]): Unit = {
    // Akka HTTP still needs a classic ActorSystem to start

    import system.executionContext

    val routeHandler = new HttpRouter(eligibilityActor)

    val port = sys.env.get("HTTP_PORT").fold(8080)(_.toInt)
    val futureBinding = Http().newServerAt("0.0.0.0", port).bind(routeHandler.routes)
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