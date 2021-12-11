package com.github.stargrey.secret.utils

import android.content.ContentValues
import android.provider.BaseColumns
import com.github.stargrey.secret.data.CipherDatabase
import com.github.stargrey.secret.data.DatabaseHandle
import com.github.stargrey.secret.bean.CardData
import com.github.stargrey.secret.bean.CipherData
import com.github.stargrey.secret.bean.NotesData

object CipherDataUtils {
    val CipherTable = CipherDatabase.CipherTable
    val CipherNotes = CipherDatabase.CipherNotes
    val CipherCard = CipherDatabase.CipherCard


    fun getAccountDatas(
        columns: Array<String>? = null,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        groupBy: String? = null,
        having: String? = null,
        orderBy: String = "ASC"
    ): MutableList<Any> {
        val cursor = DatabaseHandle.getAccountData(
            columns,
            selection,
            selectionArgs,
            groupBy,
            having,
            orderBy
        )
        val items = mutableListOf<Any>()
        with(cursor) {
            while (moveToNext()) {
                val cipherData = CipherData()

                cipherData.ID = getString(getColumnIndexOrThrow(BaseColumns._ID))
                cipherData.TITLE = getString(getColumnIndexOrThrow(CipherTable.COLUMN_TITLE))
                cipherData.IMAGE = getBlob(getColumnIndexOrThrow(CipherTable.COLUMN_IMAGE))

                cipherData.USERNAME = getString(getColumnIndexOrThrow(CipherTable.COLUMN_USERNAME))
                cipherData.PASSWORD = getString(getColumnIndexOrThrow(CipherTable.COLUMN_PASSWORD))
                cipherData.WEBSITE = getString(getColumnIndexOrThrow(CipherTable.COLUMN_WEBSITE))
                cipherData.NOTES = getString(getColumnIndexOrThrow(CipherTable.COLUMN_NOTES))
                cipherData.TAG = getString(getColumnIndexOrThrow(CipherTable.COLUMN_TAG))

                cipherData.CUSTOM = getString(getColumnIndexOrThrow(CipherTable.COLUMN_CUSTOM))
                cipherData.ATTACHMENT =
                    getString(getColumnIndexOrThrow(CipherTable.COLUMN_ATTACHMENT))

                cipherData.ADDTIME = getString(getColumnIndexOrThrow(CipherTable.COLUMN_ADDTIME))
                cipherData.LASTCHANGETIME =
                    getString(getColumnIndexOrThrow(CipherTable.COLUMN_LASTCHANGETIME))

                items.add(cipherData)
            }
        }
        return items
    }

    fun getAccountDataByID(ID: String): CipherData {
        val cursor = DatabaseHandle.getAccountDataByID(ID)
        val cipherData = CipherData()

        with(cursor) {
            while (moveToNext()) {
                cipherData.ID = getString(getColumnIndexOrThrow(BaseColumns._ID))
                cipherData.TITLE = getString(getColumnIndexOrThrow(CipherTable.COLUMN_TITLE))
                cipherData.IMAGE = getBlob(getColumnIndexOrThrow(CipherTable.COLUMN_IMAGE))

                cipherData.USERNAME = getString(getColumnIndexOrThrow(CipherTable.COLUMN_USERNAME))
                cipherData.PASSWORD = getString(getColumnIndexOrThrow(CipherTable.COLUMN_PASSWORD))
                cipherData.WEBSITE = getString(getColumnIndexOrThrow(CipherTable.COLUMN_WEBSITE))
                cipherData.NOTES = getString(getColumnIndexOrThrow(CipherTable.COLUMN_NOTES))
                cipherData.TAG = getString(getColumnIndexOrThrow(CipherTable.COLUMN_TAG))

                cipherData.CUSTOM = getString(getColumnIndexOrThrow(CipherTable.COLUMN_CUSTOM))
                cipherData.ATTACHMENT =
                    getString(getColumnIndexOrThrow(CipherTable.COLUMN_ATTACHMENT))

                cipherData.ADDTIME = getString(getColumnIndexOrThrow(CipherTable.COLUMN_ADDTIME))
                cipherData.LASTCHANGETIME =
                    getString(getColumnIndexOrThrow(CipherTable.COLUMN_LASTCHANGETIME))
            }
        }
        return cipherData
    }


