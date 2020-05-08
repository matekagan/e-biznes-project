package repositories

import javax.inject.{Inject, Singleton}
import models.Advertisement
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AdvertisementRepository @Inject()(val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig=dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class AdvertisementTable(tag: Tag) extends Table[Advertisement](tag, "advertisements") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def text = column[String]("text")

    def link = column[String]("link")

    def * = (id, text, link) <> ((Advertisement.apply _).tupled, Advertisement.unapply)
  }

  val advertisements = TableQuery[AdvertisementTable]

  def create(text: String, link: String): Future[Advertisement] = db.run {
    (advertisements.map(c => (c.text, c.link))
      returning advertisements.map(_.id)
      into { case ((text, link), id) => Advertisement(id, text, link)}
      ) += (text, link)
  }

  def list(): Future[Seq[Advertisement]] = db.run(advertisements.result)


  def getByIDOption(id: Int): Future[Option[Advertisement]] = db.run {
    advertisements.filter(_.id === id).result.headOption
  }

  def delete(id: Int): Future[Unit] = db.run(advertisements.filter(_.id === id).delete).map(_ => ())

  def update(id: Int, newAd: Advertisement): Future[Unit] = {
    val adToUpdate = newAd.copy(id)
    db.run(advertisements.filter(_.id === id).update(adToUpdate)).map(_ => ())
  }

}
