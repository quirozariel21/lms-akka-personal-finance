package com.http

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import scala.io.StdIn


object HttpServerRoutingMinimal extends App {

  implicit val system = ActorSystem(Behaviors.empty, "my-system")
  implicit val executionContext = system.executionContext

  /*val route =
    path("hello") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
      }
    }*/

/*  val route = path("with-parameters") {
    get {
      parameters(Symbol("page").as[String], Symbol("size").as[String]) { (page, size) =>
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"parameters passed: page=$page and size=$size"))
      }
    }
  }*/

  // concatenate multiple routes
  val route = path("movies" / "heartbeat") {
    get {
      complete("Success")
    }
  } ~ path("movies" / "test") {
    get {
      complete("Verified")
    }
  }

  case class Movie(id: Int, name: String, length: Int)
  import spray.json.DefaultJsonProtocol._
  object jsonImplicits {
    implicit val movieFormat = jsonFormat3(Movie)
  }

  // use tilde operator
  def innerRoute(id: Int): Route = {
    get {
     complete {
       "Received GET request for order: " + id
     }
    }~
    put {
      complete {
        "Received PUT request for order: " + id
      }
    }
  }

  val tildeRoute = path("order" / IntNumber) { id => innerRoute(id)}

  val bindingFuture = Http().newServerAt("localhost", 8080).bind(tildeRoute)

  /*println(s"Server now online, Please navigate to http://localhost:8080/hello\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())*/

  bindingFuture.map { _ =>
    println("Successfully started on localhost:8080")
  } recover { case ex =>
    println("Failed to start the server due to: " + ex.getMessage)

  }

}
