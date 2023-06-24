package com.applaudo.akkalms.models.forms

import sttp.tapir.EndpointIO.annotations.{endpointInput, path}
import sttp.tapir.Schema.annotations.description

@endpointInput("finance/{financeId}")
case class FinancePathArguments(@path
                                @description("ID of the finance that needs to be updated")
                                financeId: Long)
