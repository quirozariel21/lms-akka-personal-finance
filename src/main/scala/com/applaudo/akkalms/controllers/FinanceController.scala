package com.applaudo.akkalms.controllers

import akka.http.scaladsl.server.Route
import com.applaudo.akkalms.dao.{CategoryDaoImpl, FinanceDaoImpl, IncomeDaoImpl}
import com.applaudo.akkalms.entities.GenerateSummaryEntity
import com.applaudo.akkalms.enums.{Currencies, IncomeTypes, Months}
import com.applaudo.akkalms.models.errors.{BadRequest, ErrorInfo, InternalServerError, NoContent}
import com.applaudo.akkalms.models.forms.{FinancePathArguments, GetFinancesEndpointArguments}
import com.applaudo.akkalms.models.requests.{AddFinanceRequest, AddIncomeRequest, UpdateFinanceRequest, UpdateIncomeRequest}
import com.applaudo.akkalms.models.responses.{BalanceResponse, CategorySummaryReportResponse, FinanceResponse, GenerateSummaryResponse, GetIncomeResponse, IncomeResponse, SubcategoryResponse}
import com.applaudo.akkalms.services.FinanceService
import com.typesafe.scalalogging.LazyLogging
import io.circe._
import io.circe.generic.auto._
import sttp.tapir._
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import sttp.model.StatusCode
import sttp.tapir.server.akkahttp.{AkkaHttpServerInterpreter, AkkaHttpServerOptions}
import sttp.tapir.server.model.ValuedEndpointOutput

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}


