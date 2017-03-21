package filters

import java.util.concurrent.TimeoutException
import javax.inject._

import akka.stream.Materializer
import play.api.Logger
import play.api.cache._
import play.api.mvc._
import services.ConfigService

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
  * This is a simple filter that intercepts all incoming request and verifies token in headers. It's
  * added to the application's list of filters by the
  * [[Filters]] class.
  *
  * @param mat This object is needed to handle streaming of requests
  * and responses.
  * @param exec This class is needed to execute code asynchronously.
  * It is used below by the `map` method.
  */
@Singleton
class ApiRateLimitFilter @Inject()(cs: ConfigService,
                                   implicit override val mat: Materializer,
                                   exec: ExecutionContext, cache: CacheApi) extends Filter {

  // Per Defined constants
  val globalApiRateLimitCounter:Int = 10
  val globalThresholdApiTimeLimit:Long = 10000 // In milliseconds i.e 10 Seconds

  override def apply(nextFilter: RequestHeader => Future[Result])
                    (requestHeader: RequestHeader): Future[Result] = {
    Logger.debug("-------- Inside api rate limit filter method ------------")
    Logger.debug("")


    val t:Option[String] = requestHeader.headers.get("token")

    if(requestHeader.headers.get("token").isEmpty && !requestHeader.headers.get("token").isDefined) {
      Logger.debug("Token not found in headers!!! ")
      Future.failed(throw new Exception("Token not found in headers!"))
    } else {
      Logger.debug("Token found in headers")

      val token:String = t.get

      var al:Int = -1
      if (cache.get(s"config:$token").isEmpty) cs.getConfigByToken(token) else al = cache.get[Int](s"config:$token").get

      val apiLimit:Int = if(al != -1) al else globalApiRateLimitCounter

      Logger.debug(s"apiLimit: $apiLimit")

      var count:Int = 1
      val cacheVal:Option[String] = cache.get[String](token)

      val currTime:Long = System.currentTimeMillis()
      var cacheValStr:String = s"$count:true:$currTime"

      if(!cacheVal.isEmpty && cacheVal.isDefined) {

        val splitedList = cacheVal.get.split(":")
        count += splitedList(0).toInt

        val isActive:Boolean = splitedList(1).toBoolean
        val timeOfFirstApiAccess:Long = splitedList(2).toLong

        val timeDiff:Long = currTime - timeOfFirstApiAccess

        Logger.debug(s"TimeDiff: $timeDiff")
        /***
          *  Idea is to store the token in cache with default infinite expiry time. Attributes are count, boolean active or inactive
          *  & time in milliseconds since first api call
          *
          *  Logic:
          *
          *  If someone visiting for the first time store the token with default parameters with infinite expiry time. We can optimise
          *  it further if somehow we have means to update the existing cache key. --> Add it to TODO
          *
          *  If count && timeDifference of current_time & timeOfFirstApiAccess is less than their respective threshold limit then
          *  just increment api counter & set the key with new value
          *
          *  If count has still less than threshold && timeDifference is greater than the threshold limit then it is clearly a low api usage
          *  case. Hence remove the existing key & set the new key with new time.
          *
          *  In all other case just set the expiry of key to the timeDifference or as per the requirement i.e 5Min, Hence the key will be automatically removed & user
          *  can access thereafter.
          */

        if(isActive) {
          if (count <= apiLimit && timeDiff <= globalThresholdApiTimeLimit ) {
            cacheValStr = s"$count:true:$timeOfFirstApiAccess"
            cache.set(token, cacheValStr)
          } else if (timeDiff > globalThresholdApiTimeLimit && count <= apiLimit) {
            Logger.debug("Removing cache key")
            cache.remove(token)
            cache.set(token, cacheValStr)
          } else {
            Logger.debug("Suspending api for the next 5 minutes")
            cacheValStr = s"$count:false:$timeOfFirstApiAccess"
            cache.set(token, cacheValStr, Duration(5, MINUTES))
          }
        } else {

          // TODO Add a generic handler to handle exceptions & show user proper response
          Logger.debug("Api rate limit reached!!!")
          Future.failed(throw new TimeoutException("Api rate limit reached"))
        }

      } else {
        cache.set(token, cacheValStr)
      }

      Logger.debug(token + ":" + cache.get(token).toString)
      Logger.debug("-------- Inside api rate limit filter method ------------")
      Logger.debug("")
    }
    nextFilter(requestHeader)
  }

}
