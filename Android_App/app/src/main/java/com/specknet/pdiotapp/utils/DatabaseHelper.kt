package com.specknet.pdiotapp.utils

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.specknet.pdiotapp.UserLogin
import java.text.SimpleDateFormat
import java.util.Date

class DatabaseHelper(private val context: Context):
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){
    companion object{
        private const val DATABASE_NAME = "UserDatabase.db"
        private  const val DATABASE_VERSION = 2
        private const val LOGIN_TABLE_NAME = "logindata"
        private const val DATA_TABLE_NAME = "userdata"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_TIMESTAMP = "time"
        private const val COLUMN_PREDICTION = "prediction"

    }


    override fun onCreate(db: SQLiteDatabase?) {
        val createLoginTableQuery = ("CREATE TABLE $LOGIN_TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_NAME TEXT," +
                "$COLUMN_USERNAME TEXT," +
                "$COLUMN_PASSWORD TEXT)")

        val createDataTableQuery = ("CREATE TABLE $DATA_TABLE_NAME (" +
                "$COLUMN_USERNAME TEXT," +
                "$COLUMN_TIMESTAMP DATETIME," +
                "$COLUMN_PREDICTION TEXT)")
        db?.execSQL(createLoginTableQuery)
        db?.execSQL(createDataTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        val dropLoginTableQuery = "DROP TABLE IF EXISTS $LOGIN_TABLE_NAME"
        db?.execSQL(dropLoginTableQuery)
        val dropDataTableQuery = "DROP TABLE IF EXISTS $DATA_TABLE_NAME"
        db?.execSQL(dropDataTableQuery)
        onCreate(db)
    }

    fun insertUser(name: String, username: String, password: String): Boolean {
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, password)
        }
        val db = writableDatabase
        val rowId = db.insert(LOGIN_TABLE_NAME, null, values)
        return rowId > 0
    }

    fun readUser(username: String, password: String): Boolean {
        val db = readableDatabase
        val sel = "$COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?"
        val selArgs = arrayOf(username, password)
        val cursor = db.query(LOGIN_TABLE_NAME, null, sel, selArgs, null, null, null)

        val userExists = cursor.count > 0
        cursor.close()
        return userExists
    }

    fun checkUser(username: String): Boolean {
        val db = readableDatabase
        val sel = "$COLUMN_USERNAME = ?"
        val selArgs = arrayOf(username)
        val cursor = db.query(LOGIN_TABLE_NAME, null, sel, selArgs, null, null, null)

        val userExists = cursor.count > 0
        cursor.close()
        return userExists
    }

    fun insertData(prediction: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentTime = sdf.format(Date())

        val values = ContentValues().apply {
            put(COLUMN_USERNAME, UserLogin.currentUser)
            put(COLUMN_TIMESTAMP, currentTime)
            put(COLUMN_PREDICTION, prediction)
        }
        val db = writableDatabase
        db.insert(DATA_TABLE_NAME, null, values)
    }

    fun deleteData(timestamp: String) {
        val deleteDataQuery = ("DELETE FROM $DATA_TABLE_NAME WHERE " +
                "$COLUMN_TIMESTAMP >= '$timestamp'")
        val db = writableDatabase
        db.execSQL(deleteDataQuery)
    }

    fun readData(date: String): List<String>{
        val db = readableDatabase
        val username = UserLogin.currentUser
        val startTime = "$date 00:00:00"
        val endTime = "$date 23:59:59"
        val query = "SELECT DISTINCT $COLUMN_PREDICTION FROM $DATA_TABLE_NAME WHERE $COLUMN_USERNAME = ? AND $COLUMN_TIMESTAMP BETWEEN ? AND ?"
        val cursor = db.rawQuery(query, arrayOf(username, startTime, endTime))
        val predictions = mutableListOf<String>()
        while (cursor.moveToNext()) {
            val prediction = cursor.getString(cursor.getColumnIndex(COLUMN_PREDICTION))
            predictions.add(prediction)
        }
        cursor.close()
        return predictions
    }
}