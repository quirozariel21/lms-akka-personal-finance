package com.applaudo.akkalms.models.responses

case class GetIncomeResponse(totalAmountReceived: BigDecimal,
                             currency: String,
                             incomes: List[IncomeResponse])
