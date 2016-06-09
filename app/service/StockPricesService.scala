package service

import scala.concurrent.Future

/**
 * Created by dmitry on 31/10/15.
 */
trait StockPricesService {
  def setStockPrice(stock: String, price: Double, dttm: Int): Future[Unit]

  def getStockPrice(stock: String, dttm: Int): Future[Double]

  def info(stock: String): Future[Seq[(Int, Double)]]
}

trait StockPricesComponent {
  def stockPricesService: StockPricesService
}
