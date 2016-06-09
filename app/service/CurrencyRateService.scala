package service

import scala.concurrent.Future

/**
 * Created by dmitry on 31/10/15.
 */
trait CurrencyRateService {
  def setCurrencyRateOnDate(c: String, rate: Double, dttm: Int): Future[Unit]

  def getCurrencyRateOnDate(c: String, dttm: Int): Future[Double]

  def info(c: String): Future[Seq[(Int, Double)]]
}

trait CurrencyRateComponent {
  def currencyRateService: CurrencyRateService
}