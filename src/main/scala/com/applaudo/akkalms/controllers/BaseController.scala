package com.applaudo.akkalms.controllers

import com.applaudo.akkalms.models.errors.{BadRequest, ErrorInfo, InternalServerError, NoContent, NotFound}
import sttp.model.StatusCode
import sttp.tapir.generic.auto._
import sttp.tapir.{auth, endpoint}
import sttp.tapir._
import io.circe.generic.auto._
import sttp.tapir.json.circe.jsonBody

import scala.concurrent.{ExecutionContext}


class BaseController()(implicit ec: ExecutionContext) {

  def baseEndpoint(): Endpoint[Unit, Unit, ErrorInfo, Unit, Any] =
    endpoint.in("api" / "v1") // base endpoint
      .errorOut(
        oneOf[ErrorInfo](
          oneOfVariant(StatusCode.NoContent, emptyOutputAs(NoContent).description("No Content")),
          oneOfVariant(statusCode(StatusCode.BadRequest).and(jsonBody[BadRequest].description("Bad Request"))),
          oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[NotFound].description("Not Found"))),
          oneOfVariant(statusCode(StatusCode.InternalServerError).and(jsonBody[InternalServerError].description("Internal Server Error")))
        ))
}
