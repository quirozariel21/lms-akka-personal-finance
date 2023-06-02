package com.applaudo.akkalms.actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter

import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import akka.stream.ActorMaterializer
import com.applaudo.akkalms.models.requests.AddFinanceRequest
import com.applaudo.akkalms.models.responses.{AddFinanceResponse, AddIncomeResponse}

import scala.concurrent.duration._
import sttp.tapir._
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import sttp.tapir.generic.Derived
import sttp.tapir.Schema
import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir.swagger.bundle.SwaggerInterpreter

// state





object PersistentPersonalFinance {
  case class AddFinance(finance: AddFinanceRequest)
  case object OperationSuccess

}

class PersistentPersonalFinance extends Actor with ActorLogging {
  import PersistentPersonalFinance._

  var finances = Map[String, AddFinanceResponse]()

  override def receive: Receive = {
    case AddFinance(finance) =>
      log.info(s"trying to add finance $finance")
      val addIncomeResponse: AddIncomeResponse = new AddIncomeResponse(1, finance.incomes.head.incomeType, finance.incomes.head.amount, finance.incomes.head.currency, "")
      val list: List[AddIncomeResponse] = List(addIncomeResponse)
      val addFinanceResponse: AddFinanceResponse = new AddFinanceResponse(1, finance.year, finance.month, list)
      sender() ! addFinanceResponse
  }
}

object MarshallJSON extends App {

  implicit val system = ActorSystem("MarshallJSON")
  //implicit val materializer = ActorMaterializer()
  import system.dispatcher
  import PersistentPersonalFinance._

  val rtjvmGameMap = system.actorOf(Props[PersistentPersonalFinance], "rockTheJVMGameAreaMap")

  implicit val timeout = Timeout(2 seconds)

  //val addFinanceRequest: EndpointIO[AddFinanceRequest] = jsonBody[AddFinanceRequest]

  val baseEndpoint = endpoint.in("api" / "v1")

  /*implicit lazy val addFinanceRequestSchema: Schema[AddFinanceRequest] = //Schema.derive
  implicitly[Derived[Schema[AddFinanceRequest]]].value.modify(_.year)(_.description("How many fruits?"))*/

  val createFinanceEndpoint: Endpoint[Unit, AddFinanceRequest, Unit, (AddFinanceResponse, StatusCode), Any] =
    baseEndpoint
      .post
      .in("finance")
      .summary("Add a new personal finance")
      .tag("Finance")
      .in(
        jsonBody[AddFinanceRequest]//.schema
          .description("Finance object that needs to be added")
          //.example(Any, "")
      )
      //.out(statusCode.description(StatusCode.Created, "Created"))
      .out(jsonBody[AddFinanceResponse])
      .out(statusCode.description(StatusCode.Created, "Created"))
      //.errorOut(statusCode)


def createFinanceLogic(finance: AddFinanceRequest): Future[Either[Unit, (AddFinanceResponse, StatusCode)]] =
  Future {
    val addIncomeResponse: AddIncomeResponse = AddIncomeResponse(1, finance.incomes.head.incomeType, finance.incomes.head.amount, finance.incomes.head.currency, "")
    val list: List[AddIncomeResponse] = List(addIncomeResponse)
    val addFinanceResponse: AddFinanceResponse = AddFinanceResponse(1, finance.year, finance.month, list)
    Right[Unit, (AddFinanceResponse, StatusCode)](addFinanceResponse -> StatusCode.Created)
  }

  /*//def countCharacters(req: AddFinanceRequest): Future[Either[StatusCode, Future[AddFinanceResponse]]] = {
  def countCharacters(req: AddFinanceRequest): Future[Either[StatusCode, StatusCode]] = {
    //val optionFuture = (rtjvmGameMap ? AddFinance(req)).mapTo[AddFinanceResponse]
    //complete(StatusCodes.Created, optionFuture)
    //Future.successful(Right(optionFuture))
    Future.successful(Right(StatusCode.Accepted))
  }*/


/*  val countServer: Route =
    AkkaHttpServerInterpreter()
      .toRoute(
        createFinanceEndpoint
          .serverLogic( countCharacters
          /*request => {
          val optionFuture = (rtjvmGameMap ? AddFinance(request)).mapTo[AddFinanceResponse]
          Future.successful(Right(StatusCode.Created -> optionFuture))
          }*/
        )
      )
*/

  val helloWorld: PublicEndpoint[String, Unit, String, Any] =
    baseEndpoint.get.in("hello").in(query[String]("name")).out(stringBody)

  // converting an endpoint to a route (providing server-side logic); extension method comes from imported packages
  val helloWorldRoute: Route =
    AkkaHttpServerInterpreter().toRoute(helloWorld.serverLogicSuccess(name => Future.successful(s"Hello, $name!")))

  val createFinanceRoute: Route =
    AkkaHttpServerInterpreter().toRoute(createFinanceEndpoint.serverLogic(createFinanceLogic))

  // generating and exposing the documentation in yml
  val swaggerUIRoute =
    AkkaHttpServerInterpreter().toRoute(
      SwaggerInterpreter().fromEndpoints[Future](List(helloWorld, createFinanceEndpoint), "Personal Finance API", "1.0.0")
    )

  // starting the server
  val routes = {
    import akka.http.scaladsl.server.Directives._
    concat(helloWorldRoute, createFinanceRoute, swaggerUIRoute)
  }

  //Http().newServerAt("localhost", 8080).bind(routes)
  val bindAndCheck = Http().newServerAt("localhost", 8080).bindFlow(routes).map { _ =>
    // testing
    println("Go to: http://localhost:8080/docs")
    println("Press any key to exit ...")
    scala.io.StdIn.readLine()
  }

  // cleanup
  Await.result(bindAndCheck.transformWith { r => system.terminate().transform(_ => r) }, Duration.Inf)

}