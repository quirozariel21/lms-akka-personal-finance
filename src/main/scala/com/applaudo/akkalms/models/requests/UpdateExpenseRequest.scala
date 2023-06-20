package com.applaudo.akkalms.models.requests

import java.time.{LocalDate}

case class UpdateExpenseRequest(id: Int,
                                categoryId: Int,
                                subcategoryId: Int,
                                note: Option[String],
                                amount: BigDecimal,
                                currency: String,
                                expenseDate: LocalDate)
