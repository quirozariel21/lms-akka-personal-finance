package com.applaudo.akkalms.models.responses

import java.time.LocalDate

case class SubcategoryResponse(id: Long,
                               name: String,
                               note: Option[String],
                               amount: BigDecimal,
                               currency: String,
                               expenseDate: LocalDate)
