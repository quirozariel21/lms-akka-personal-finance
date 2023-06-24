package com.applaudo.akkalms.enums

object IncomeTypes extends Enumeration {
  type IncomeType = Value

  val SALARY: IncomeType = Value("Salary")
  val EXTRA: IncomeType = Value("Extra")
  val RENT: IncomeType = Value("Rent")
}
