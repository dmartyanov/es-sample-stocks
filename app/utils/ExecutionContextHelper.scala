package utils

import scala.concurrent.ExecutionContext

/**
  * Created by dmitry on 31/10/15.
  */
trait ExecutionContextHelper {
   implicit def ec: ExecutionContext
 }