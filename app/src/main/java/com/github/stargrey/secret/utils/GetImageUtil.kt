package com.github.stargrey.secret.utils

import android.content.ContentResolver
import android.content.Intent
import com.github.stargrey.secret.bean.ImageData

fun getImageUtil(data: Intent?,contentResolver: ContentResolver): ImageData{
    val photoUri = data?.data
    val file = photoUri?.let { contentResolver.openInputStream(photoUri) }

    val fileBytes = file?.readBytes()
    val fileType = photoUri?.let { contentResolver.getType(photoUri) }
    var filename: String = ""
    var filesize: String = ""

    val cursor = photoUri?.let { contentResolver.query(photoUri, null, null, null, null, null) }

    with(cursor) {
        while (this?.moveToNext() == true) {
            filename = getString(getColumnIndexOrThrow(android.provider.MediaStore.Files.FileColumns.DISPLAY_NAME))
            filesize = getString(getColumnIndexOrThrow(android.provider.MediaStore.Files.FileColumns.SIZE))
        }
    }
    return ImageData(filename,fileType,filesize,fileBytes)
}