package com.cookly.data.repository

import com.cookly.data.db.ProductDao
import com.cookly.data.model.Product
import kotlinx.coroutines.flow.Flow

/** Repositorio: los ViewModels piden datos aquí, sin saber que debajo hay Room/SQLite. */
class ProductRepository(private val dao: ProductDao) {
    fun observe(ownerId: Long): Flow<List<Product>> = dao.observeByOwner(ownerId)
    suspend fun find(id: Long, ownerId: Long): Product? = dao.findById(id, ownerId)
    suspend fun findByBarcode(barcode: String, ownerId: Long): Product? = dao.findByBarcode(ownerId, barcode)
    suspend fun add(product: Product): Long = dao.insert(product)
    suspend fun update(product: Product) = dao.update(product)
    suspend fun delete(product: Product) = dao.delete(product)
}
