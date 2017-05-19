package domain

import org.joda.time.DateTime

case class Product(id: Option[Long] = None,
                   name: String,
                   category: ProductCategory,
                   price: Double,
                   quantity: Long,
                   createdAt: DateTime,
                   fillStock: Boolean = false,
                   status: ProductStatus = ProductStatus.ACTIVE,
                   minQuantity: Long)