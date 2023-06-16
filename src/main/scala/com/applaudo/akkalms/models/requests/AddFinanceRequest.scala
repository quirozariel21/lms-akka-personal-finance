package com.applaudo.akkalms.models.requests

import io.circe._
import io.circe.generic.auto._
import sttp.tapir._
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._

object Months extends Enumeration {
  type Month = Value

  val JANUARY: Month = Value("January")
  val FEBRUARY: Month = Value("February")
  val MARCH: Month = Value("March")
  val APRIL: Month = Value("April")
  val MAY: Month = Value("May")
  val JUNE: Month = Value("June")
  val JULY: Month = Value("July")
  val AUGUST: Month = Value("August")
  val SEPTEMBER: Month = Value("September")
  val OCTOBER: Month = Value("October")
  val NOVEMBER: Month = Value("November")
  val DECEMBER: Month = Value("December")
}

case class AddFinanceRequest(year: Int,
                             month: Months.Month,
                             incomes: List[AddIncomeRequest])

/*// these need to be provided so that circe knows how to encode/decode enumerations
implicit val enumDecoder: Decoder[Months.Month] = Decoder.decodeEnumeration(Months)
implicit val enumEncoder: Encoder[Months.Month] = Encoder.encodeEnumeration(Months)

  // the schema for the body is automatically-derived, using the default schema for
  // enumerations (Schema.derivedEnumerationValue)
  jsonBody[Body]*/