    fun insertAccountData(cipherData: CipherData) {
        val values = ContentValues().apply {
            put(CipherTable.COLUMN_TITLE, cipherData.TITLE)
            put(CipherTable.COLUMN_IMAGE, cipherData.IMAGE)

            put(CipherTable.COLUMN_USERNAME, cipherData.USERNAME)
            put(CipherTable.COLUMN_PASSWORD, cipherData.PASSWORD)
            put(CipherTable.COLUMN_WEBSITE, cipherData.WEBSITE)
            put(CipherTable.COLUMN_NOTES, cipherData.NOTES)
            put(CipherTable.COLUMN_TAG, cipherData.TAG)

            put(CipherTable.COLUMN_CUSTOM, cipherData.CUSTOM)
            put(CipherTable.COLUMN_ATTACHMENT, cipherData.ATTACHMENT)

            put(CipherTable.COLUMN_ADDTIME, cipherData.ADDTIME)
            put(CipherTable.COLUMN_LASTCHANGETIME, cipherData.LASTCHANGETIME)
        }
        DatabaseHandle.insertAccountData(values)
    }


    fun updateAccountDataByID(ID: String, cipherData: CipherData) {
        val values = ContentValues().apply {
            put(CipherTable.COLUMN_TITLE, cipherData.TITLE)
            put(CipherTable.COLUMN_IMAGE, cipherData.IMAGE)

            put(CipherTable.COLUMN_USERNAME, cipherData.USERNAME)
            put(CipherTable.COLUMN_PASSWORD, cipherData.PASSWORD)
            put(CipherTable.COLUMN_WEBSITE, cipherData.WEBSITE)
            put(CipherTable.COLUMN_NOTES, cipherData.NOTES)
            put(CipherTable.COLUMN_TAG, cipherData.TAG)

            put(CipherTable.COLUMN_CUSTOM, cipherData.CUSTOM)
            put(CipherTable.COLUMN_ATTACHMENT, cipherData.ATTACHMENT)

            // put(CipherTable.COLUMN_ADDTIME, cipherData.ADDTIME)
            put(CipherTable.COLUMN_LASTCHANGETIME, cipherData.LASTCHANGETIME)
        }
        DatabaseHandle.updateAccountDataByID(ID, values)
    }

    fun deleteAccountDataByID(ID: String) {
        DatabaseHandle.deleteAccountDataByID(ID)
    }



    fun getNotesData(
        columns: Array<String>? = null,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        groupBy: String? = null,
        having: String? = null,
        orderBy: String = "ASC"
    ): MutableList<Any> {
        var cursor = DatabaseHandle.getNotesData(columns, selection, selectionArgs, groupBy, having, orderBy)

        val items = mutableListOf<Any>()
        with(cursor) {
            while (moveToNext()) {
                val ID = getString(getColumnIndexOrThrow(BaseColumns._ID))
                val title = getString(getColumnIndexOrThrow(CipherNotes.COLUMN_TITLE))
                val content = getString(getColumnIndexOrThrow(CipherNotes.COLUMN_TEXT))
                val addTime = getString(getColumnIndexOrThrow(CipherNotes.COLUMN_ADDTIME))
                val lastChangeTime =
                    getString(getColumnIndexOrThrow(CipherNotes.COLUMN_LASTCHANGETIME))

                val item = NotesData(ID, title, content, addTime, lastChangeTime)
                items.add(item)
            }
        }
        return items
    }

    fun getNotesDataByID(ID: String): NotesData {
        val cursor = DatabaseHandle.getNotesDataByID(ID)
        val notesData = NotesData()

        with(cursor) {
            while (moveToNext()) {
                notesData.ID = getString(getColumnIndexOrThrow(BaseColumns._ID))
                notesData.title = getString(getColumnIndexOrThrow(CipherNotes.COLUMN_TITLE))
                notesData.content = getString(getColumnIndexOrThrow(CipherNotes.COLUMN_TEXT))
                notesData.addTime = getString(getColumnIndexOrThrow(CipherNotes.COLUMN_ADDTIME))
                notesData.lastChangeTime =
                    getString(getColumnIndexOrThrow(CipherNotes.COLUMN_LASTCHANGETIME))
            }
        }
        return notesData
    }

    fun insertNoteData(notesData: NotesData) {
        val values = ContentValues().apply {
            put(DatabaseHandle.CipherNotes.COLUMN_TITLE, notesData.title)
            put(DatabaseHandle.CipherNotes.COLUMN_TEXT, notesData.content)
            put(DatabaseHandle.CipherNotes.COLUMN_ADDTIME, notesData.addTime)
            put(DatabaseHandle.CipherNotes.COLUMN_LASTCHANGETIME, notesData.lastChangeTime)
        }
        DatabaseHandle.insertNotesData(values)
    }

    fun deleteNoteDataByID(ID: String) {
        DatabaseHandle.deleteNotesDataByID(ID)
    }

