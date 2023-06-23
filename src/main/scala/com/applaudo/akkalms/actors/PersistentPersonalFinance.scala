package com.applaudo.akkalms.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import akka.util.Timeout
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter

import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import akka.stream.ActorMaterializer
import com.applaudo.akkalms.db.PersonalFinanceDB
import com.applaudo.akkalms.db.PersonalFinanceDB.AddFinance
import com.applaudo.akkalms.entities.Finance
import com.applaudo.akkalms.enums.{Currencies, IncomeTypes, Months}
import com.applaudo.akkalms.models.requests.AddFinanceRequest
import com.applaudo.akkalms.models.responses.{FinanceResponse, IncomeResponse}

import scala.concurrent.duration._
import sttp.tapir._
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import sttp.tapir.generic.Derived
import sttp.tapir.Schema
import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success}

// state


object PersistentPersonalFinance {


  //commands
  sealed trait Command
  object Command {
    case class CreatePersonalFinance(year: Int, month: String, replyTo: ActorRef[Response]) extends Command
  }

  // events = to persist to Cassandra
  trait Event
  case class PersonalFinanceCreated(personalFinance: PersonalFinance) extends Event

  // state
  //Finance -> it is into entities package
  case class PersonalFinance(id: Long, year: Int, month: String)

  //responses
  sealed trait Response
  object Response {
    case class PersonalFinanceCreateResponse(id: Long) extends Response
  }

  import Command._
  import Response._

  val commandHandler: (PersonalFinance, Command) => Effect[Event, PersonalFinance] = (state, command) =>
    command match {
      case CreatePersonalFinance(year, month, replyTo) =>
        val id = state.id
        Effect
        .persist(PersonalFinanceCreated(PersonalFinance(id, year, month))) // Cassandra
        .thenReply(replyTo)(_ => PersonalFinanceCreateResponse(id))
    }

  val eventHandler: (PersonalFinance, Event) => PersonalFinance = (state, event) =>
    event match {
      case PersonalFinanceCreated(personalFinance) => personalFinance
    }

  def apply(id: Long): Behavior[Command] =
    EventSourcedBehavior[Command, Event, PersonalFinance](
      persistenceId = PersistenceId.ofUniqueId(id.toString),
      emptyState = PersonalFinance(id, 2023, ""),
      commandHandler = commandHandler,
      eventHandler = eventHandler
    )
}

class PersistentPersonalFinance {//extends Actor with ActorLogging {

}



/*  // starting the server
  val routes = {
    import akka.http.scaladsl.server.Directives._
    concat(helloWorldRoute, createFinanceRoute, swaggerUIRoute)
  }*/

  /*//Http().newServerAt("localhost", 8080).bind(routes)
  val bindAndCheck = Http().newServerAt("localhost", 8080).bindFlow(routes).map { _ =>
    // testing
    println("Go to: http://localhost:8080/docs")
    println("Press any key to exit ...")
    scala.io.StdIn.readLine()
  }*/

  // cleanup
