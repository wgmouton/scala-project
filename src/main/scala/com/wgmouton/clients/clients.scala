package com.wgmouton.clients

import akka.actor.typed.{ActorRef, ActorSystem, Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors
import com.wgmouton.clients.httpserver.HttpServer
import com.wgmouton.eligibility.Command


def apply(eligibilityActor: ActorRef[Command]) = Behaviors
  .supervise(Behaviors.setup[Nothing] { context =>
    // Start http server
    HttpServer.start(eligibilityActor)(context.system)
    Behaviors.empty
  })
  .onFailure(SupervisorStrategy.restart)