    fun updateNotesDataByID(ID: String, notesData: NotesData) {
        val values = ContentValues().apply {
            put(CipherNotes.COLUMN_TITLE, notesData.title)
            put(CipherNotes.COLUMN_TEXT, notesData.content)
            put(CipherNotes.COLUMN_LASTCHANGETIME, notesData.lastChangeTime)
        }
        DatabaseHandle.updateNotesDataByID(ID, values)
    }



    fun getCardsData(
        columns: Array<String>? = null,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        groupBy: String? = null,
        having: String? = null,
        orderBy: String = "ASC"
    ): MutableList<Any> {
        val cursor =
            DatabaseHandle.getCardsData(columns, selection, selectionArgs, groupBy, having, orderBy)
        val items = mutableListOf<Any>()
        with(cursor) {
            while (moveToNext()) {
                val ID = getString(getColumnIndexOrThrow(BaseColumns._ID))
                val title = getString(getColumnIndexOrThrow(CipherCard.COLUMN_TITLE))
                val image = getString(getColumnIndexOrThrow(CipherCard.COLUMN_IMAGE))

                val cardnumber = getString(getColumnIndexOrThrow(CipherCard.COLUMN_CARDNUMBER))
                val cardpassword = getString(getColumnIndexOrThrow(CipherCard.COLUMN_CARDPASSWORD))
                val notes = getString(getColumnIndexOrThrow(CipherCard.COLUMN_NOTES))

                val addtime = getString(getColumnIndexOrThrow(CipherCard.COLUMN_ADDTIME))
                val lastchangetime =
                    getString(getColumnIndexOrThrow(CipherCard.COLUMN_LASTCHANGETIME))

                val item = CardData(
                    ID,
                    title,
                    image,
                    cardnumber,
                    cardpassword,
                    notes,
                    addtime,
                    lastchangetime
                )
                items.add(item)
            }
        }
        return items
    }

    fun getCardDataByID(ID: String): CardData {
        val cursor = DatabaseHandle.getCardDataByID(ID)
        val cardData = CardData()

        with(cursor) {
            while (moveToNext()) {
                cardData.ID = getString(getColumnIndexOrThrow(BaseColumns._ID))
                cardData.title = getString(getColumnIndexOrThrow(CipherCard.COLUMN_TITLE))
                cardData.image = getString(getColumnIndexOrThrow(CipherCard.COLUMN_IMAGE))

                cardData.cardnumber = getString(getColumnIndexOrThrow(CipherCard.COLUMN_CARDNUMBER))
                cardData.cardpassword =
                    getString(getColumnIndexOrThrow(CipherCard.COLUMN_CARDPASSWORD))

                cardData.notes = getString(getColumnIndexOrThrow(CipherCard.COLUMN_NOTES))
                cardData.addtime = getString(getColumnIndexOrThrow(CipherCard.COLUMN_ADDTIME))
                cardData.lastchangetime =
                    getString(getColumnIndexOrThrow(CipherCard.COLUMN_LASTCHANGETIME))
            }
        }
        return cardData
    }

    fun insertCardData(cardData: CardData) {
        val values = ContentValues().apply {
            put(CipherCard.COLUMN_TITLE, cardData.title)
            put(CipherCard.COLUMN_IMAGE, cardData.image)

            put(CipherCard.COLUMN_CARDNUMBER, cardData.cardnumber)
            put(CipherCard.COLUMN_CARDPASSWORD, cardData.cardpassword)
            put(CipherCard.COLUMN_NOTES, cardData.notes)

            put(CipherCard.COLUMN_ADDTIME, cardData.addtime)
            put(CipherCard.COLUMN_LASTCHANGETIME, cardData.lastchangetime)
        }
        DatabaseHandle.insertCardData(values)
    }

    fun deleteCardDataByID(ID: String) {
        DatabaseHandle.deleteCardDataByID(ID)
    }

    fun updateCardDataByID(ID: String, cardData: CardData) {
        val values = ContentValues().apply {
            put(CipherCard.COLUMN_TITLE, cardData.title)
            put(CipherCard.COLUMN_IMAGE, cardData.image)
            put(CipherCard.COLUMN_CARDNUMBER, cardData.cardnumber)
            put(CipherCard.COLUMN_CARDPASSWORD, cardData.cardpassword)
            put(CipherCard.COLUMN_NOTES, cardData.notes)
            put(CipherCard.COLUMN_LASTCHANGETIME, cardData.lastchangetime)
        }
        DatabaseHandle.updateCardDataByID(ID, values)
    }
}