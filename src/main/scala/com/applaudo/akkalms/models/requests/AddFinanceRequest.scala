package com.applaudo.akkalms.models.requests

import com.applaudo.akkalms.enums.Months

case class AddFinanceRequest(year: Int,
                             month: Months.Month,
                             incomes: List[AddIncomeRequest])

