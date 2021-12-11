package com.github.stargrey.secret.data

import android.content.Context
import android.provider.BaseColumns
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteOpenHelper

class CipherDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object{
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "cipherDB.db"

        val CipherTable = CipherDatabase.CipherTable
        val CipherText = CipherDatabase.CipherNotes
        val CipherCard = CipherDatabase.CipherCard

        private const val SQL_CREATE_ENTRIES =
            """
                CREATE TABLE ${CipherTable.TABLE_NAME} (
                ${BaseColumns._ID} INTEGER PRIMARY KEY,
                ${CipherTable.COLUMN_TITLE} TEXT,
                ${CipherTable.COLUMN_IMAGE} BLOB,
                
                ${CipherTable.COLUMN_USERNAME} TEXT,
                ${CipherTable.COLUMN_PASSWORD} TEXT,
                ${CipherTable.COLUMN_WEBSITE} TEXT,
                ${CipherTable.COLUMN_NOTES} TEXT,
                ${CipherTable.COLUMN_TAG} TEXT,
                
                ${CipherTable.COLUMN_CUSTOM} TEXT,
                ${CipherTable.COLUMN_ATTACHMENT} BLOB,
                
                ${CipherTable.COLUMN_ADDTIME} TEXT,
                ${CipherTable.COLUMN_LASTCHANGETIME} TEXT
                )
            """

        private const val SQL_CREATE_CIPHERTEXT =
            """
                CREATE TABLE ${CipherText.TABLE_NAME} (
                ${BaseColumns._ID} INTEGER PRIMARY KEY,
                ${CipherText.COLUMN_TITLE} TEXT,
                ${CipherText.COLUMN_TEXT} TEXT,
                ${CipherText.COLUMN_ADDTIME} TEXT,
                ${CipherText.COLUMN_LASTCHANGETIME} TEXT
                )
            """

        private const val SQL_CREATE_CIPHERCARD =
            """
                CREATE TABLE ${CipherCard.TABLE_NAME} (
                ${BaseColumns._ID} INTEGER PRIMARY KEY,
                ${CipherCard.COLUMN_TITLE} TEXT,
                ${CipherCard.COLUMN_IMAGE} BLOB,
                
                ${CipherCard.COLUMN_CARDNUMBER} TEXT,
                ${CipherCard.COLUMN_CARDPASSWORD} TEXT,
                ${CipherCard.COLUMN_NOTES} TEXT,
                
                ${CipherCard.COLUMN_ADDTIME} TEXT,
                ${CipherCard.COLUMN_LASTCHANGETIME} TEXT
                )
            """

        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${CipherTable.TABLE_NAME}"
        private const val SQL_DELETE_CIPHERTEXT = "DROP TABLE IF EXISTS ${CipherText.TABLE_NAME}"
        private const val SQL_DELETE_CIPHERCARD = "DROP TABLE IF EXISTS ${CipherCard.TABLE_NAME}"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        
        db?.execSQL(SQL_CREATE_ENTRIES)
        db?.execSQL(SQL_CREATE_CIPHERTEXT)
        db?.execSQL(SQL_CREATE_CIPHERCARD)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

}