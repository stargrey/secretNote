package com.github.stargrey.secret.bean

data class CipherData(
    var ID: String?,

    var TITLE: String,
    var IMAGE: ByteArray?,

    var USERNAME: String,
    var PASSWORD: String?,
    var WEBSITE: String?,
    var NOTES: String?,
    var TAG: String?,

    var CUSTOM: String?,
    var ATTACHMENT:String?,

    var ADDTIME: String?,
    var LASTCHANGETIME: String?

){
    constructor() : this(null,"defaultTitle",null,
        "defaultUsername",null,null,
        null,null,null,null,null,null)
}
