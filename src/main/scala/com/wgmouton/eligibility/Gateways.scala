//package com.wgmouton.eligibility
//
//import akka.actor.typed.scaladsl.Behaviors
//import cats.data.EitherT
//import com.wgmouton.util.Gateway
//import com.wgmouton.eligibility.types.Provider
//
//import scala.concurrent.Future
//import scala.concurrent.ExecutionContext.Implicits.global
//
//
//
////object CSCardsGatewayImplementationStub extends Gateway with CSCardsGatewayBehavior {
////  val data: Map[(String, Int), List[CSCardsScore]] = Map(
////    ("John Smith", 26) -> List(
////      CSCardsScore(
////        cardName = "SuperSaver Card",
////        apr = 21.4,
////        eligibility = 6.3
////      ),
////      CSCardsScore(
////        cardName = "SuperSpender Card",
////        apr = 19.2,
////        eligibility = 5.0
////      )
////    )
////  )
////
////  override def getScore(name: String, creditScore: Int): EitherT[Future, String, List[CSCardsScore]] = {
////    EitherT.fromOption[Future](data.get((name, creditScore)), "Not Found")
////  }
////
////  //  def apply() = Behaviors.setup { context =>
////  //
////  //    Behaviors.receive{
////  //      case _ =>
////  //
////  //        getScore()
////  //        Behaviors.same
////  //    }
////  //    Behaviors.same
////  //  }
////}
//
////
////class CSCardsGatewayImplementationReal extends Gateway with CSCardsGatewayBehavior {
////
////  import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest}
////
////  override def getScore(name: String, creditScore: Int): EitherT[Future, String, List[CSCardsScore]] = {
////    val req = HttpRequest(
////      method = HttpMethods.POST,
////      uri = "https://userservice.example/users",
////      entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, "data")
////    )
////    EitherT.rightT[Future, String](List.empty[CSCardsScore])
////  }
////}
////
////
//
////object ScoredCardsGatewayImplementationStub extends Gateway with ScoredCardsGatewayBehavior {
////  val data: Map[(String, Int, Int), List[ScoredCardsScore]] = Map(
////    ("John Smith", 0, 0) -> List(
////      ScoredCardsScore(
////        card = "ScoredCard Builder",
////        apr = 19.4,
////        approvalRating = 0.8
////      )
////    )
////  )
////
////  override def getScore(name: String, creditScore: Int, salary: Int): EitherT[Future, String, List[ScoredCardsScore]] = {
////    EitherT.fromOption[Future](data.get((name, creditScore, salary)), "Not Found")
////  }
////}
////
////
////class ScoredCardsGatewayImplementationReal extends Gateway with ScoredCardsGatewayBehavior {
////
////  import akka.http.scaladsl.client._
////  import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest}
////
////  override def getScore(name: String, creditScore: Int, salary: Int): EitherT[Future, String, List[ScoredCardsScore]] = {
////    val req = HttpRequest(
////      method = HttpMethods.POST,
////      uri = "https://userservice.example/users",
////      entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, "data")
////    )
////    EitherT.rightT[Future, String](List.empty[ScoredCardsScore])
////  }
////}
//
//
