package com.github.stargrey.secret.bean

import kotlinx.serialization.Serializable

@Serializable
data class ImageData(
    var name: String? = null,
    var type: String? = null,
    var size: String? = null,
    var bytes: ByteArray? = null
)
