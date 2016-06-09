package utils

import play.api.Configuration

import scala.concurrent.duration.FiniteDuration
import scala.reflect.runtime.universe._
import scala.language.implicitConversions
import scala.collection.JavaConverters._

/**
 * Created by dmitry on 31/10/15.
 */
trait ConfigHelper {
  val conf: Configuration

  implicit def config(c: Configuration): ExtendedConfiguration = new ExtendedConfiguration(c)
}

class ExtendedConfiguration(config: Configuration) {
  private val StringTag = typeTag[String]
  private val ScalaDurationTag = typeTag[scala.concurrent.duration.Duration]
  private val ScalaFiniteDurationTag = typeTag[scala.concurrent.duration.FiniteDuration]
  private val StringListTag = typeTag[List[String]]
  private val BooleanTag = typeTag[Boolean]

  def getOpt[T](path: String)(implicit tag: TypeTag[T]): Option[T] = (tag match {
    case StringTag => config.getString(path)
    case ScalaDurationTag => config.getMilliseconds(path)
    case ScalaFiniteDurationTag =>
      FiniteDuration(config.getMilliseconds(path).get, scala.concurrent.duration.MILLISECONDS)
    case TypeTag.Int => config.getInt(path)
    case TypeTag.Double => config.getDouble(path)
    case TypeTag.Long => config.getLong(path)
    case StringListTag => config.getStringList(path).map(_.asScala.toList)
    case BooleanTag => config.getBoolean(path)
    case _ => throw new IllegalArgumentException(s"Configuration option type $tag not implemented")
  }).asInstanceOf[Option[T]]

  def get[T](path: String, default: => T)(implicit tag: TypeTag[T]) = getOpt(path).getOrElse(default)

  def get[T](path: String)(implicit tag: TypeTag[T]) = getOpt(path)
    .getOrElse(throw new RuntimeException(s"Configuration value at path $path not found"))
}


