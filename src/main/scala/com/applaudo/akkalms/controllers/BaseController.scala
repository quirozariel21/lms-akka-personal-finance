package com.applaudo.akkalms.controllers

import sttp.tapir._


class BaseController {

  def baseEndpoint(): Endpoint[Unit, Unit, Unit, Unit, Any] =
    endpoint.in("api" / "v1")
}
