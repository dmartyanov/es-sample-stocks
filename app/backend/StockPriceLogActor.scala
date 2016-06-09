package backend

import akka.actor.Actor
import com.typesafe.scalalogging.Logger
import service.StockProviderComponent
import utils.{LoggerHelper, ConfigHelper, LoggerService}

import scala.collection.mutable.Map

/**
 * Created by dmitry on 1/11/15.
 */
abstract class StockPriceLogActor extends Actor with StockProviderComponent {
  this: LoggerService with ConfigHelper =>

  import StockPriceLogActor._

  override val log: Logger = LoggerHelper[StockPriceLogActor]

  val stocksLog = buildMap

  override def receive: Receive = {
    case StockGetPrice(sId, dttm) =>
      stocksLog.get(sId) match {
        case Some(l) =>
          sender() ! l
            .filter(_._1 <= dttm)
            .maxBy(_._1)
            ._2
        case None =>
          sender() ! akka.actor.Status.Failure(
            new IllegalArgumentException(s"Stock with id $sId is not supported")
          )
      }
    case StockSetPrice(sId, price, dttm) =>
      stocksLog.get(sId) match {
        case Some(l) =>
          stocksLog.put(sId, l ++ Map(dttm -> price))
          sender() ! "Ok"
        case None =>
          sender() ! akka.actor.Status.Failure(new IllegalArgumentException(s"Stock with id $sId is not supported"))
      }

    case StockInfo(sId) =>
      stocksLog.get(sId) match {
        case Some(l) =>
          sender() ! l.toSeq.sortBy(-_._1)
        case None =>
          sender() ! akka.actor.Status.Failure(new IllegalArgumentException(s"Stock with id $sId is not supported"))
      }
    case msg =>
      log.error(s"Received unexpected message ${msg.toString}")
  }

  def buildMap: Map[String, Map[Int, Double]] = {
    val mb = Map.newBuilder[String, Map[Int, Double]]

    stockProvider.stocks foreach { stock =>
      mb += stock -> Map(0 -> conf.get[Double](s"stock.$stock.initialPrice", 1d))
    }
    mb.result()
  }
}

object StockPriceLogActor {

  case class StockSetPrice(sId: String, price: Double, dttm: Int)

  case class StockGetPrice(sId: String, dttm: Int)

  case class StockInfo(sId: String)
}
