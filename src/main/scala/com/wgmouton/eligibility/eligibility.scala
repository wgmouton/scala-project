package com.wgmouton.eligibility

import akka.actor.typed.{ActorRef, Behavior, Scheduler, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.AskPattern.*
import akka.util.Timeout
import com.wgmouton.eligibility.boudaries.*
import com.wgmouton.eligibility.entities.CreditCard
import com.wgmouton.eligibility.interactors.{QueryCards, QueryPersonEligibility}
import com.wgmouton.eligibility.types.*


import scala.concurrent.ExecutionContext.Implicits.global

private def handleCommand(
                           implicit creditCardEntity: CreditCard, queryPersonEligibility: QueryPersonEligibility
                         ): Command => Behavior[Command] = {
  case QueryPersonEligibilityUsingPersonDetails(name, creditScore, salary, replyTo) =>
    queryPersonEligibility.usingPersonDetails(name, creditScore, salary).value.foreach(replyTo.tell)
    Behaviors.same

  case _ =>
    Behaviors.same
}

def apply(creditCardEntityGateway: CreditCardEntityGateway): Behavior[Command] =
  Behaviors
    .supervise(Behaviors.setup[Command] { context =>
      val creditCardEntityGatewayActor = context.spawn(creditCardEntityGateway(), "CreditCardEntityGateway")
      context.watch(creditCardEntityGatewayActor)

      //Initialize entities
      implicit val creditCardEntity: CreditCard = new CreditCard(creditCardEntityGatewayActor)

      //Initialize interactors
      implicit val queryPersonEligibility: QueryPersonEligibility = new QueryPersonEligibility
      implicit val queryCards: QueryCards = new QueryCards

      //Start message handler
      Behaviors.receiveMessage(handleCommand)
    })
    .onFailure(SupervisorStrategy.restart)
