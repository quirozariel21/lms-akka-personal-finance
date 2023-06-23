package com.applaudo.akkalms.models.forms

import com.applaudo.akkalms.enums.Months
import sttp.tapir.EndpointIO.annotations.{description, example, query}


case class GetFinancesEndpointArguments(
                                       @query
                                       @description("Filter to search a personal finance by year")
                                       @example(Some(2021))
                                       year: Option[Int],

                                       @query
                                       @description("Filter to search a personal finance by month")
                                       @example(Some(Months.JUNE))
                                       month: Option[Months.Month]
                                       )
