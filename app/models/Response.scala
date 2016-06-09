package models

import play.api.libs.json.JsValue

/**
 * Created by dmitry on 1/11/15.
 */
case class ResponseModel(
                          error: Boolean,
                          body: Option[JsValue],
                          errMsg: Option[String] = None
                          )

object ResponseModel {

  import play.api.libs.functional.syntax._
  import play.api.libs.json.{JsPath, Writes}

  implicit val ruleWrites: Writes[ResponseModel] = (
    (JsPath \ "error").write[Boolean] and
    (JsPath \ "body").writeNullable[JsValue] and
    (JsPath \ "errMsg").writeNullable[String]
  )(unlift(ResponseModel.unapply))
}