package com.applaudo.akkalms.entities

import java.time.{LocalDate, LocalDateTime}

case class Expense(id: Long,
                   categoryId: Long,
                   subcategoryId: Long,
                   note: Option[String],
                   amount: BigDecimal,
                   currency: String,
                   expenseDate: LocalDate,
                   personalFinanceId: Long)
