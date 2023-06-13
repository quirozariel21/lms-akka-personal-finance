package com.applaudo.akkalms.controllers

import akka.http.scaladsl.server.Route
import com.applaudo.akkalms.dao.CategoryDaoImpl
import com.applaudo.akkalms.models.errors.{ErrorInfo, InternalServerError}
import com.applaudo.akkalms.models.requests.AddCategoryRequest
import com.applaudo.akkalms.models.responses.CategoryResponse
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.{Endpoint, EndpointInput, oneOf, oneOfVariant, statusCode, stringBody}
import sttp.tapir.generic.auto._
import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir.server.akkahttp.{AkkaHttpServerInterpreter, AkkaHttpServerOptions}

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}

class CategoryController(baseController: BaseController, categoryDao: CategoryDaoImpl)(implicit ec: ExecutionContext) {

  val createCategoryEndpoint: Endpoint[Unit, AddCategoryRequest, ErrorInfo, (CategoryResponse, StatusCode), Any] =
    baseController.baseEndpoint()
      .post
      .in("category")
      .summary("Add a new category")
      .tag("Category")
      .in(
        jsonBody[AddCategoryRequest]
          .description("Category object that need to be added")
          .example(AddCategoryRequest("Gym", Some("Some description"), None))
      )
      .out(jsonBody[CategoryResponse])
      .out(statusCode.description(StatusCode.Created, "Successful created the category"))
      .errorOut(
        oneOf[ErrorInfo](
          //oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[NotFound].description("When something not found"))),
          //oneOfVariant(statusCode(StatusCode.BadRequest).and(jsonBody[BadRequest].description("Bad request"))),
          oneOfVariant(statusCode(StatusCode.InternalServerError).and(jsonBody[InternalServerError].description("Internal Server Error")))
      ))

  val getCategoriesEndpoint: Endpoint[Unit, Unit, Unit, List[CategoryResponse], Any] =
    baseController.baseEndpoint()
      .get
      .in("category")
      .summary("Returns a list of categories")
      .tag("Category")
      .out(
        jsonBody[List[CategoryResponse]]
          .description("List of categories")
      )

  val createCategoryRoute: Route =
    AkkaHttpServerInterpreter().toRoute(createCategoryEndpoint.serverLogic(createCategoryLogic))

  def createCategoryLogic(categoryRequest: AddCategoryRequest): Future[Either[ErrorInfo, (CategoryResponse, StatusCode)]] =
    Future {
      if(1 == 1) {
        val category = categoryDao.saveCategory(categoryRequest.name, categoryRequest.description, categoryRequest.subcategoryId)
        val categoryResponse = CategoryResponse(category.id, category.name, category.description, category.createdAt, category.subcategoryId, category.isActive)
        Right((categoryResponse -> StatusCode.Created))
      } else {
        Left(InternalServerError("Some error"))
      }
    }

  val getCategoriesRoute: Route =
    AkkaHttpServerInterpreter().toRoute(getCategoriesEndpoint.serverLogic( _ => {
      val categories =  categoryDao.getCategories()
      val response: List[CategoryResponse] = categories.map( c => CategoryResponse(c.id, c.name, c.description, c.createdAt, c.subcategoryId, c.isActive))
      Future.successful[Either[Unit, List[CategoryResponse]]](Right(response))
    }
    ))
    //AkkaHttpServerInterpreter().toRoute(getCategoriesEndpoint.serverLogic(getCategoriesLogic))




  def getCategoriesLogic(): Future[Either[Unit, List[CategoryResponse]]] = {
    //Future {
    val categories =  categoryDao.getCategories()
    val response: List[CategoryResponse] = categories.map( c => CategoryResponse(c.id, c.name, c.description, c.createdAt, c.subcategoryId, c.isActive))
    //Right[Unit, List[CategoryResponse]](List(CategoryResponse(1, "", None, LocalDateTime.now, None, false)))
    Future.successful(Right(response))
    //}
  }


  val categoryRoutes: List[Route] = List(createCategoryRoute, getCategoriesRoute)
}
