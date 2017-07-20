package controllers

import javax.inject.Singleton

import com.google.inject.Inject
import commons.{CollectionLinks, Error, ErrorResponse}
import hateoas.buyer_categories._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, Controller}
import repositories.{BuyerCategoryRepository, ConsumptionThresholdRepository}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BuyerCategories @Inject()(buyerCategories: BuyerCategoryRepository, thresholds: ConsumptionThresholdRepository)
                               (implicit val ec: ExecutionContext) extends Controller {

  import hateoas.JsonApi._

  def create(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[BuyerCategoryRequest].fold(
      failures => Future.successful(BadRequest(Json.toJson(
        ErrorResponse(errors = Seq(Error(BAD_REQUEST.toString, "Malformed JSON specified.")))
      ))),

      spec => {
        buyerCategories.save(spec.toDomain).map(category =>
          Created(Json.toJson(
            BuyerCategoryResponse.fromDomain(category)
          ))
        )
      }
    )
  }

  def retrieveAll(offset: Int, limit: Int): Action[AnyContent] = Action.async { implicit request =>
    buyerCategories.retrieveAll(offset, limit).map(categories => {
      val self = routes.BuyerCategories.retrieveAll(offset, limit).absoluteURL()
      val next = if (limit == categories.length) Some(routes.BuyerCategories.retrieveAll(offset + limit, limit).absoluteURL()) else None

      Ok(Json.toJson(
        BuyerCategoryCollectionResponse.fromDomain(categories, CollectionLinks(self, next))
      ))
    })
  }

  def retrieveThresholds(buyerCategoryId: Long, offset: Int, limit: Int): Action[AnyContent] = Action.async { implicit request =>
    // FIXME Buyer Category doesn't exist
    thresholds.retrieveByBuyerCategory(buyerCategoryId, offset, limit).map(result => {
      val self = routes.BuyerCategories.retrieveThresholds(buyerCategoryId, offset, limit).absoluteURL()
      val next = if (limit == result.length) Some(routes.BuyerCategories.retrieveThresholds(buyerCategoryId, offset + limit, limit).absoluteURL()) else None

      Ok(Json.toJson(
        ConsumptionThresholdCollectionResponse.fromDomain(result, CollectionLinks(self, next))
      ))
    })
  }

  def addThreshold(id: Long): Action[JsValue] = Action.async(parse.json) { implicit request =>
    // FIXME Buyer Category doesn't exist
    request.body.validate[ConsumptionThresholdRequest].fold(
      failures => Future.successful(BadRequest(Json.toJson(
        ErrorResponse(errors = Seq(Error(BAD_REQUEST.toString, "Malformed JSON specified.")))
      ))),

      spec =>
        thresholds.save(spec.toDomain).map(threshold =>
          Created(Json.toJson(
            ConsumptionThresholdResponse.fromDomain(threshold)
          ))
        )
    )
  }

  def removeThreshold(categoryId: Long, thresholdId: Long): Action[AnyContent] = Action.async {
    buyerCategories.retrieveOne(categoryId) flatMap {
      case Some(category) =>
        thresholds.delete(thresholdId).map(affected =>
          if (affected > 0) {
            NoContent
          }
          else {
            NotFound(Json.toJson(
              ErrorResponse(errors = Seq(Error(NOT_FOUND.toString, s"Threshold $thresholdId doesn't exist!")))
            ))
          }
        )

      case None => Future.successful(NotFound(Json.toJson(
        ErrorResponse(errors = Seq(Error(NOT_FOUND.toString, s"Buyer Category $categoryId doesn't exist!")))
      )))
    }
  }

}