package hateoas

import commons.CollectionLinks
import domain.ActionDiscount
import org.joda.time.DateTime

object action_discounts {

  case class ActionDiscountAttributes(name: String, from: DateTime, to: DateTime, discount: Double)

  case class ActionDiscountRequestData(`type`: String, attributes: ActionDiscountAttributes)

  case class ActionDiscountRequest(data: ActionDiscountRequestData) {
    def toDomain: ActionDiscount = {
      val attributes = data.attributes

      ActionDiscount(
        name = attributes.name,
        from = attributes.from,
        to = attributes.to,
        discount = attributes.discount
      )
    }
  }

  case class ActionDiscountResponseData(`type`: String, id: Long, attributes: ActionDiscountAttributes)

  object ActionDiscountResponseData {
    def fromDomain(actionDiscount: ActionDiscount): ActionDiscountResponseData = {
      ActionDiscountResponseData(
        `type` = ActionDiscountsType,
        id = actionDiscount.id.get,
        attributes = ActionDiscountAttributes(
          name = actionDiscount.name,
          from = actionDiscount.from,
          to = actionDiscount.to,
          discount = actionDiscount.discount
        )
      )
    }
  }

  case class ActionDiscountResponse(data: ActionDiscountResponseData)

  object ActionDiscountResponse {
    def fromDomain(actionDiscount: ActionDiscount): ActionDiscountResponse =
      ActionDiscountResponse(data = ActionDiscountResponseData.fromDomain(actionDiscount))
  }

  case class ActionDiscountCollectionResponse(data: Seq[ActionDiscountResponseData], links: CollectionLinks)

  object ActionDiscountCollectionResponse {
    def fromDomain(actionDiscounts: Seq[ActionDiscount], links: CollectionLinks): ActionDiscountCollectionResponse = {
      ActionDiscountCollectionResponse(
        data = actionDiscounts.map(ActionDiscountResponseData.fromDomain),
        links = links
      )
    }
  }

}
