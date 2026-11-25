package com.cookly.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.cookly.data.model.Product
import kotlinx.coroutines.flow.Flow

/** Consultas SQL de la tabla products. En KMP las funciones de Room deben ser suspend o devolver Flow. */
@Dao
interface ProductDao {
    /** Flow: cada vez que la tabla cambia, emite la lista nueva (la Nevera se actualiza sola). */
    @Query("SELECT * FROM products WHERE ownerId = :ownerId ORDER BY expirationEpochDay ASC")
    fun observeByOwner(ownerId: Long): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id AND ownerId = :ownerId LIMIT 1")
    suspend fun findById(id: Long, ownerId: Long): Product?

    @Query("SELECT * FROM products WHERE ownerId = :ownerId AND barcode = :barcode LIMIT 1")
    suspend fun findByBarcode(ownerId: Long, barcode: String): Product?

    @Insert
    suspend fun insert(product: Product): Long

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)
}
