package impl

import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import backend.StockOperationLogActor
import com.typesafe.scalalogging.Logger
import models.{StockOperation, StockOperationsResult}
import service.{CurrencyRateComponent, OperationService, StockPricesComponent, StockProviderComponent}
import utils._

import scala.concurrent.Future
import scala.concurrent.duration._

/**
 * Created by dmitry on 2/11/15.
 */
abstract class OperationServiceImpl extends OperationService with StockProviderComponent
with CurrencyRateComponent with StockPricesComponent {
  this: ActorSystemHelper with ConfigHelper with LoggerService with ExecutionContextHelper =>

  import StockOperationLogActor._

  override val log: Logger = LoggerHelper[OperationServiceImpl]

  lazy val t = conf.get[Long]("backendTimeout", 10000l)
  implicit val timeout = Timeout(t, MILLISECONDS)

  lazy val ls = stockProvider.stocks map { stock =>
    stock -> as.actorOf(Props(new StockOperationLogActor(stock) with LoggerService))
  } toMap


  override def appendEvent(stop: StockOperation): Future[Unit] =
    withSelection(stop.stock) { ar =>
      ask(ar, AppendStockOp(stop)).mapTo[String].map {
        res => log.info(s"Appended event ${stop.toString}")
      } recover {
        case err: Exception => throw loggedException(err)
      }
    }

  override def net(dttm: Int): Future[StockOperationsResult] =
    Future.sequence(stockProvider.stocks map { stock =>
      netByStock(stock, dttm)
    }) map { rs => rs.reduce(_ ~ _) }


  override def netByStock(stock: String, dttm: Int) =
    withSelection(stock) {
      ar => ask(ar, GetLog(dttm)).mapTo[Seq[StockOperation]]
    } flatMap { log =>
      (Future sequence log.map(eventToResult)).map { ls =>
        ls.fold(StockOperationsResult(0))(_ ~ _)
      }
    }

  private def eventToResult(so: StockOperation) = {
    (for {
      rate <- currencyRateService.getCurrencyRateOnDate(stockProvider.currency(so.stock), so.dttm)
      price <- stockPricesService.getStockPrice(so.stock, so.dttm)
    } yield so.quantity * price * rate) map { usdNet =>
      if (so.buy) StockOperationsResult(
        net = -usdNet,
        activities = Map(so.stock -> so.quantity)
      )
      else StockOperationsResult(
        net = usdNet,
        activities = Map(so.stock -> -1 * so.quantity)
      )
    }
  }

  private def withSelection[T](sId: String)(op: ActorRef => Future[T]) = ls.get(sId) match {
    case Some(ar) => op(ar)
    case None => Future.failed(new IllegalArgumentException(s"Stock $sId is not supported"))
  }

}
