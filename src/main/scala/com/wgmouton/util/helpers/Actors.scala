//package com.wgmouton.util.helpers
//
//import akka.actor.typed.{ActorRef, Behavior}
//import akka.actor.typed.scaladsl.Behaviors
//
//import scala.concurrent.Future
//
//object Actors {
//
//  def asyncReply[R](v: Future[R], replyTo: ActorRef[R]): Behavior[R] = {
//    v.foreach(replyTo.tell)
//    Behaviors.same[R]
//  }
//}
