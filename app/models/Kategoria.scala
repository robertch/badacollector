package models

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.dbio
import slick.dbio.Effect.Read
import slick.driver.JdbcProfile
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class Kategoria(id: Long, name: String)

class KategoriaRepo @Inject()()(protected val dbConfigProvider: DatabaseConfigProvider) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.driver.api._
  private val Kategorie = TableQuery[KategorieTable]

  private def _findById(id: Long): DBIO[Option[Kategoria]] =
    Kategorie.filter(_.id === id).result.headOption

  private def _findByName(name: String): Query[KategorieTable, Kategoria, List] =
    Kategorie.filter(_.name === name).to[List]

  def findById(id: Long): Future[Option[Kategoria]] =
    db.run(_findById(id))

  def findByName(name: String): Future[List[Kategoria]] =
    db.run(_findByName(name).result)

  def all: Future[List[Kategoria]] =
    db.run(Kategorie.to[List].result)

  def create(name: String): Future[Long] = {
    val kategoria = Kategoria(0, name)
    db.run(Kategorie returning Kategorie.map(_.id) += kategoria)
  }
}
