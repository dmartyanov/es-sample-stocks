package models

/**
 * Created by dmitry on 2/11/15.
 */
case class StockOperationsResult(
                                  net: Double,
                                  activities: Map[String, Long] = Map()
                                  ) {
  def ~(that: StockOperationsResult) =
    StockOperationsResult(
      net = this.net + that.net,
      activities = merge(this.activities, that.activities)
    )

  def merge(m1: Map[String, Long], m2: Map[String, Long]) =
    m1 ++ m2 map { case (k, v) => k -> (v + m1.getOrElse(k, 0l)) }

}

object StockOperationsResult {

  import play.api.libs.functional.syntax._
  import play.api.libs.json.{JsPath, Writes}

  implicit val ruleWrites: Writes[StockOperationsResult] = (
    (JsPath \ "net").write[Double] and
    (JsPath \ "activities").write[Map[String, Long]]
  )(unlift(StockOperationsResult.unapply))
}


