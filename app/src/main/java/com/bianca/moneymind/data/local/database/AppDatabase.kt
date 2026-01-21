package com.bianca.moneymind.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bianca.moneymind.data.local.dao.CategoryDao
import com.bianca.moneymind.data.local.dao.TransactionDao
import com.bianca.moneymind.data.local.entity.CategoryEntity
import com.bianca.moneymind.data.local.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
}
