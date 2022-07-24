package com.wgmouton.eligibility

import collection.mutable.Stack
import org.scalatest.flatspec.AnyFlatSpec
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActors, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class InteractorsSpec()
  extends TestKit(ActorSystem("InteractorsSpec"))
    with ImplicitSender
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  /*
  {
"name": "John Smith",
"creditScore": 500,
"salary": 28000
}
Response:
[
{
"provider": "ScoredCards"
"name": "ScoredCard Builder",
"apr": 19.4,
"cardScore": 0.212
},
{
"provider": "CSCards",
"name": "SuperSaver Card",
"apr": 21.4,
"cardScore": 0.137
},
{
"provider": "CSCards",
"name": "SuperSpender Card",
"apr": 19.2,
"cardScore": 0.135
  */
  "An Echo actor" must {

    "send back messages unchanged" in {

      val request = GetPersonEligibilityScore("John Smith", creditScore = 500, salary = 28000)
      val echo = system.actorOf(TestActors.echoActorProps)
      echo ! "hello world"
      expectMsg("hello world")
    }

  }

//  "lookup person eligibility" should "return this response given this input" in {
//
//    assert(false)
//  }
}
