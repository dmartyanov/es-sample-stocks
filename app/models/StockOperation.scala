package models

/**
 * Created by dmitry on 2/11/15.
 */
case class StockOperation(
                           stock: String,
                           buy: Boolean,
                           quantity: Long,
                           dttm: Int
                           )
