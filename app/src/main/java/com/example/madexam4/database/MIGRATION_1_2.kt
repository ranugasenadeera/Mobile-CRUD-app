package com.example.madexam4.database

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Perform migration SQL statements here
        database.execSQL("ALTER TABLE tasks ADD COLUMN taskDate TEXT DEFAULT '' NOT NULL")
    }
}