package com.wgmouton.eligibility

import akka.actor.typed.{ActorRef, Behavior, Scheduler, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.AskPattern.*
import akka.util.Timeout
import com.wgmouton.eligibility.interactors.Interactor

import scala.concurrent.ExecutionContext.Implicits.global

sealed trait Command
final case class GetPersonEligibilityScore(name: String, creditScore: Int, salary: Int, replyTo: ActorRef[Either[String, List[PersonEligibilityScore]]]) extends Command

def apply(): Behavior[Command] =
  Behaviors
    .supervise(Behaviors.setup[Command] { context =>
      Behaviors.receiveMessage {
        case GetPersonEligibilityScore(name, creditScore, salary, replyTo) =>
          Interactor
            .lookupPersonEligibility(name, creditScore, salary).value
            .foreach(replyTo.tell)
          Behaviors.same

        case _ =>
          Behaviors.same
      }
    })
    .onFailure(SupervisorStrategy.restart)

def getPersonEligibilityScore(name: String, creditScore: Int, salary: Int)(implicit actorRef: ActorRef[Command], timeout: Timeout, scheduler: Scheduler) = {
  actorRef.ask(GetPersonEligibilityScore(name, creditScore, salary, _))
}