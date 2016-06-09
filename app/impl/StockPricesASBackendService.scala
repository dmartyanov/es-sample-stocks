package impl

import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import backend.StockPriceLogActor
import com.typesafe.scalalogging.Logger
import service.StockPricesService
import utils._

import scala.concurrent.Future
import scala.concurrent.duration._

/**
 * Created by dmitry on 1/11/15.
 */
abstract class StockPricesASBackendService extends StockPricesService {
  this: LoggerService with ConfigHelper with ExecutionContextHelper with ActorSystemHelper =>

  import StockPriceLogActor._

  override val log: Logger = LoggerHelper[StockPricesASBackendService]

  lazy val backendActor = as.actorOf(Props(Wired.backendStockActor))
  lazy val t = conf.get[Long]("backendTimeout", 10000l)
  implicit val timeout = Timeout(t, MILLISECONDS)

  override def setStockPrice(stock: String, price: Double, dttm: Int): Future[Unit] =
    ask(backendActor, StockSetPrice(stock, price, dttm)).mapTo[String] map {
      res => log.info(s"Stock $stock price on $dttm was set to $price")
    } recover {
      case err: Exception => throw loggedException(err)
    }

  override def getStockPrice(stock: String, dttm: Int): Future[Double] =
    ask(backendActor, StockGetPrice(stock, dttm)).mapTo[Double]

  override def info(stock: String): Future[Seq[(Int, Double)]] =
    ask(backendActor, StockInfo(stock)).mapTo[Seq[(Int, Double)]]
}
