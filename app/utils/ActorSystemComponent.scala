package utils

import akka.actor.ActorSystem

/**
 * Created by dmitry on 31/10/15.
 */
trait ActorSystemHelper {
  implicit val as: ActorSystem
}