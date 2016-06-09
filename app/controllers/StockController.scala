package controllers

import com.typesafe.scalalogging.Logger
import play.api.libs.json._
import utils.Wired.{StockInMemoryComponent, ActorSystemExecutionContext, CurrencyInMemoryComponent}
import utils.{LoggerHelper, LoggerService}

/**
 * Created by dmitry on 1/11/15.
 */
class StockController extends AbstractController 
with CurrencyInMemoryComponent
with StockInMemoryComponent
with LoggerService with ActorSystemExecutionContext {

  override val log: Logger = LoggerHelper[StockController]
  def seq2JsValue(o: Seq[(Int, Double)]): JsValue =
    JsObject(o.map { case (k, v) => k.toString -> JsString(v.toString) })

  def appendCurrencyRate(cId: String, dttm: Int, rate: Double) =
    handleRequest(s"PUT     /currency/$cId/rate/$dttm ") {
      if(dttm>0) currencyRateService.setCurrencyRateOnDate(cId, rate, dttm).map(_ => JsString("Appended"))
      else throw new IllegalArgumentException("dttm value should be positive")
    }

  def fetchCurrencyRate(cId: String, dttm: Int) =
    handleRequest(s"GET     /currency/$cId/rate/:dttm ") {
      if(dttm>0) currencyRateService.getCurrencyRateOnDate(cId, dttm).map(r => JsString(r.toString))
      else throw new IllegalArgumentException("dttm value should be positive")
    }

  def currencyInfo(cId: String) =
    handleRequest(s"GET    /currency/$cId/rate") {
      currencyRateService.info(cId).map(seq2JsValue)
    }

  def appendStockPrice(sId: String, dttm: Int, price: Double) = {
    handleRequest(s"PUT     /stock/$sId/price/$dttm") {
      if(dttm>0) stockPricesService.setStockPrice(sId, price, dttm).map(_ => JsString("Appended"))
      else throw new IllegalArgumentException("dttm value should be positive")
    }
  }
  
  def fetchStockPrice(sId: String, dttm: Int) = 
    handleRequest(s"GET     /stock/$sId/price/$dttm") {
      if(dttm>0) stockPricesService.getStockPrice(sId, dttm).map(r => JsString(r.toString))
      else throw new IllegalArgumentException("dttm value should be positive")
    }
  
  def stockInfo(sId: String) = 
  handleRequest(s"GET     /stock/$sId/price") {
    stockPricesService.info(sId).map(seq2JsValue)
  }
}
