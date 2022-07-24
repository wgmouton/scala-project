import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.config.ConfigFactory
import com.wgmouton.eligibility as EligibilityActor
import com.wgmouton.clients as ClientActor

object Main extends App {
  val config = ConfigFactory.load()

  val rootBehavior = Behaviors.setup[Nothing] { context =>
    // Spawn Eligibility Supervisor and Actor
    val eligibilityActor: ActorRef[EligibilityActor.Command] = context.spawn(EligibilityActor(), "eligibilityActor")
    context.watch(eligibilityActor)

    // Spawn Clients Supervisor and Actor
    val clientsActor: ActorRef[Nothing] = context.spawn(ClientActor(eligibilityActor), "clientActor")
    context.watch(clientsActor)

    Behaviors.empty
  }

  // Start Root Supervisor
  ActorSystem[Nothing](rootBehavior, "ClearScoreService")
}