package dal

import javax.inject.Inject

import models.{Config, Hotel}
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by style on 21/03/17.
  */
class ConfigRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import driver.api._

  private class ConfigTable(tag: Tag) extends Table[Config](tag, "config") {

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def token = column[String]("token")

    def apiLimit = column[Int]("apiLimit")

    def * = (id, token, apiLimit) <> ((Config.apply _).tupled, Config.unapply)
  }

  private val config = TableQuery[ConfigTable]

  def getConfigByToken(t: String): Future[Seq[Config]] = db.run {
    config.filter(_.token === t).result
  }

}
