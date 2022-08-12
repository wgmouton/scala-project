package com.wgmouton.eligibility.commands

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import com.wgmouton.eligibility.errors.EligibilityErrors
import com.wgmouton.eligibility.types.{CSCardsScore, PersonEligibilityScore, ScoredCardsScore}
import com.wgmouton.util.errors.ServiceError
import com.wgmouton.eligibility.types.*
import akka.actor.typed.scaladsl.AskPattern.*
import scala.concurrent.duration.*

import scala.concurrent.Future

sealed trait InteractorCommand

final case class QueryPersonEligibilityUsingPersonDetails(
                                                           name: String,
                                                           creditScore: Int,
                                                           salary: Int,
                                                           replyTo: ActorRef[Either[ServiceError, List[PersonEligibilityScore]]]
                                                         ) extends InteractorCommand

final case class QueryCardsByPlatform(
                                       platform: String,
                                       replyTo: ActorRef[Either[ServiceError, List[PersonEligibilityScore]]]
                                     ) extends InteractorCommand


/**
 * Implicit class wrapper for communicating with the eligibility service
 * @param actorRef
 */
implicit class InteractorCommands(actorRef: ActorRef[InteractorCommand]) {
  implicit val timeout: Timeout = 1.minute

  def queryPersonEligibilityUsingPersonDetails(name: String, creditScore: Int, salary: Int)(implicit system: ActorSystem[_]): Future[Either[ServiceError, List[PersonEligibilityScore]]] = {
    actorRef.ask(QueryPersonEligibilityUsingPersonDetails(name, creditScore, salary, _))
  }

  def queryCardsByPlatform(platform: String)(implicit system: ActorSystem[_]): Future[Either[ServiceError, List[PersonEligibilityScore]]] = {
    actorRef.ask(QueryCardsByPlatform(platform, _))
  }
}
