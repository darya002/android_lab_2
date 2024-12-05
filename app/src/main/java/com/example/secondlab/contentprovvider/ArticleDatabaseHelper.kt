package com.example.secondlab.contentprovvider

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ArticleDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "articles.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "articles"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_AUTHOR = "author"
        const val COLUMN_DATE = "date"
        const val COLUMN_CONTENT = "content"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_AUTHOR TEXT NOT NULL,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_CONTENT TEXT NOT NULL
            )
        """
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }
}
