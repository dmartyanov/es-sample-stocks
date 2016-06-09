import utils.{Wired, LoggerHelper, LoggerService}
import Wired.PlayConfiguration
import com.typesafe.scalalogging.Logger
import play.api.{Application, GlobalSettings}
import utils.{LoggerHelper, LoggerService}

import scala.util.Try

/**
 * Created by dmitry on 1/11/15.
 */
object Global extends GlobalSettings with PlayConfiguration with LoggerService {

  override val log: Logger = LoggerHelper[Global.type]

  override def onStart(app: Application): Unit = {
    super.onStart(app)
    log.info("Stock event apps started successfully")
  }

  override def beforeStart(app: Application): Unit = {
    super.beforeStart(app)
    try {Wired.stockProviderFromConf}
    catch{
      case err: Throwable =>
        log.error(s"Application will be stopped because of initialization error ${err.getMessage}", err)
        app.stop()
    }
  }
}
