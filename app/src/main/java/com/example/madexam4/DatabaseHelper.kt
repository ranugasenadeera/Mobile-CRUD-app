package com.example.madexam4

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context): SQLiteOpenHelper(context, Database_Name, null, Database_version) {

    companion object{
        private const val Database_Name = "task.db"
        private const val Database_version = 1
        private const val Table_name = "eventlist"
        private const val Column_id = "id"
        private const val Column_title = "title"
        private const val Column_desc = "description"
        private const val Column_date = "date"
        private const val Column_location = "location"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = "CREATE TABLE $Table_name ($Column_id INTEGER PRIMARY KEY, $Column_title TEXT, $Column_desc TEXT, $Column_date TEXT, $Column_location TEXT)"
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dropTableQuery = "DROP TABLE IF EXISTS $Table_name"
        db?.execSQL(dropTableQuery)
        onCreate(db)
    }

    fun insertEvent(event: Events){
        val db = writableDatabase
        val values = ContentValues().apply{
            put(Column_title, event.title)
            put(Column_desc, event.description)
            put(Column_date, event.date)
            put(Column_location, event.location)
        }
        db.insert(Table_name, null, values)
    }

    fun getAllEvents(): List<Events>{
        val eventsList = mutableListOf<Events>()
        val db = readableDatabase
        val query = "SELECT * FROM $Table_name"
        val cursor = db.rawQuery(query, null)

        while(cursor.moveToNext()){
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(Column_id))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(Column_title))
            val description = cursor.getString(cursor.getColumnIndexOrThrow(Column_desc))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(Column_date))
            val location = cursor.getString(cursor.getColumnIndexOrThrow(Column_location))

            val event = Events(id, title, description, date, location)
            eventsList.add(event)
        }
        cursor.close()
        db.close()
        return eventsList
    }
}