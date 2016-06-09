package service

import models.{StockOperation, StockOperationsResult}

import scala.concurrent.Future

/**
 * Created by dmitry on 2/11/15.
 */
trait OperationService {
  def appendEvent(stop: StockOperation): Future[Unit]

  def net(dttm: Int): Future[StockOperationsResult]

  def netByStock(sId: String, dttm: Int): Future[StockOperationsResult]
}

trait OperationComponent {
  def operationService: OperationService
}