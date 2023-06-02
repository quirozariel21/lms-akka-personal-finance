package com.applaudo.akkalms.models.requests

case class AddFinanceRequest(year: Int,
                             month: String,
                             incomes: List[AddIncomeRequest])
