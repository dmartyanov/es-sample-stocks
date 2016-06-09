package utils

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.reflect.ClassTag

/**
 * Created by dmitry on 31/10/15.
 */
trait LoggerService {
  val log: Logger

  def loggedException(err: Throwable) = {
    log.error(err.getMessage, err)
    err
  }
}


object LoggerHelper {
  def apply[T](implicit cls: ClassTag[T]): Logger =
    com.typesafe.scalalogging.Logger(LoggerFactory.getLogger(cls.runtimeClass))
}
