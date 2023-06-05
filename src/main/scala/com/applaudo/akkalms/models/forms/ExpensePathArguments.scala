package com.applaudo.akkalms.models.forms

import sttp.tapir.EndpointIO.annotations.{endpointInput, path}
import sttp.tapir.Schema.annotations.description

@endpointInput("finance/{financeId}/expense/{expenseId}")
case class ExpensePathArguments(
                               @path
                               @description("ID of the finance that needs to be updated")
                               financeId: Long,

                               @path
                               @description("ID of the expense that needs to be updated")
                               expenseId: Long
                               )
