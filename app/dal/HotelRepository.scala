package dal

import javax.inject.Inject

import models.Hotel
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by style on 19/03/17.
  */
class HotelRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import driver.api._

  private class HotelTable(tag: Tag) extends Table[Hotel](tag, "hotel") {

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def city = column[String]("city")

    def room = column[String]("room")

    def price = column[Int]("price")

    def * = (id, city, room, price) <> ((Hotel.apply _).tupled, Hotel.unapply)
  }

  private val hotel = TableQuery[HotelTable]

  def listHotelOrderByPriceDESC(): Future[Seq[Hotel]] = db.run {
    hotel.sortBy(h => h.price.desc).result
  }

  def listHotelOrderByPriceASC(): Future[Seq[Hotel]] = db.run {
      hotel.sortBy(h => h.price.asc).result
  }

  def getHotelsByCityOrderByPriceASC(cityName: String): Future[Seq[Hotel]] = db.run {
      hotel.filter(_.city === cityName).sortBy(h => h.price.asc).result
  }

  def getHotelsByCityOrderByPriceDESC(cityName: String): Future[Seq[Hotel]] = db.run {
    hotel.filter(_.city === cityName).sortBy(h => h.price.desc).result
  }
}
