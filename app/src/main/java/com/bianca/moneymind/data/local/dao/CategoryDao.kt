package com.bianca.moneymind.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bianca.moneymind.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder")
    fun getAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY sortOrder")
    fun getByType(type: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE parentId IS NULL ORDER BY sortOrder")
    fun getParentCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE parentId = :parentId ORDER BY sortOrder")
    fun getSubCategories(parentId: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Delete
    suspend fun delete(category: CategoryEntity)
}
