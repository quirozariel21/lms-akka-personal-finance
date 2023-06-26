package com.applaudo.akkalms.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.persistence.PersistentActor
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import akka.util.Timeout
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter

import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import akka.stream.ActorMaterializer
import com.applaudo.akkalms.actors.PersistentPersonalFinance.Command.CreatePersonalFinance
import com.applaudo.akkalms.actors.PersistentPersonalFinance.Response.PersonalFinanceCreateResponse
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


object PersistentPersonalFinance  extends App {


  //commands
  sealed trait Command
  object Command {
    case class CreatePersonalFinance(year: Int, month: String) extends Command
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


  class Accountant extends PersistentActor with ActorLogging {
    override def persistenceId: String = "simple-accountant"

    var latestPersonalFinanceId: Long = 0

    override def receiveRecover: Receive = {
      case PersonalFinanceCreateResponse(id) =>
        latestPersonalFinanceId = id
    }

    override def receiveCommand: Receive = {
      case CreatePersonalFinance(year, month) =>
        log.info(s"Receive personalFinance for year: $year")
        persist(PersonalFinanceCreateResponse(latestPersonalFinanceId)) { e =>
          latestPersonalFinanceId += 1
          log.info(s"Persisted $e as id ${e.id}")
        }
    }

  }

  val system = ActorSystem("PersistentActors")
  val accountant = system.actorOf(Props[Accountant], "simpleAccountant")

  for(i <- 1 to 10) {
    accountant ! CreatePersonalFinance(2023, "December")
  }
}




