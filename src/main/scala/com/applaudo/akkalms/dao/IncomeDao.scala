package com.applaudo.akkalms.dao

import cats.effect.IO
import com.applaudo.akkalms.config.PostgresDBConfig
import com.applaudo.akkalms.models.requests.{AddFinanceRequest, AddIncomeRequest}
import doobie.implicits.toSqlInterpolator
import doobie.implicits._
import cats.effect._
import doobie.util.transactor.Transactor.Aux

trait IncomeDao {

  val xa: Aux[IO, Unit] = PostgresDBConfig.xa

  def saveIncomes(financeId: Long, incomesRequest: AddIncomeRequest): Int

}


class IncomeDaoImpl extends IncomeDao {

  import cats.effect.unsafe.implicits.global
  import doobie.postgres._
  import doobie.postgres.implicits._

  import doobie.implicits._
  import doobie.implicits.javasql._
  import doobie.postgres.implicits._
  import doobie._

  implicit val writer: Write[AddIncomeRequest] =
    Write[(String, String, Option[String], String, BigDecimal, Boolean)]
      .contramap(c => (c.incomeType.toString, c.currency.toString, c.note, "admin", c.amount, true))

  override def saveIncomes(financeId: Long, incomesRequest: AddIncomeRequest): Int = {
    sql"""
         INSERT INTO income(name, currency, note, created_by, amount, is_active, personal_finance_id)
         VALUES ($incomesRequest, $financeId)
       """
      .update.withUniqueGeneratedKeys[Int]("id")
      .transact(xa)
      .unsafeRunSync()
  }
}
