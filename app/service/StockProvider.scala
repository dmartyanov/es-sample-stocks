package service

/**
 * Created by dmitry on 1/11/15.
 */
trait StockProvider {
  val currencies: Set[String]
  val stocks: Set[String]
  def currency(stockId: String): String
}

trait StockProviderComponent {
  def stockProvider: StockProvider
}
