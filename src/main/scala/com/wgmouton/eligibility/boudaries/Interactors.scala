package com.wgmouton.eligibility.boudaries

import akka.actor.typed.ActorRef
import com.wgmouton.eligibility.types.PersonEligibilityScore

sealed trait Command

final case class QueryPersonEligibilityUsingPersonDetails(
                                                           name: String,
                                                           creditScore: Int,
                                                           salary: Int,
                                                           replyTo: ActorRef[Either[String, List[PersonEligibilityScore]]]
                                                         ) extends Command

final case class QueryCardsByPlatform(
                                       platform: String,
                                       replyTo: ActorRef[Either[String, List[PersonEligibilityScore]]]
                                     ) extends Command

