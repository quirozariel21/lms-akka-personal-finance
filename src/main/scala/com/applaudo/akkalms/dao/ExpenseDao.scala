package com.applaudo.akkalms.dao

import cats.effect.IO
import com.applaudo.akkalms.config.DoobieConfig
import com.applaudo.akkalms.models.requests.AddExpenseRequest
import doobie.implicits.toSqlInterpolator
import doobie.implicits._
import com.applaudo.akkalms.entities.Expense
import doobie.Write
import doobie.util.transactor.Transactor.Aux
import cats.effect.unsafe.implicits.global
import doobie.postgres._
import doobie.postgres.implicits._

import java.time.LocalDate

trait ExpenseDao {
  val xa: Aux[IO, Unit] = DoobieConfig.xa

  def save(expenseRequest: AddExpenseRequest, financeId: Long): Expense
  def delete(personalFinanceId: Long, expenseId: Long): Int
  def findById(expenseId: Long): Option[Expense]
  def findByPersonalFinanceId(personalFinanceId: Long): List[Expense]
  def sumTotalAmount(personalFinanceId: Long): (BigDecimal, String)
}

class ExpenseDaoImpl extends ExpenseDao {

  implicit val writer: Write[AddExpenseRequest] =
    Write[(Long, Long, Option[String], BigDecimal, String, LocalDate, String, Boolean)]
      .contramap(c => (c.categoryId, c.subcategoryId, c.note, c.amount, c.currency.toString, c.expenseDate, "admin", true))

  override def save(expenseRequest: AddExpenseRequest, personalFinanceId: Long): Expense = {
    sql"""
         INSERT INTO expense(category_id, subcategory_id, note, amount, currency, expensed_date, created_by, is_active, personal_finance_id)
         VALUES ($expenseRequest, $personalFinanceId)
         """.update.withUniqueGeneratedKeys[Expense]("id", "category_id", "subcategory_id", "note", "amount", "currency", "expensed_date", "personal_finance_id")
      .transact(xa)
      .unsafeRunSync()
  }

  override def delete(personalFinanceId: Long, expenseId: Long): Int = {
    sql"""
         UPDATE expense SET is_active = false
         WHERE id = $expenseId AND personal_finance_id = $personalFinanceId
         """.update
      .run
      .transact(xa)
      .unsafeRunSync()
  }

  override def findById(expenseId: Long): Option[Expense] = {
    val statement =
      sql"""
           SELECT id, category_id, subcategory_id, note, amount, currency, expensed_date, personal_finance_id
           FROM expense
           WHERE is_active = true AND id = $expenseId
         """
         statement.query[Expense]
           .option
           .transact(xa)
           .unsafeRunSync()
  }

  override def findByPersonalFinanceId(personalFinanceId: Long): List[Expense] = {
    val statement =
      sql"""
           SELECT id, category_id, subcategory_id, note, amount, currency, expensed_date, personal_finance_id
           FROM expense
           WHERE is_active = true AND personal_finance_id = $personalFinanceId
           ORDER BY expensed_date desc
         """
       statement.query[Expense]
      .to[List]
      .transact(xa)
      .unsafeRunSync()
  }

  override def sumTotalAmount(personalFinanceId: Long): (BigDecimal, String) = {
    val statement =
      sql"""
           SELECT SUM(amount), currency
           FROM expense
           WHERE personal_finance_id = $personalFinanceId AND is_active = true
           GROUP BY personal_finance_id, currency;
         """
         statement.query[(BigDecimal, String)]
           .unique
           .transact(xa)
           .unsafeRunSync()
  }
}
