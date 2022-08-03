import akka.actor.typed.{ActorRef, ActorSystem, Props}
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.config.ConfigFactory
import com.wgmouton.eligibility as EligibilityActor
import com.wgmouton.eligibility.boudaries.{CreditCardEntityGateway, InteractorCommand as EligibilityCommand}
import com.wgmouton.clients as ClientActor
import com.wgmouton.gateways.creditcard.Real

object Main extends App {
  val config = ConfigFactory.load()

  val rootBehavior = Behaviors.setup[Nothing] { context =>

    //Gateway Actors
    val creditCardEntityGateway: CreditCardEntityGateway = Real

    // Spawn Eligibility Supervisor and Actor
    val eligibilityActor: ActorRef[EligibilityCommand] = context.spawn(EligibilityActor(creditCardEntityGateway)(context.system), "eligibilityActor")
    context.watch(eligibilityActor)

    // Spawn Clients Supervisor and Actor
    val clientsActor: ActorRef[Nothing] = context.spawn(ClientActor(eligibilityActor), "clientActor")
    context.watch(clientsActor)

    Behaviors.empty
  }

  // Start Root Supervisor
  ActorSystem[Nothing](rootBehavior, "ClearScoreService")
}