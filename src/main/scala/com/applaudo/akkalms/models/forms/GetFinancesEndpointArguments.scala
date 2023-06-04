package com.applaudo.akkalms.models.forms

import sttp.tapir.EndpointIO.annotations.{description, query, example}



case class GetFinancesEndpointArguments(
                                       @query
                                       @description("Filter to search a personal finance by year")
                                       //@example("2021")
                                       year: Option[Int],

                                       @query
                                       @description("Filter to search a personal finance by month")
                                       //@example("JANUARY")
                                       month: Option[String]
                                       )
