package com.applaudo.akkalms.controllers

import akka.http.scaladsl.server.Route
import com.applaudo.akkalms.dao.{CategoryDaoImpl, FinanceDaoImpl, IncomeDaoImpl}
import com.applaudo.akkalms.enums.{Currencies, IncomeTypes, Months}
import com.applaudo.akkalms.models.errors.{ErrorInfo, InternalServerError}
import com.applaudo.akkalms.models.forms.GetFinancesEndpointArguments
import com.applaudo.akkalms.models.requests.{AddFinanceRequest, AddIncomeRequest, UpdateFinanceRequest, UpdateIncomeRequest}
import com.applaudo.akkalms.models.responses.{FinanceResponse, IncomeResponse}
import com.typesafe.scalalogging.LazyLogging
import io.circe._
import io.circe.generic.auto._
import sttp.tapir._
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import sttp.model.StatusCode
import sttp.tapir.server.akkahttp.{AkkaHttpServerInterpreter, AkkaHttpServerOptions}

import scala.concurrent.{ExecutionContext, Future}


class FinanceController(baseController: BaseController, financeDao:FinanceDaoImpl, incomeDao: IncomeDaoImpl)(implicit ec: ExecutionContext)
  extends LazyLogging {

  implicit val enumDecoder: Decoder[Months.Month] = Decoder.decodeEnumeration(Months)
  implicit val enumEncoder: Encoder[Months.Month] = Encoder.encodeEnumeration(Months)

  implicit val enumCurrencyDecoder: Decoder[Currencies.Currency] = Decoder.decodeEnumeration(Currencies)
  implicit val enumCurrencyEncoder: Encoder[Currencies.Currency] = Encoder.encodeEnumeration(Currencies)

  implicit val enumIncomeTypeDecoder: Decoder[IncomeTypes.IncomeType] = Decoder.decodeEnumeration(IncomeTypes)
  implicit val enumIncomeTypeEncoder: Encoder[IncomeTypes.IncomeType] = Encoder.encodeEnumeration(IncomeTypes)

  val createFinanceEndpoint: Endpoint[Unit, AddFinanceRequest, ErrorInfo, (FinanceResponse, StatusCode), Any] =
    baseController.baseEndpoint()
      .post
      .in("finance")
      .summary("Add a new personal finance")
      .tag("Finance")
      .in(
        jsonBody[AddFinanceRequest]
          .description("Finance object that needs to be added")
          .example(AddFinanceRequest(2022, Months.APRIL, List(AddIncomeRequest(IncomeTypes.SALARY, 1000.27, Currencies.USD, None))))
      )
      .out(jsonBody[FinanceResponse])
      .out(statusCode.description(StatusCode.Created, "Successful created the personal finance"))

  val patchFinanceEndpoint: Endpoint[Unit, UpdateFinanceRequest, ErrorInfo, (FinanceResponse, StatusCode), Any] =
    baseController.baseEndpoint()
      .patch
      .in("finance")
      .summary("Update a personal finance")
      .tag("Finance")
      .in(
        jsonBody[UpdateFinanceRequest]
          .description("Finance object that needs to be updated")
          .example(UpdateFinanceRequest(1, 2022, Months.JUNE.toString, List(UpdateIncomeRequest(1, IncomeTypes.SALARY.toString, 1000.27, Currencies.USD.toString, None))))
      )
      .out(jsonBody[FinanceResponse])
      .out(statusCode.description(StatusCode.Ok, "Successful updated the personal finance"))


  val getFinanceEndpoint: Endpoint[Unit, GetFinancesEndpointArguments, ErrorInfo, List[FinanceResponse], Any] =
    baseController.baseEndpoint()
      .get
      .in("finance")
      .summary("Returns a list of personal finances")
      .tag("Finance")
      .in(EndpointInput.derived
        [GetFinancesEndpointArguments]) // arguments described in that class
      .out(
        jsonBody[List[FinanceResponse]]
      )


  //val customDecodeFailureHandler: DecodeFailureHandler = ???

/*  val customServerOptions: AkkaHttpServerOptions = AkkaHttpServerOptions
    .customiseInterceptors
    //.decodeFailureHandler(customDecodeFailureHandler)
    .decodeFailureHandler(ctx => {
      ctx.failingInput match {
        // when defining how a decode failure should be handled, we need to describe the output to be used, and
        // a value for this output

        case EndpointInput.derived. => Some(ValuedEndpointOutput(stringBody, StatusCode.Created))
        case EndpointInput.Query(_, _, _, _) => Some(ValuedEndpointOutput(stringBody, "Incorrect format!!!"))
        // in other cases, using the default behavior
        case _ => DefaultDecodeFailureHandler.default(ctx)
      }
    })
    .options*/
  // converting an endpoint to a route (providing server-side logic); extension method comes from imported packages
  val createFinanceRoute: Route =
    AkkaHttpServerInterpreter().toRoute(createFinanceEndpoint.serverLogic(createFinanceLogic))

  val updateFinanceRoute: Route =
    AkkaHttpServerInterpreter().toRoute(patchFinanceEndpoint.serverLogic(updateFinanceLogic))

  val getFinanceRoute: Route =
    AkkaHttpServerInterpreter().toRoute(getFinanceEndpoint.serverLogic(getFinanceLogic))

  def createFinanceLogic(finance: AddFinanceRequest): Future[Either[ErrorInfo, (FinanceResponse, StatusCode)]] =
    Future {
      logger.info("Adding a new Personal Finance")
      //TODO Validate if already exists a personal finance with the same year and month
      // TODO research rollback, for example if fails some income it should be rollback
      val addIncomeResponse: IncomeResponse = IncomeResponse(1, finance.incomes.head.incomeType.toString, finance.incomes.head.amount, finance.incomes.head.currency.toString, finance.incomes.head.note)
      val list: List[IncomeResponse] = List(addIncomeResponse)
      val financeResDao = financeDao.saveFinance(finance)
      val ids: List[Int] = finance.incomes.map(income => incomeDao.saveIncomes(financeResDao.id, income))
      val addFinanceResponse: FinanceResponse = FinanceResponse(financeResDao.id, financeResDao.year, financeResDao.month, list)
      if(1 == 1)
        Right(addFinanceResponse -> StatusCode.Created)
        //Right[Unit, (FinanceResponse, StatusCode)](addFinanceResponse -> StatusCode.Created)
      else
        Left(InternalServerError("Some error", StatusCode.InternalServerError.code))
    }

  def updateFinanceLogic(finance: UpdateFinanceRequest): Future[Either[ErrorInfo, (FinanceResponse, StatusCode)]] =
    Future {
      val incomeResponse: IncomeResponse = IncomeResponse(finance.incomes.head.id, finance.incomes.head.incomeType, finance.incomes.head.amount, finance.incomes.head.currency, finance.incomes.head.note)
      val list: List[IncomeResponse] = List(incomeResponse)
      val addFinanceResponse: FinanceResponse = FinanceResponse(finance.id, finance.year, finance.month, list)
      Right[ErrorInfo, (FinanceResponse, StatusCode)](addFinanceResponse -> StatusCode.Ok)
    }

  def getFinanceLogic(queryArgs: GetFinancesEndpointArguments): Future[Either[ErrorInfo, List[FinanceResponse]]] =
    Future {
      val income = IncomeResponse(1, "SALARY", 1000.27, "USD", None)
      val financeResp = FinanceResponse(1, 2023, "JANUARY", List(income))
      val response = List(financeResp)
      Right(response)
    }

  val financeEndpoints = List(createFinanceEndpoint, patchFinanceEndpoint, getFinanceEndpoint)
  /** Convenient way to assemble endpoints from the controller and then concat this route to main route. */
  val financeRoutes: List[Route] = List(createFinanceRoute, updateFinanceRoute, getFinanceRoute)

}
