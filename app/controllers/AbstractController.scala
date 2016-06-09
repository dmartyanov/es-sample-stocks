package controllers

import models.ResponseModel
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, Controller}
import utils.{ExecutionContextHelper, LoggerService}

import scala.concurrent.Future

/**
 * Created by dmitry on 1/11/15.
 */
abstract class AbstractController extends Controller {
  this: LoggerService with ExecutionContextHelper =>

  implicit val responseModelWrites = Json.writes[ResponseModel]

  def handleRequest[T](endpoint: String)(req: => Future[JsValue]) = Action.async {
    req.map { result => Ok(Json.toJson(ResponseModel(false, Some(result)))) }
      .recover {
        case err: Exception =>
          log.error(s"$endpoint proceed with excpetion ${err.getMessage}", err)
          Ok(Json.toJson(ResponseModel(true, None, Some(err.getMessage))))
      }
  }
}
