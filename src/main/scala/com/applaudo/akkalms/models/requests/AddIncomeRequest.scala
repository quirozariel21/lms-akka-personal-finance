package com.applaudo.akkalms.models.requests

import com.applaudo.akkalms.enums.{Currencies, IncomeTypes}

case class AddIncomeRequest(incomeType: IncomeTypes.IncomeType,
                            amount: BigDecimal,
                            currency: Currencies.Currency,
                            note: Option[String])

