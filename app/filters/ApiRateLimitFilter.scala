package filters

import java.util.concurrent.TimeoutException
import javax.inject._

import akka.stream.Materializer
import play.api.Logger
import play.api.cache._
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
  * This is a simple filter that adds a header to all requests. It's
  * added to the application's list of filters by the
  * [[Filters]] class.
  *
  * @param mat This object is needed to handle streaming of requests
  * and responses.
  * @param exec This class is needed to execute code asynchronously.
  * It is used below by the `map` method.
  */
@Singleton
class ApiRateLimitFilter @Inject()(
                                    implicit override val mat: Materializer,
                                    exec: ExecutionContext, cache: CacheApi) extends Filter {

  override def apply(nextFilter: RequestHeader => Future[Result])
                    (requestHeader: RequestHeader): Future[Result] = {
    // Run the next filter in the chain. This will call other filters
    // and eventually call the action. Take the result and modify it
    // by adding a new header.
    Logger.debug("-------- Inside api rate limit filter method ------------")
    Logger.debug("")

    val token:String = requestHeader.headers.get("token").toString

    if(requestHeader.headers.get("token").isEmpty && !requestHeader.headers.get("token").isDefined) {
      Logger.debug("Token not found in headers!!! ")
      Future.failed(throw new Exception("Token not found!"))
    } else {
      Logger.debug("Token found in headers")

      // Per Defined constants
      val thresholdApiLimit:Int = 10
      val thresholdApiTimeLimit:Long = 10000 // In milliseconds i.e 10 Seconds

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
          *  In all other case just set the expiry of key to the timeDifference, Hence the key will be automatically removed & user
          *  can access thereafter.
          */

        if(isActive) {
          if (count <= thresholdApiLimit && timeDiff <= thresholdApiTimeLimit ) {
            cacheValStr = s"$count:true:$timeOfFirstApiAccess"
            cache.set(token, cacheValStr)
          } else if (timeDiff > thresholdApiTimeLimit && count <= thresholdApiLimit) {
            Logger.debug("Removing cache key")
            cache.remove(token)
            cache.set(token, cacheValStr)
          } else {
            cacheValStr = s"$count:false:$timeOfFirstApiAccess"
            cache.set(token, cacheValStr, Duration((currTime-timeOfFirstApiAccess), MILLISECONDS))
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
