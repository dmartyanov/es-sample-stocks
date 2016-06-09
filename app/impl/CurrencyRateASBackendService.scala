package impl

import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import backend.CurrencyRateLogActor
import com.typesafe.scalalogging.Logger
import service.CurrencyRateService
import utils._

import scala.concurrent.Future
import scala.concurrent.duration._

/**
 * Created by dmitry on 31/10/15.
 */
abstract class CurrencyRateASBackendService extends CurrencyRateService {
  this: LoggerService with ConfigHelper with ExecutionContextHelper with ActorSystemHelper =>

  import CurrencyRateLogActor._

  override val log: Logger = LoggerHelper[CurrencyRateASBackendService]

  lazy val backendActor = as.actorOf(Props(Wired.backendCurrencyActor))
  lazy val t = conf.get[Long]("backendTimeout", 10000l)
  implicit val timeout = Timeout(t, MILLISECONDS)

  //asynchromous call to event sourcing system
  override def setCurrencyRateOnDate(cId: String, factor: Double, dttm: Int): Future[Unit] =
    ask(backendActor, CurrencySetRate(cId, factor, dttm)).mapTo[String] map {
      res => log.info(s"Rate for $cId on $dttm was set to $factor")
    } recover {
      case err: Exception => throw loggedException(err)
    }

  //asynchromous call to event sourcing system
  override def getCurrencyRateOnDate(cId: String, dttm: Int): Future[Double] =
    ask(backendActor, CurrencyGetRate(cId, dttm)).mapTo[Double]

  override def info(c: String): Future[Seq[(Int, Double)]] =
    ask(backendActor, CurrencyInfo(c)).mapTo[Seq[(Int, Double)]]
}
