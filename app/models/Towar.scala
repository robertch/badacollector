package models

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.dbio
import slick.lifted.Rep
import slick.dbio.Effect.Read
import slick.driver.JdbcProfile
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class Towar( name:String, link:String, t_kat_id:Long, id:Long)
case class Cena( c_id:Long, c_t_id:Long, c_cena:Double, c_jednostka:String, c_ilosc:Double)

class TowarRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.driver.api._
  private val Towary = TableQuery[TowaryTable]
  private val Kategorie = TableQuery[KategorieTable]

  private def _findById(id: Long): DBIO[Option[Towar]] =
    Towary.filter(_.id === id).result.headOption

  private def _findByName(name: String): Query[TowaryTable, Towar, List] =
    Towary.filter(_.name === name).to[List]

  def findById(id: Long): Future[Option[Towar]] =
    db.run(_findById(id))

  def findByName(name: String): Future[List[Towar]] =
    db.run(_findByName(name).result)

  def count(filter: String): Future[Int] = {
    db.run(Towary.filter { towary => towary.name.toLowerCase like filter.toLowerCase }.length.result)
  }

  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Future[Page[(Towar, Kategoria)]] = {

      val offset = pageSize * page
      val query = (for{
          (towar, kategoria) <- Towary join Kategorie on (_.t_kat_id === _.id)
          if towar.name.toLowerCase like filter.toLowerCase
      } yield (towar, kategoria.id,kategoria.name))
        .drop(offset)
        .take(pageSize)

      for{
        totalRows <- count(filter)
        list = query.result.map{ rows => rows.collect{case (towar,id,name) => (towar,Kategoria(id,name))}}
        result <- db.run(list)
      } yield Page(result,page,offset,totalRows)
  }

  def create(name: String,link:String,kategoria: Kategoria): Future[Long] = {
    val towar = Towar(name,link,kategoria.id,0)
    db.run(Towary returning Towary.map(_.id) += towar)
  }

  class TowaryTable(tag: Tag) extends Table[Towar](tag, "towary") {

    def id = column[Long]("t_id", O.AutoInc, O.PrimaryKey)
    def name = column[String]("t_nazwa")
    def link = column[String]("t_link")
    def t_kat_id = column[Long]("t_kat_id")
    def kat_id = foreignKey("KATID_FK",t_kat_id,TableQuery[KategorieTable])(_.id)

    def * = (name,link,t_kat_id,id) <> (Towar.tupled, Towar.unapply _)
    def ? = (name.?,link.?,t_kat_id.?,id.?).shaped.<>({ r => import r._; _1.map(_ => Towar.tupled((_1.get, _2.get, _3.get, _4.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

  }
}
/*
class CenaRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.driver.api._
  private val Ceny = TableQuery[CenyTable]
  private val Towary = TableQuery[TowaryTable]

  private def _findById(id: Long): DBIO[Option[Cena]] =
    Ceny.filter(_.c_id === id).result.headOption

  private def _findByPrice(cena: Double): Query[CenyTable, Cena, List] =
    Ceny.filter(_.c_cena === cena).to[List]

  def findById(id: Long): Future[Option[Cena]] =
    db.run(_findById(id))

  def count(cena: Double): Future[Int] = {
    db.run(Ceny.filter { ceny => ceny.c_cena === filter }.length.result)
  }

  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, cena: Double ): Future[Page[(Cena, Towar)]] = {

      val offset = pageSize * page
      val query = (for{
          (cena, towar) <- Ceny join Towary on (_.c_t_id === _.id)
          if towar.cena == cena
      } yield (cena, towar.id,towar.name,towar.link))
        .drop(offset)
        .take(pageSize)

      for{
        totalRows <- count(filter)
        list = query.result.map{ rows => rows.collect{case (cena,id,name,link) => (cena,Towar(id,link,name))}}
        result <- db.run(list)
      } yield Page(result,page,offset,totalRows)
  }

  def create(cena: Double,jednostka:String,ilosc:Double,towar: Towar): Future[Long] = {
    val cena = Cena(0,towar.id,cena,jednostka,ilosc)
    db.run(Ceny returning Ceny.map(_.id) += cena)
  }

  class CenyTable(tag: Tag) extends Table[Cena](tag,"ceny"){
    def c_id = column[Long]("c_id", O.AutoInc, O.PrimaryKey)
    def c_t_id = column[Long]("c_t_id")
    def c_cena = column[Double]("c_cena")
    def c_jednostka = column[String]("c_jednostka")
    def c_ilosc = column[Double]("c_ilosc")
    def t_id  = foreignKey("TIDFK",u_g_id,TableQuery[TowaryTable])(_.id)
    def * = (c_id,c_t_id,c_cena,c_jednostka,c_ilosc) <> (Cena.tupled, Cena.unapply _)
  }
}
*/
