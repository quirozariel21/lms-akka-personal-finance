package com.applaudo.akkalms.models.responses

case class GenerateSummaryResponse(
                                    id: Long,
                                    year: Int,
                                    month: String,
                                    balance: BalanceResponse,
                                    categories: List[CategorySummaryReportResponse]
                                  )
