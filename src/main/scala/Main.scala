import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import com.wgmouton.eligibility as EligibilityActor
import com.wgmouton.clients as ClientActor


object Main {

  def main(args: Array[String]): Unit = {
    //#server-bootstrapping
    val rootBehavior = Behaviors.setup[Nothing] { context =>

      val eligibilityActor: ActorRef[EligibilityActor.Command] = context.spawn(EligibilityActor.apply(), "eligibilityActor")
      context.watch(eligibilityActor)

      val clientsActor: ActorRef[Nothing] = context.spawn(ClientActor.apply(eligibilityActor), "clientActor")
      context.watch(clientsActor)

      Behaviors.empty
    }
    ActorSystem[Nothing](rootBehavior, "ClearScoreService")
  }


  //      val userRegistryActor = context.spawn(UserRegistry(), "UserRegistryActor")
  //      context.watch(userRegistryActor)
  //
  //      val routes = new UserRoutes(userRegistryActor)(context.system)
  //      startHttpServer(routes.userRoutes)(context.system)
  //
  //      Behaviors.empty
  //    }
  //    val system = ActorSystem[Nothing](rootBehavior, "HelloAkkaHttpServer")
  //#server-bootstrapping


  //  override implicit val system: ActorSystem = ActorSystem()
  //  override implicit val executor: ExecutionContext = system.dispatcher

  //  override val config = ConfigFactory.load()
  //  override val logger = Logging(system, "ClearScoreService")

  //  val httpServer: ActorSystem[GreeterMain.SayHello] = ActorSystem(GreeterMain(), "AkkaQuickStart")
  //  val service: ActorSystem[Any] = ActorSystem("aaaa", "Service")

  //  Http()
  //    .newServerAt(
  //      interface = config.getString("http.interface"),
  //      port = config.getInt("http.port")
  //    )
  //    .bindFlow(routes)
}