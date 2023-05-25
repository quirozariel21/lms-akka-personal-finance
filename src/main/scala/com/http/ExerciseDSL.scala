package com.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import spray.json._

import scala.concurrent.duration._
import scala.util.{Failure, Success}

case class Person(pin: Int, name:String)

trait PersonJsonProtocol extends DefaultJsonProtocol {
  implicit val personJson = jsonFormat2(Person)
}

object ExerciseDSL extends App with PersonJsonProtocol {

  implicit val system = ActorSystem("DSLExercise")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  var people = List(
    Person(1, "Ariel"),
    Person(2, "Alina"),
    Person(3, "Gael")
  )

  val personServerRoute =
    pathPrefix("api" / "people") {
      get {
        (path(IntNumber) | parameter('pin.as[Int])) { pin =>
          complete(
            HttpEntity(
              ContentTypes.`application/json`,
              people.find(_.pin == pin).toJson.prettyPrint
            )
          )
        } ~
        pathEndOrSingleSlash {
          complete(
            HttpEntity(
              ContentTypes.`application/json`,
              people.toJson.prettyPrint
            )
          )
        }
      } ~
      ( post & pathEndOrSingleSlash & extractRequest & extractLog ) { (request, log) =>
          val entity = request.entity
          val strictEntityFuture = entity.toStrict(2 seconds)
          val personFuture = strictEntityFuture.map(_.data.utf8String.parseJson.convertTo[Person])

          onComplete(personFuture) {
            case Success(person) =>
              log.info(s"Got person: $person")
              people =people :+ person
              complete(StatusCodes.OK)
            case Failure(ex) =>
              failWith(ex)
          }


      }
    }

  Http().bindAndHandle(personServerRoute, "localhost", 8080)
}
