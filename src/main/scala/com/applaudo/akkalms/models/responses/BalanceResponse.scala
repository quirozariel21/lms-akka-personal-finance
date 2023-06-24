package com.applaudo.akkalms.models.responses

case class BalanceResponse(
                          totalReceived: BigDecimal,
                          totalSpent: BigDecimal,
                          totalSaved: BigDecimal
                          )
