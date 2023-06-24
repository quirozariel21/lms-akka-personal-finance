package com.applaudo.akkalms.dao

import cats.effect.IO
import com.applaudo.akkalms.config.DoobieConfig
import com.applaudo.akkalms.entities.{Finance, GenerateSummaryEntity}
import com.applaudo.akkalms.models.requests.AddFinanceRequest
import doobie.implicits.toSqlInterpolator
import doobie.implicits._
import cats.effect._
import com.applaudo.akkalms.enums.Months
import doobie.util.fragments.whereAndOpt
import doobie.util.transactor.Transactor.Aux

trait FinanceDao {

  val xa: Aux[IO, Unit] = DoobieConfig.xa

  def save(finance: AddFinanceRequest): Finance
  def findById(personalFinanceId: Long): Option[Finance]
  def findByYearAndMonth(year: Option[Int], month: Option[Months.Month]): List[Finance]
  def generateSummary(personalFinanceId: Long): List[GenerateSummaryEntity]
}

class FinanceDaoImpl extends FinanceDao {

  import cats.effect.unsafe.implicits.global
  import doobie.postgres._
  import doobie.postgres.implicits._

  import doobie.implicits._
  import doobie.implicits.javasql._
  import doobie.postgres.implicits._
  import doobie._

  implicit val writer: Write[AddFinanceRequest] =
    Write[(Int, String, String, Boolean)]
      .contramap(c => (c.year, c.month.toString, "admin", true))

  override def save(financeRequest: AddFinanceRequest): Finance = {
    sql"""
         INSERT INTO personal_finance (year, month, created_by, is_active)
         VALUES ($financeRequest)
       """
      .update.withUniqueGeneratedKeys[Finance]("id", "year", "month")
      .transact(xa)
      .unsafeRunSync()
  }

  override def findById(personalFinanceId: Long): Option[Finance] = {

    val statement =
      sql"""
         SELECT p.id, p.year, p.month,
         ARRAY_TO_STRING (
            ARRAY_AGG (
              i.id || '|' || i.name || '|' || i.currency || '|' || i.amount || '|' || CASE WHEN i.note IS NULL THEN ' ' END || '|'
              ORDER BY i.id
            )
          , '#' ) AS incomes, sum(i.amount) AS totalReceived
         FROM personal_finance p
         LEFT JOIN income i ON i.personal_finance_id = p.id
         WHERE  p.is_active = true AND i.is_active = true AND p.id = $personalFinanceId
         GROUP BY p.id
       """
       statement.query[Finance]
         .option
         .transact(xa)
         .unsafeRunSync()
  }

  override def findByYearAndMonth(year: Option[Int], month: Option[Months.Month]): List[Finance] = {
    //implicit val han = LogHandler.jdkLogHandler
    val isActive = Some(true)
    val f1 = year.map(y => fr"p.year = $y ")
    val f2 = month.map(m => fr"p.month = ${m.toString} ")
    val f3 = isActive.map(i => fr"p.is_active = $i AND i.is_active = $i")
    val q: Fragment =
      fr"""
          SELECT p.id, p.year, p.month,
          ARRAY_TO_STRING (
            ARRAY_AGG (
              i.id || '|' || i.name || '|' || i.currency || '|' || i.amount || '|' || CASE WHEN i.note IS NULL THEN ' ' END || '|'
              ORDER BY i.id
            )
          , '#' ) AS incomes
          FROM personal_finance p
          LEFT JOIN income i ON i.personal_finance_id = p.id
        """                           ++
        whereAndOpt(f1, f2, f3)       ++
        fr"""
            GROUP BY p.id
            ORDER BY p.year DESC
          """
    q.query[Finance]
          .to[List]
           .transact(xa)
           .unsafeRunSync()
  }

  override def generateSummary(personalFinanceId: Long): List[GenerateSummaryEntity] = {
    val statement =
      sql"""
           SELECT p.id, p.year, p.month, e.id expense_id, e.category_id, cat.name AS category_name,
                  e.subcategory_id, cat2.name AS subcategory_name, e.amount,
                  e.currency, e.note, e.expensed_date
           FROM personal_finance p
           LEFT JOIN expense e ON e.personal_finance_id = p.id
           LEFT JOIN category cat ON e.category_id = cat.id
           LEFT JOIN category cat2 ON e.subcategory_id = cat2.id
           WHERE p.is_active = true AND e.is_active= true AND cat.is_active = true AND cat2.is_active = true AND p.id = $personalFinanceId
           ORDER BY cat.name, cat2.name
         """
         statement.query[GenerateSummaryEntity]
           .to[List]
           .transact(xa)
           .unsafeRunSync()
  }
}
