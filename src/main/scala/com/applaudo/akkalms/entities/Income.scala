package com.applaudo.akkalms.entities

case class Income(id: Long,
                  name: String,
                  amount: BigDecimal,
                  currency: String,
                  note: Option[String],
                  personalFinanceId: Long)
