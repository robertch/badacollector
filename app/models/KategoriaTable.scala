package models

import slick.driver.PostgresDriver.api._

class KategorieTable(tag: Tag) extends Table[Kategoria](tag, "kategorie") {

  def id = column[Long]("kat_id", O.AutoInc, O.PrimaryKey)
  def name = column[String]("kat_nazwa")

  def * = (id, name) <> (Kategoria.tupled, Kategoria.unapply)
  def ? = (id.?, name.?).shaped.<>({ r => import r._; _1.map(_ => Kategoria.tupled((_1.get, _2.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

}
