package com.wgmouton.eligibility

import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Scheduler, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.AskPattern.*
import akka.util.Timeout
import com.wgmouton.eligibility.boudaries.*
import com.wgmouton.eligibility.commands.*
import com.wgmouton.eligibility.entities.CreditCard
import com.wgmouton.eligibility.errors.EligibilityErrors
import com.wgmouton.eligibility.interactors.{QueryCards, QueryPersonEligibility}
import com.wgmouton.eligibility.types.*

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Simple wrapper function that splits the validation and message handling logic.
 * @param handleMessageF
 * @return
 */
private def handleCommandValidation(handleMessageF: InteractorCommand => Behavior[InteractorCommand]): InteractorCommand => Behavior[InteractorCommand] = {
  case QueryPersonEligibilityUsingPersonDetails(_, _, salary, replyTo) if salary < 0 =>
    replyTo.tell(Left(EligibilityErrors.REQUEST_VALIDATION_FAILED(String.format("the salary %d and must above 0", salary))))
    Behaviors.same
  case QueryPersonEligibilityUsingPersonDetails(_, creditScore, _, replyTo) if creditScore > 700 || creditScore < 0 =>
    replyTo.tell(Left(EligibilityErrors.REQUEST_VALIDATION_FAILED(String.format("a credit score of %d and must be between 0 and 700", creditScore))))
    Behaviors.same

  // Executes the messages
  case msg => handleMessageF(msg)
}


/**
 * Handles the routing of the messages received from the actor to the correct interactor.
 * @param creditCardEntity
 * @param queryPersonEligibility
 * @return
 */
private def handleCommand(
                           implicit creditCardEntity: CreditCard, queryPersonEligibility: QueryPersonEligibility
                         ): InteractorCommand => Behavior[InteractorCommand] = handleCommandValidation {

    case QueryPersonEligibilityUsingPersonDetails(name, creditScore, salary, replyTo) =>
      queryPersonEligibility.usingPersonDetails(name, creditScore, salary).value.foreach(replyTo.tell)
      Behaviors.same

  case _ =>
    Behaviors.same
}

/**
 * Starts and configures the actor
 * @param creditCardEntityGateway Inject the correct Gateway implementation for the CreditCardEntityGateway.
 * @param system ActorSystem
 * @return
 */
def apply(creditCardEntityGateway: CreditCardEntityGateway)(implicit system: ActorSystem[_]): Behavior[InteractorCommand] =
  Behaviors
    .supervise(Behaviors.setup[InteractorCommand] { context =>
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
