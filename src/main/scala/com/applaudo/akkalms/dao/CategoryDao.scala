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
         SELECT c1.id, c1.name, c1.description,
                c1.created_at, c1.subcategory_id, c1.is_active, ARRAY_TO_STRING(
                ARRAY_AGG (
					                  c2.id || '|' || c2.name || '|' || c1.description || '|' || c1.created_at || '|' || c2.subcategory_id || '|' ||c1.is_active
					                  ORDER BY c2.name
                ), ',' ) AS subcategories
         FROM category c1
         LEFT JOIN category c2 ON c1.id = c2.subcategory_id
         WHERE c1.is_active=true AND c1.subcategory_id IS NULL AND c2.is_active=true
         GROUP BY c1.id
         ORDER BY c1.name
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
      .update.withUniqueGeneratedKeys[Category]("id", "name", "description", "created_at", "subcategory_id", "is_active", "subcategory_id")
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
