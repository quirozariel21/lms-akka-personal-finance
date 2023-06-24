package com.applaudo.akkalms.controllers

import akka.http.scaladsl.server.Route
import com.applaudo.akkalms.dao.CategoryDaoImpl
import com.applaudo.akkalms.models.errors.{ErrorInfo, NoContent, NotFound}
import com.applaudo.akkalms.models.forms.CategoryPathArguments
import com.applaudo.akkalms.models.requests.AddCategoryRequest
import com.applaudo.akkalms.models.responses.CategoryResponse
import com.typesafe.scalalogging.LazyLogging
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.{AnyEndpoint, Endpoint, EndpointInput, statusCode}
import sttp.tapir.generic.auto._
import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter

import java.time.LocalDateTime
import java.util.Locale
import scala.concurrent.{ExecutionContext, Future}

class CategoryController(baseController: BaseController, categoryDao: CategoryDaoImpl)(implicit ec: ExecutionContext)
  extends LazyLogging {

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

  val getCategoriesEndpoint: Endpoint[Unit, Unit, ErrorInfo, (List[CategoryResponse], StatusCode), Any] =
    baseController.baseEndpoint()
      .get
      .in("category")
      .summary("Returns a list of categories")
      .tag("Category")
      .out(
        jsonBody[List[CategoryResponse]]
          .description("List of categories")
      )
      .out(statusCode.description(StatusCode.Ok, "Successful getting categories"))

  val deleteCategoryEndpoint: Endpoint[Unit, CategoryPathArguments, ErrorInfo, StatusCode, Any] =
    baseController.baseEndpoint()
      .delete
      .in(EndpointInput.derived[CategoryPathArguments])
      .summary("Delete a specific category")
      .tag("Category")
      .out(statusCode.description(StatusCode.NoContent, "Successful deleted the expense"))

  val getCategoryByIdEndpoint: Endpoint[Unit, CategoryPathArguments, ErrorInfo, CategoryResponse, Any] =
    baseController.baseEndpoint()
      .get
      .in(EndpointInput.derived[CategoryPathArguments])
      .summary("Info for a specific category")
      .tag("Category")
      .out(
        jsonBody[CategoryResponse]
      )

  val createCategoryRoute =
    AkkaHttpServerInterpreter().toRoute(createCategoryEndpoint.serverLogic(createCategoryLogic))

  def createCategoryLogic(categoryRequest: AddCategoryRequest): Future[Either[ErrorInfo, (CategoryResponse, StatusCode)]] =
    Future {
    logger.info("Creating category: {}", categoryRequest.name)
    val category = categoryDao.saveCategory(categoryRequest.name, categoryRequest.description, categoryRequest.parentId)
    val categoryResponse = CategoryResponse(category.id, category.name,
                                            category.description, category.createdAt,
                                            category.parentId, category.isActive, None)
    Right(categoryResponse -> StatusCode.Created)
  }

  val getCategoriesRoute =
    AkkaHttpServerInterpreter().toRoute(getCategoriesEndpoint.serverLogic( _ => {
      logger.info("Getting categories")
      val categories =  categoryDao.getCategories
      if(categories.isEmpty) {
        logger.info("List of categories is empty")
        Future.successful[Either[ErrorInfo, (List[CategoryResponse], StatusCode)]](Left(NoContent))
      } else {
        val response: List[CategoryResponse] = categories.map( c => {
          var categoriesResponse = CategoryResponse(c.id, c.name, c.description, c.createdAt, c.parentId, c.isActive, None)

          if(c.parentId.isEmpty && c.subcategories.get.nonEmpty) {
            //TODO Refactor
            val subcategories = c.subcategories.get
            val categoryArray = subcategories.split(",")
            var list: List[CategoryResponse] = List()
            for(myString <- categoryArray) {
              val a = myString.split('|')
              import java.time.format.DateTimeFormatter
              val DATE_TIME_FORMATTER =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSS", Locale.US)
              // DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.[SSSSSS][SSS]");
              logger.info("a(3)= {}", a(3))
              val p = CategoryResponse(a(0).toLong, a(1), Some(a(2)), LocalDateTime.parse(a(3), DATE_TIME_FORMATTER), Some(a(4).toLong), a(5).toBoolean, None)
              list = list :+ p
            }
            categoriesResponse = CategoryResponse(c.id, c.name, c.description, c.createdAt, c.parentId, c.isActive, Some(list))
          }
          categoriesResponse
        })
         Future.successful(Right(response -> StatusCode.Ok))
      }
    }
    ))

  val deleteCategoryRoute =
    AkkaHttpServerInterpreter().toRoute(deleteCategoryEndpoint.serverLogic(deleteCategoryLogic))

  def deleteCategoryLogic(pathArguments: CategoryPathArguments): Future[Either[ErrorInfo, StatusCode]] = {
    logger.info("Deleting category with id: {}", pathArguments.categoryId)
      val categoryIdRequest = pathArguments.categoryId
      val category = categoryDao.getCategoryById(categoryIdRequest)
      if(category.isEmpty) {
        logger.info("Category with id: {} no found", categoryIdRequest)
        Future.successful[Either[ErrorInfo, StatusCode]](Left(NotFound(s"Category with id: $categoryIdRequest not found", StatusCode.NotFound.code)))
      } else {
        // TODO if parent_is null verify if it has active child
        logger.info("Deleting...")
        val categoryId = categoryDao.deleteCategory(pathArguments.categoryId)
        logger.info("category with id: {} deleted successfully", categoryId)
        Future.successful(Right(StatusCode.NoContent))
      }
    }

  def getCategoryByIdRoute: Route =
    AkkaHttpServerInterpreter().toRoute(getCategoryByIdEndpoint.serverLogic(getCategoryByIdLogic))

  def getCategoryByIdLogic(pathArguments: CategoryPathArguments): Future[Either[ErrorInfo, CategoryResponse]] = {
    val categoryIdRequest = pathArguments.categoryId
    logger.info("Finding category with id: {}", categoryIdRequest)
    val category = categoryDao.getCategoryById(categoryIdRequest)
    if(category.isEmpty) {
      Future.successful[Either[ErrorInfo, CategoryResponse]](Left(NotFound(s"Category with id: $categoryIdRequest not found", StatusCode.NotFound.code)))
    } else {
      val categoryVal = category.get
      Future.successful(Right(CategoryResponse(categoryVal.id, categoryVal.name, categoryVal.description, categoryVal.createdAt, categoryVal.parentId, categoryVal.isActive, None)))
    }
  }

  val categoryEndpoints: List[AnyEndpoint] = List(createCategoryEndpoint, getCategoriesEndpoint,
                                                  deleteCategoryEndpoint, getCategoryByIdEndpoint)

  val categoryRoutes: List[Route] = List(createCategoryRoute, getCategoriesRoute,
                                         deleteCategoryRoute, getCategoryByIdRoute)
}
