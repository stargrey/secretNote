package com.github.stargrey.secret.data

import android.content.ContentValues
import android.content.Context
import android.provider.BaseColumns
import net.sqlcipher.Cursor
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteException

object DatabaseHandle {
    val CipherTable = CipherDatabase.CipherTable
    val CipherNotes = CipherDatabase.CipherNotes
    val CipherCard = CipherDatabase.CipherCard

    lateinit var database: SQLiteDatabase
    lateinit var dbHelper: CipherDbHelper

    fun init(context: Context) {
        // 加载要用的资源库,不加载直接使用会出现错误
        SQLiteDatabase.loadLibs(context)
        dbHelper = CipherDbHelper(context)
        // database = dbHelper.getWritableDatabase(passwordHashResult)
    }

    fun openDataBase(passwordHashResult: String){
        database = dbHelper.getWritableDatabase(passwordHashResult)
    }

    fun closeDataBase() {
        database.close()
    }

    /*
     * 捕获数据库异常来判断密码是否正确
     * 在数据库已打开时该方法无效
     */
    fun verifyPassword(context: Context,verifyPassword: String): Boolean{
        SQLiteDatabase.loadLibs(context)
        dbHelper = CipherDbHelper(context)
        return try {
            database = dbHelper.getWritableDatabase(verifyPassword)
            true
        } catch (e: SQLiteException){
            false
        }
    }

    /*
     * 修改数据库密码
     */
    fun changePassword(newPassword: String) {
        database.changePassword(newPassword)
    }

    fun getAccountData(
        columns: Array<String>? = null,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        groupBy: String? = null,
        having: String? = null,
        orderBy: String = "ASC"
    ): Cursor {
        return database.query(
            CipherTable.TABLE_NAME, // 表名
            columns, // 查询的数据,传 null 获取所有列
            selection, // 传 null 表示获取所有行
            selectionArgs,  // 同上
            groupBy, // 将查到的行分组
            having, // 分组过滤
            orderBy // 排序方式
        )
    }

    // 依据 ID 获取单行所有数据
    fun getAccountDataByID(ID: String): Cursor {
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(ID)
        val softOrder = "${BaseColumns._ID} DESC"

        val cursor = database.query(
            CipherTable.TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            softOrder
        )
        return cursor
    }

    fun insertAccountData(values: ContentValues) {
        database.insert(CipherTable.TABLE_NAME, null, values)
    }

    fun updateAccountDataByID(ID: String, values: ContentValues) {
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = ID
        database.update(CipherTable.TABLE_NAME, values, selection, arrayOf(selectionArgs))
    }

    fun deleteAccountDataByID(ID: String) {
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = ID
        database.delete(CipherTable.TABLE_NAME, selection, arrayOf(selectionArgs))
    }



    fun getNotesData(
        columns: Array<String>? = null,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        groupBy: String? = null,
        having: String? = null,
        orderBy: String = "ASC"
    ): Cursor {
        // val projection = arrayOf(BaseColumns._ID, CipherText.COLUMN_TITLE, CipherText.COLUMN_TEXT, CipherText.COLUMN_ADDTIME, CipherText.COLUMN_LASTCHANGETIME)
        val softOrder = "${BaseColumns._ID} DESC"
        val cursor = database.query(
            CipherNotes.TABLE_NAME,
            columns,
            selection,
            selectionArgs,
            groupBy,
            having,
            orderBy
        )
        return cursor
    }

    fun getNotesDataByID(ID: String): Cursor {
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(ID)
        val softOrder = "${BaseColumns._ID} DESC"
        val cursor = database.query(
            CipherNotes.TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            softOrder
        )
        return cursor
    }

    fun insertNotesData(values: ContentValues){
        database.insert(CipherNotes.TABLE_NAME,null,values)
    }

    fun deleteNotesDataByID(ID: String) {
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = ID
        database.delete(CipherNotes.TABLE_NAME, selection, arrayOf(selectionArgs))
    }

    fun updateNotesDataByID(ID: String, values: ContentValues) {
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = ID

        database.update(
            CipherNotes.TABLE_NAME,
            values,
            selection,
            arrayOf(selectionArgs)
        )
    }



    fun getCardsData(
        columns: Array<String>? = null,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        groupBy: String? = null,
        having: String? = null,
        orderBy: String = "ASC"
    ): Cursor {
        return database.query(
            CipherCard.TABLE_NAME,
            columns,
            selection,
            selectionArgs,
            groupBy,
            having,
            orderBy
        )
    }

    fun getCardDataByID(ID: String): Cursor {
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(ID)
        val softOrder = "${BaseColumns._ID} DESC"
        val cursor = database.query(
            CipherCard.TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            softOrder
        )
        return cursor
    }

    fun insertCardData(values: ContentValues){
        database.insert(CipherCard.TABLE_NAME, null,values)
    }

    fun deleteCardDataByID(ID: String) {
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = ID
        database.delete(CipherCard.TABLE_NAME, selection, arrayOf(selectionArgs))
    }

    fun updateCardDataByID(ID: String, values: ContentValues) {
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = ID

        database.update(
            CipherCard.TABLE_NAME,
            values,
            selection,
            arrayOf(selectionArgs)
        )
    }

}