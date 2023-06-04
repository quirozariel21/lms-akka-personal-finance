package com.applaudo.akkalms.models.responses

case class FinanceResponse(id: Int,
                           year: Int,
                           month: String,
                           incomes: List[IncomeResponse])
