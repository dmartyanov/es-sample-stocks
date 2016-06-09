package impl

import service.StockProvider
import utils.ConfigHelper

/**
 * Created by dmitry on 1/11/15.
 */
abstract class ConfigurationStockProvider extends StockProvider {
this: ConfigHelper =>
  val stocks = conf.get[List[String]]("stockList",  List("abc", "def", "xyz")) toSet
  val currencies  = conf.get[List[String]]("currencyList", List("usd", "gbp", "eur")) toSet

  val s2c = stocks map {stock =>
    val currency = conf.get[String](s"stock.$stock.currency",
        throw new IllegalStateException(s"Currency for stock $stock should be defined")
      )
    if(currencies.contains(currency)) stock -> currency
    else throw new IllegalStateException(s"Currency $currency is not supported")
  } toMap

  override def currency(stockId: String): String =
    s2c.getOrElse(stockId, throw new IllegalArgumentException(s"Stock $stockId is not supported"))
}
