package com.applaudo.akkalms.dao

import cats.effect.IO
import com.applaudo.akkalms.config.DoobieConfig
import com.applaudo.akkalms.models.requests.{AddIncomeRequest, UpdateIncomeRequest}
import doobie._
import doobie.implicits.toSqlInterpolator
import doobie.implicits._
import doobie.postgres._
import doobie.postgres.implicits._
import com.applaudo.akkalms.entities.Income
import doobie.util.transactor.Transactor.Aux

trait IncomeDao {

  val xa: Aux[IO, Unit] = DoobieConfig.xa

  def save(financeId: Long, incomesRequest: AddIncomeRequest): Income
  def update(financeId: Long, incomesRequest: UpdateIncomeRequest): Income
  def delete(financeId: Long, incomeId: Long): Int
  def findById(id: Long): Option[Income]
  def findByPersonalFinanceId(id: Long): List[Income]
  def sumTotalAmount(personalFinanceId: Long): (BigDecimal, String)
}


class IncomeDaoImpl extends IncomeDao {

  import cats.effect.unsafe.implicits.global

  implicit val writer: Write[AddIncomeRequest] =
    Write[(String, String, Option[String], String, BigDecimal, Boolean)]
      .contramap(c => (c.incomeType.toString, c.currency.toString, c.note, "admin", c.amount, true))

  override def save(financeId: Long, incomesRequest: AddIncomeRequest): Income = {
    sql"""
         INSERT INTO income(name, currency, note, created_by, amount, is_active, personal_finance_id)
         VALUES ($incomesRequest, $financeId)
       """
      .update.withUniqueGeneratedKeys[Income]("id", "name", "amount", "currency", "note", "personal_finance_id")
      .transact(xa)
      .unsafeRunSync()
  }

  override def findById(id: Long): Option[Income] = {
    val statement =
      sql"""
           SELECT id, name,amount, currency, note, personal_finance_id
           FROM income
           WHERE is_active = true AND id = $id
         """
         statement.query[Income]
           .option
           .transact(xa)
           .unsafeRunSync()
  }

  override def sumTotalAmount(personalFinanceId: Long): (BigDecimal, String) = {
    val statement =
      sql"""
           SELECT SUM(amount), currency
           FROM income
           WHERE personal_finance_id = $personalFinanceId AND is_active = true
           GROUP BY personal_finance_id, currency;
         """
    statement.query[(BigDecimal, String)]
      .unique
      .transact(xa)
      .unsafeRunSync()
  }

  override def update(personalFinanceId: Long, incomesRequest: UpdateIncomeRequest): Income = {
    sql"""
         UPDATE income SET name = ${incomesRequest.incomeType.toString},
                          amount = ${incomesRequest.amount},
                          currency = ${incomesRequest.currency.toString},
                          note = ${incomesRequest.note}
         WHERE id = ${incomesRequest.id} AND
               personal_finance_id = $personalFinanceId AND
               is_active = true
       """
      .update.withUniqueGeneratedKeys[Income]("id", "name", "amount", "currency", "note", "personal_finance_id")
      .transact(xa)
      .unsafeRunSync()
  }

  override def delete(personalFinanceId: Long, incomeId: Long): Int = {
    sql"""
         UPDATE income SET is_active = false
         WHERE id = $incomeId AND
               personal_finance_id = $personalFinanceId AND
               is_active = true
       """
      .update
      .run
      .transact(xa)
      .unsafeRunSync()
  }

  override def findByPersonalFinanceId(id: Long): List[Income] = {
    val statement =
      sql"""
           SELECT id, name,amount, currency, note, personal_finance_id
           FROM income
           WHERE is_active = true AND personal_finance_id = $id
         """
    statement.query[Income]
      .to[List]
      .transact(xa)
      .unsafeRunSync()
  }
}
