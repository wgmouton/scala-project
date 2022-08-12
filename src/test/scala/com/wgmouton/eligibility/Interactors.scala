package com.wgmouton.eligibility

import collection.mutable.Stack
import org.scalatest.flatspec.AnyFlatSpec
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.{ActorSystem, Behavior}
import com.wgmouton.eligibility.boudaries.CreditCardEntityGateway
import com.wgmouton.eligibility.commands.*
import com.wgmouton.gateways.creditcard.Test
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.*
import scala.concurrent.Await

class InteractorsSpec extends AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  val testKit: ActorTestKit = ActorTestKit()
  implicit val system: ActorSystem[Nothing] = testKit.system

  override def afterAll(): Unit = testKit.shutdownTestKit()

}

class InteractorTest extends InteractorsSpec {
  "An eligibility actor" must {

    "send back this response" in {
      val creditCardEntityGateway: CreditCardEntityGateway = Test

      val eligibilityActor: Behavior[InteractorCommand] = apply(creditCardEntityGateway)
      val sender = testKit.spawn(eligibilityActor)
      val duration = 10.seconds

      val signalFut = sender.queryPersonEligibilityUsingPersonDetails("John Smith", creditScore = 500, salary = 28000)
      val signal = Await.result(signalFut, duration)
      print(signal)
      //      assert(signal == )


    }

  }

  //  "lookup person eligibility" should "return this response given this input" in {
  //
  //    assert(false)
  //  }
}
