package controllers

import com.typesafe.scalalogging.Logger
import models.{StockOperation, StockOperationsResult}
import play.api.libs.json.{JsString, Json}
import utils.Wired.{ActorSystemExecutionContext, OperationServiceComponentImpl}
import utils.{LoggerHelper, LoggerService}

/**
 * Created by dmitry on 2/11/15.
 */
class OperationController extends AbstractController with OperationServiceComponentImpl
with LoggerService with ActorSystemExecutionContext {

  override val log: Logger = LoggerHelper[OperationController]

  implicit val operationsResultsWrites = Json.writes[StockOperationsResult]

  def buy(sId: String, dttm: Int, quantity: Long) =
    handleRequest(s"PUT     /ops/$sId/buy/$dttm") {
      if (dttm > 0) operationService.appendEvent(StockOperation(
        stock = sId,
        buy = true,
        quantity = quantity,
        dttm = dttm
      )).map(_ => JsString("Appended"))
      else throw new IllegalArgumentException("dttm value should be positive")
    }

  def sell(sId: String, dttm: Int, quantity: Long) =
    handleRequest(s"PUT     /ops/$sId/sell/$dttm") {
      if (dttm > 0) operationService.appendEvent(StockOperation(
        stock = sId,
        buy = false,
        quantity = quantity,
        dttm = dttm
      )).map(_ => JsString("Appended"))
      else throw new IllegalArgumentException("dttm value should be positive")
    }

  def net(dttm: Int) =
    handleRequest(s"GET     /ops/net/$dttm") {
      if (dttm > 0) operationService.net(dttm).map(r => Json.toJson(r))
      else throw new IllegalArgumentException("dttm value should be positive")
    }

  def netByStock(sId: String, dttm: Int) =
    handleRequest(s"GET     /ops/:sId/net/:dttm") {
      if (dttm > 0) operationService.netByStock(sId, dttm).map(r => Json.toJson(r))
      else throw new IllegalArgumentException("dttm value should be positive")
    }
}
