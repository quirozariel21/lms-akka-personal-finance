package com.applaudo.akkalms.dao

import cats.effect.IO
import com.applaudo.akkalms.config.PostgresDBConfig
import com.applaudo.akkalms.model_db.Finance
import com.applaudo.akkalms.models.requests.AddFinanceRequest
import doobie.implicits.toSqlInterpolator
import doobie.implicits._
import cats.effect._
import doobie.util.transactor.Transactor.Aux

trait FinanceDao {

  val xa: Aux[IO, Unit] = PostgresDBConfig.xa

  def saveFinance(finance: AddFinanceRequest): Finance

  def getFinanceById(personalFinanceId: Long): Option[Finance]
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

  override def saveFinance(financeRequest: AddFinanceRequest): Finance = {
    sql"""
         INSERT INTO personal_finance (year, month, created_by, is_active)
         VALUES ($financeRequest)
       """
      .update.withUniqueGeneratedKeys[Finance]("id", "year", "month")
      .transact(xa)
      .unsafeRunSync()
  }

  override def getFinanceById(personalFinanceId: Long): Option[Finance] = {
    // TODO Get incomes
    val statement =
      sql"""
         SELECT id, year, month
         FROM personal_finance
         WHERE  is_active = true AND id = $personalFinanceId
       """
       statement.query[Finance]
         .option
         .transact(xa)
         .unsafeRunSync()
  }
}
