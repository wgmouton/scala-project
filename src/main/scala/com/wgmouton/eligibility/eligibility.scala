package com.wgmouton.eligibility

import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors
import scala.concurrent.ExecutionContext.Implicits.global

final case class PersonEligibilityScore(provider: Provider, name: String, apr: BigDecimal, cardScore: BigDecimal)

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
