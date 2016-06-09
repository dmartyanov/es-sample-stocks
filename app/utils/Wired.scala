package utils

import akka.actor.Props
import backend.{CurrencyRateLogActor, StockOperationLogActor, StockPriceLogActor}
import impl.{ConfigurationStockProvider, CurrencyRateASBackendService, OperationServiceImpl, StockPricesASBackendService}
import play.api.Configuration
import service._

import scala.concurrent.ExecutionContext

/**
 * Created by dmitry on 1/11/15.
 */
object Wired {

  lazy val configuration = play.api.Play.current.configuration

  trait PlayConfiguration extends ConfigHelper {
    lazy val conf: Configuration = configuration
  }

  lazy val actorSystem = play.libs.Akka.system()

  trait StopsPlayActorSystem extends ActorSystemHelper {
    override val as = actorSystem
  }

  trait ActorSystemExecutionContext extends ExecutionContextHelper {
    override implicit def ec: ExecutionContext = actorSystem.dispatcher
  }

  lazy val stockProviderFromConf = new ConfigurationStockProvider with PlayConfiguration

  trait ConfStockComponent extends StockProviderComponent {
    override def stockProvider: StockProvider = stockProviderFromConf
  }

  lazy val currencyLocalStorageServiceImpl = new CurrencyRateASBackendService with PlayConfiguration
    with LoggerService with ActorSystemExecutionContext with StopsPlayActorSystem

  trait CurrencyInMemoryComponent extends CurrencyRateComponent {
    override def currencyRateService: CurrencyRateService = currencyLocalStorageServiceImpl
  }

  lazy val stockLocalStorageServiceImpl = new StockPricesASBackendService with PlayConfiguration
    with LoggerService with ActorSystemExecutionContext with StopsPlayActorSystem

  trait StockInMemoryComponent extends StockPricesComponent {
    override def stockPricesService: StockPricesService = stockLocalStorageServiceImpl
  }

  lazy val operationServiceImpl = new OperationServiceImpl with StockInMemoryComponent with CurrencyInMemoryComponent
    with LoggerService with PlayConfiguration with ConfStockComponent with ActorSystemExecutionContext
    with StopsPlayActorSystem

  trait OperationServiceComponentImpl extends OperationComponent {
    override def operationService: OperationService = operationServiceImpl
  }

  lazy val backendCurrencyActor = new CurrencyRateLogActor with ConfStockComponent
    with LoggerService with PlayConfiguration

  lazy val backendStockActor = new StockPriceLogActor with ConfStockComponent
    with LoggerService with PlayConfiguration

}
