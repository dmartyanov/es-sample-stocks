package backend

import akka.actor.Actor
import com.typesafe.scalalogging.Logger
import models.StockOperation
import utils.{LoggerHelper, LoggerService}

import scala.util.Try

/**
 * Created by dmitry on 2/11/15.
 */
class StockOperationLogActor(stock: String) extends Actor {
  this: LoggerService =>

  import StockOperationLogActor._
  override val log: Logger = LoggerHelper[StockOperationLogActor]
  var operationsLog = Seq.empty[StockOperation]

  override def receive: Receive = {
    case AppendStockOp(st) if stock == st.stock =>
      Try(validate(st)) map { stock =>
        operationsLog = operationsLog :+ st
        sender() ! "Ok"
      } recover {
        case err: Exception =>
          sender() ! akka.actor.Status.Failure(loggedException(err))
      }
    case AppendStockOp(st) =>
      sender() ! akka.actor.Status.Failure(new RuntimeException(s"Log for $stock could not append ${st.stock}"))

    case GetLog(dttm) => sender() ! operationsLog.filter(_.dttm <= dttm)

    case msg =>
      log.warn(s"Unexpected message received [${msg.toString}}]")
      sender() ! akka.actor.Status.Failure(new RuntimeException(s"Unexpected message received [${msg.toString}]"))
  }

  def validate(st: StockOperation) = {
    if(operationsLog.isEmpty && st.buy) st
    else if(operationsLog.isEmpty)
      throw new IllegalArgumentException(s"'Sell' could not be the first operation")
    else if(operationsLog.last.dttm >= st.dttm)
      throw new IllegalArgumentException(s"Inserting operations into log is forbidden last: [${operationsLog.last.dttm}]")
    else if(!enoughQuantity(st))
      throw new IllegalStateException(s"Not enough quantity for selling. Stock: [$stock]")
    else st
  }

  def enoughQuantity(stock: StockOperation): Boolean = operationsLog.foldLeft(0l){ case (acc, st) =>
    if(st.buy) acc + st.quantity
    else acc - st.quantity
  } >= stock.quantity
}

object StockOperationLogActor {
  case class AppendStockOp(st: StockOperation)
  case class GetLog(dttm: Int)
}