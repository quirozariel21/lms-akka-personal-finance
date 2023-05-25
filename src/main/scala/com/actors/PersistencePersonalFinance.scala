package com.actors

import akka.actor.typed.ActorRef

object PersistencePersonalFinance {

  // commands - messages
  sealed trait Command
  object Command {
    case class CreateCategory(key: String, value: String, description: String, replyTo: ActorRef[Response]) extends Command
    case class CreateSubCategory(key: String, value: String, description: String, categoryId: Int, replyTo: ActorRef[Response]) extends Command
    case class CreateMainFinance(year: Int, month: String, incomes: List[String])
  }

  // state
  case class

  // response
  sealed trait Response
  object Response {

  }
}
