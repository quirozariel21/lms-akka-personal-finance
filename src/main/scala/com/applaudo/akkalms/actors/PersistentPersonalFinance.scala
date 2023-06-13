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
import com.applaudo.akkalms.db.PersonalFinanceDB
import com.applaudo.akkalms.db.PersonalFinanceDB.AddFinance
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





/*object PersistentPersonalFinance {
  case class AddFinance(finance: AddFinanceRequest)
  case object OperationSuccess

}*/

class PersistentPersonalFinance {//extends Actor with ActorLogging {
  //import PersistentPersonalFinance._

  //var finances = Map[String, FinanceResponse]()

  /*override def receive: Receive = {
    case AddFinance(finance) =>
      log.info(s"trying to add finance $finance")
      val addIncomeResponse: IncomeResponse = new IncomeResponse(1, finance.incomes.head.incomeType, finance.incomes.head.amount, finance.incomes.head.currency, None)
      val list: List[IncomeResponse] = List(addIncomeResponse)
      val addFinanceResponse: FinanceResponse = FinanceResponse(1, finance.year, finance.month, list)
      sender() ! addFinanceResponse
  }*/
}

object MarshallJSON extends App {

  implicit val system = ActorSystem("MarshallJSON")
  //implicit val materializer = ActorMaterializer()
  import system.dispatcher
  import PersonalFinanceDB._

  val rtjvmGameMap = system.actorOf(Props[PersonalFinanceDB], "rockTheJVMGameAreaMap")

  implicit val timeout = Timeout(5 seconds)

  //val addFinanceRequest: EndpointIO[AddFinanceRequest] = jsonBody[AddFinanceRequest]

  val baseEndpoint = endpoint.in("api" / "v1")

  /*implicit lazy val addFinanceRequestSchema: Schema[AddFinanceRequest] = //Schema.derive
  implicitly[Derived[Schema[AddFinanceRequest]]].value.modify(_.year)(_.description("How many fruits?"))*/

  val createFinanceEndpoint: Endpoint[Unit, AddFinanceRequest, Unit, (FinanceResponse, StatusCode), Any] =
    baseEndpoint
      .post
      .in("finance")
      .summary("Add a new personal finance")
      .tag("Finance")
      .in(
        jsonBody[AddFinanceRequest]//.schema
          .description("Finance object that needs to be added")
      )
      .out(jsonBody[FinanceResponse])
      .out(statusCode.description(StatusCode.Created, "Created"))


def createFinanceLogic(finance: AddFinanceRequest): Future[Either[Unit, (FinanceResponse, StatusCode)]] =
  Future {

    val optionFuture: Future[FinanceResponse] = (rtjvmGameMap ? AddFinance(finance)).mapTo[FinanceResponse]
    println(s"RESULT1 ====>> " + optionFuture)
    val entityFuture = optionFuture.map { op =>
      FinanceResponse(op.id, op.year, op.month, op.incomes)
    }
    println(s"RESULT2 ====>> " + entityFuture)

    entityFuture.onComplete {
      case Success(value) => println(s" SUCCESS $value")
      case Failure(exception) => println(s"Failure $exception")
    }

    val incomes = new ListBuffer[IncomeResponse]()
    finance.incomes.foreach{ in =>
      val incomeResponse = IncomeResponse(1, in.incomeType, in.amount, in.currency, in.note)
      incomes += incomeResponse
    }

    val financeResponse: FinanceResponse = FinanceResponse(1,
      finance.year, finance.month, incomes.toList)

    Thread.sleep(9000)
    Right[Unit, (FinanceResponse, StatusCode)](financeResponse -> StatusCode.Created)

    //Right[Unit, (FinanceResponse, StatusCode)](entityFuture.value.get.get -> StatusCode.Created)
    /*entityFuture.onComplete{
      case Success(value) => Right[Unit, (FinanceResponse, StatusCode)](value -> StatusCode.Created)
      //case Failure(exception) => Left[Unit, (FinanceResponse, StatusCode)](value -> StatusCode.Created)

    }*/
    //Right[Unit, (FinanceResponse, StatusCode)](entityFuture -> StatusCode.Created)
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