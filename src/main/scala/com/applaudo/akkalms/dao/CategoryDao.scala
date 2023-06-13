package com.applaudo.akkalms.dao


import com.applaudo.akkalms.config.PostgresDBConfig
import com.applaudo.akkalms.model_db.Category

import doobie.implicits.toSqlInterpolator
import doobie.implicits._
import cats.effect._
import doobie.util.transactor.Transactor.Aux

trait CategoryDao {

  val xa: Aux[IO, Unit] = PostgresDBConfig.xa

  def getCategories(): List[Category]
  def getCategoryById(id: Long): Option[Category]
  def saveCategory(name: String, description: Option[String], subcategoryId: Option[Long]): Category
  def deleteCategory(id: Long): Int
}

class CategoryDaoImpl extends CategoryDao {
  import cats.effect.unsafe.implicits.global
  import doobie.postgres._
  import doobie.postgres.implicits._

  override def getCategories(): List[Category] = {
    val statement =
      sql"""
         SELECT id, name, description, created_at, subcategory_id, is_active
         FROM category
         WHERE is_active=true
         ORDER BY name
         """
      statement.query[Category]
      .to[List]
      .transact(xa)
      .unsafeRunSync()
  }

  override def getCategoryById(id: Long): Option[Category] = {
    val statement =
      sql"""
         SELECT id, name, description, created_at, subcategory_id, is_active
         FROM category
         WHERE id = $id AND is_active=true
       """
       statement.query[Category].option.transact(xa).unsafeRunSync()
  }

  override def saveCategory(name: String, description: Option[String], subcategoryId: Option[Long]): Category = {
    sql"INSERT INTO category (name, description, is_active, subcategory_id) values ($name, $description, true, $subcategoryId)"
      .update.withUniqueGeneratedKeys[Category]("id", "name", "description", "created_at", "subcategory_id", "is_active")
      .transact(xa)
      .unsafeRunSync()
  }

  /**
   * This is a logic delete
   * @param id
   * @return
   */
  override def deleteCategory(id: Long): Int = {
    sql"UPDATE category set is_active=false WHERE id=$id"
      .update
      .run
      .transact(xa)
      .unsafeRunSync()
  }
}
