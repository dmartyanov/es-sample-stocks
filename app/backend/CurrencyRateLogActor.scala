package backend

import akka.actor.Actor
import com.typesafe.scalalogging.Logger
import service.StockProviderComponent
import utils.{ConfigHelper, LoggerHelper, LoggerService}

import scala.collection.mutable.Map

/**
 * Created by dmitry on 1/11/15.
 */
abstract class CurrencyRateLogActor extends Actor with StockProviderComponent {
  this: LoggerService with ConfigHelper =>

  import CurrencyRateLogActor._

  override val log: Logger = LoggerHelper[CurrencyRateLogActor]
  val currenciesLog = buildMap
  
  override def receive: Receive = {
    case CurrencyGetRate(cId, dttm) =>
      currenciesLog.get(cId) match {
        case Some(l) =>
          sender() ! l
            .filter(_._1 <= dttm)
            .maxBy(_._1)
            ._2
        case None =>
          sender() ! akka.actor.Status.Failure(new IllegalArgumentException(s"Currency with id $cId is not supported"))
      }

    case CurrencySetRate(cId, rate, dttm) =>
      currenciesLog.get(cId) match {
        case Some(l) =>
          currenciesLog.put(cId, l ++ Map(dttm -> rate))
          sender() ! "Ok"
        case None =>
          sender() ! akka.actor.Status.Failure(new IllegalArgumentException(s"Currency with id $cId is not supported"))
      }

    case CurrencyInfo(cId) =>
      currenciesLog.get(cId) match {
        case Some(l) =>
          sender() ! l.toSeq.sortBy(-_._1)
        case None =>
          sender() ! akka.actor.Status.Failure(new IllegalArgumentException(s"Currency with id $cId is not supported"))
      }
    case msg =>
      log.error(s"Received unexpected message ${msg.toString}")
  }
      
  
  def buildMap: Map[String, Map[Int, Double]] = {
    val mb = Map.newBuilder[String, Map[Int, Double]]

    stockProvider.currencies foreach { c =>
      mb += c -> Map(0 -> conf.get[Double](s"currency.$c.initialRate", 1d))
    }
    mb.result()
  }
}

object CurrencyRateLogActor {
  case class CurrencySetRate(c: String, rate: Double, dttm: Int)

  case class CurrencyGetRate(c: String, dttm: Int)

  case class CurrencyInfo(c: String)
}