class FinanceController(baseController: BaseController,
                        financeDao:FinanceDaoImpl,
                        incomeDao: IncomeDaoImpl)(implicit ec: ExecutionContext) extends LazyLogging {

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
          .example(
            AddFinanceRequest(2022, Months.APRIL,
              List(AddIncomeRequest(IncomeTypes.SALARY, 1000.27, Currencies.USD, None)))
          )
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
          .example(
            UpdateFinanceRequest(1, 2022, Months.JUNE.toString,
              List(UpdateIncomeRequest(1, IncomeTypes.SALARY.toString, 1000.27, Currencies.USD.toString, None)))
          )
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

  val generateSummaryEndpoint: Endpoint[Unit, FinancePathArguments, ErrorInfo, GenerateSummaryResponse, Any] =
    baseController.baseEndpoint()
      .get
      .in(EndpointInput.derived[FinancePathArguments])
      .summary("Generate summary report by financeId")
      .tag("Finance")
      .out(jsonBody[GenerateSummaryResponse])
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

/*  case class MyFailure(msg: String)
  def myFailureResponse(m: String): ValuedEndpointOutput[_] =
    ValuedEndpointOutput(jsonBody[MyFailure], MyFailure(m))
  val myServerOptions: AkkaHttpServerOptions = AkkaHttpServerOptions
    .customiseInterceptors.defaultHandlers(myFailureResponse).options*/

  // converting an endpoint to a route (providing server-side logic); extension method comes from imported packages
  val createFinanceRoute: Route =
    AkkaHttpServerInterpreter().toRoute(createFinanceEndpoint.serverLogic(createFinanceLogic))

  val updateFinanceRoute: Route =
    AkkaHttpServerInterpreter().toRoute(patchFinanceEndpoint.serverLogic(updateFinanceLogic))

  val getFinanceRoute: Route =
    AkkaHttpServerInterpreter().toRoute(getFinanceEndpoint.serverLogic(getFinanceLogic))

  val generateSummaryRoute: Route =
    AkkaHttpServerInterpreter().toRoute(generateSummaryEndpoint.serverLogic(generateSummaryLogic))

  def createFinanceLogic(financeRequest: AddFinanceRequest): Future[Either[ErrorInfo, (FinanceResponse, StatusCode)]] =
    Future {
      logger.info("Adding a new Personal Finance")
      val financesVal = financeDao.findByYearAndMonth(Some(financeRequest.year), Some(financeRequest.month))
      /*if(financesVal.nonEmpty) {
        logger.info("Personal finance invalid!")
        Left(BadRequest(s"Personal Finance with year:${financeRequest.year} and month: ${financeRequest.month.toString} already exists", StatusCode.BadRequest.code))
      } else {
        // TODO research rollback, for example if fails some income it should be rollback
        val financeResDao = financeDao.save(financeRequest)
        val ids: List[Int] = financeRequest.incomes.map(income => incomeDao.save(financeResDao.id, income))
        val incomesResponse = ids.map(id => {
          incomeDao.findById(id) match {
            case Some(i) => IncomeResponse(i.id, i.name, i.amount, i.currency, i.note)
            case None => throw new IllegalArgumentException("Some error happened")
          }
        })
        val addFinanceResponse= FinanceResponse(financeResDao.id, financeResDao.year, financeResDao.month, incomesResponse)
        Right(addFinanceResponse -> StatusCode.Created)
      }*/
      financeDao.findByYearAndMonth(Some(financeRequest.year), Some(financeRequest.month)) match {
        case p :: ps =>
          logger.info("Personal finance invalid!")
          //TODO change error to 422
          Left(BadRequest(s"Personal Finance with year:${financeRequest.year} and month: ${financeRequest.month.toString} already exists", StatusCode.BadRequest.code))
        case Nil =>
          // TODO research rollback, for example if fails some income it should be rollback
          val financeResDao = financeDao.save(financeRequest)
          val ids: List[Int] = financeRequest.incomes.map(income => incomeDao.save(financeResDao.id, income))
          val incomesResponse = ids.map(id => {
            incomeDao.findById(id) match {
              case Some(i) => IncomeResponse(i.id, i.name, i.amount, i.currency, i.note)
              case None => throw new IllegalArgumentException(s"incomeId: ${id} not found")
            }
          })
          val incomeTotalAmount = incomesResponse.map(_.amount).sum
          val currency = incomesResponse.map(_.currency).head
          logger.info("Income total amount: {} {}", incomeTotalAmount, currency)
          val getIncomeResponse = GetIncomeResponse(incomeTotalAmount, currency, incomesResponse)
          val addFinanceResponse= FinanceResponse(financeResDao.id, financeResDao.year, financeResDao.month, getIncomeResponse)
          Right(addFinanceResponse -> StatusCode.Created)
      }
    }

  def updateFinanceLogic(finance: UpdateFinanceRequest): Future[Either[ErrorInfo, (FinanceResponse, StatusCode)]] =
    Future {
      val incomeResponse: IncomeResponse = IncomeResponse(finance.incomes.head.id, finance.incomes.head.incomeType, finance.incomes.head.amount, finance.incomes.head.currency, finance.incomes.head.note)
      val list: List[IncomeResponse] = List(incomeResponse)
      val incomeTotalAmount = list.map(_.amount).sum
      val currency = list.map(_.currency).head
      logger.info("Income total amount: {} {}", incomeTotalAmount, currency)
      val getIncomeResponse = GetIncomeResponse(incomeTotalAmount, currency, list)
      val addFinanceResponse: FinanceResponse = FinanceResponse(finance.id, finance.year, finance.month, getIncomeResponse)
      Right[ErrorInfo, (FinanceResponse, StatusCode)](addFinanceResponse -> StatusCode.Ok)
    }

  def getFinanceLogic(queryArgs: GetFinancesEndpointArguments): Future[Either[ErrorInfo, List[FinanceResponse]]] =
    Future {
      logger.info(s"Getting personal finances by year: '${queryArgs.year}' and/or month: '${queryArgs.month}'")

      val personalFinances = financeDao.findByYearAndMonth(queryArgs.year, queryArgs.month)
      logger.info("Is Empty: {}", personalFinances.isEmpty)
      if(personalFinances.isEmpty) {
        Left(NoContent)
      } else {
        val response = personalFinances.map(elem => {
          val incomeList = elem.incomes.split("#").toList.map( e => {
            val incomeValues = e.split('|')

            IncomeResponse(incomeValues(0).toLong, incomeValues(1),
                           BigDecimal(incomeValues(3)), incomeValues(2),
                           Some(incomeValues(4)))
          })
          val incomeTotalAmount = incomeList.map(_.amount).sum
          val currency = incomeList.map(_.currency).head
          logger.info("Income total amount: {} {}", incomeTotalAmount, currency)
          val getIncomeResponse = GetIncomeResponse(incomeTotalAmount, currency, incomeList)
          FinanceResponse(elem.id, elem.year, elem.month, getIncomeResponse)
        })
        Right(response)
      }
    }

  def generateSummaryLogic(pathArguments: FinancePathArguments): Future[Either[ErrorInfo, GenerateSummaryResponse]] = {
    logger.info("Generating summary report for the personal finance id: {}", pathArguments.financeId)
    financeDao.generateSummary(pathArguments.financeId) match {
      case Nil => Future.successful[Either[ErrorInfo, GenerateSummaryResponse]](Left(NoContent))
      case p :: ps =>
        val mapCategory: Map[String, List[GenerateSummaryEntity]] = ps.groupBy(_.categoryName)
        logger.info("mapCategory: {}", mapCategory)
        val finance = financeDao.findById(pathArguments.financeId).get

        var categories = new ListBuffer[CategorySummaryReportResponse]
        for((k, v) <- mapCategory)
        {
          //where k is key and v is value
          print("Key:"+k+", ")
          println("Value:"+v)

          val subcategories = v.map(v => SubcategoryResponse(v.subcategoryId, v.subcategoryName, v.note, v.amount, v.currency, v.expensedDate))
          val totalAmount = v.map(_.amount).sum
          val category = v.findLast(v => v.categoryName == k).get
          val categoryObj =CategorySummaryReportResponse(category.categoryId, category.categoryName, subcategories, totalAmount, category.currency)
          categories += categoryObj
        }
        val totalSpent = categories.map(_.totalAmount).sum
        val balance = BalanceResponse(finance.totalReceived, totalSpent, finance.totalReceived - totalSpent)

        val response = GenerateSummaryResponse(finance.id, finance.year, finance.month, balance, categories.toList)
        Future.successful[Either[ErrorInfo, GenerateSummaryResponse]](Right(response))
    }
  }

  val financeEndpoints = List(createFinanceEndpoint, patchFinanceEndpoint, getFinanceEndpoint, generateSummaryEndpoint)
  /** Convenient way to assemble endpoints from the controller and then concat this route to main route. */
  val financeRoutes: List[Route] = List(createFinanceRoute, updateFinanceRoute, getFinanceRoute, generateSummaryRoute)

}
