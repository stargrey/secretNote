package com.github.stargrey.secret.bean

data class NotesData(
    var ID: String?,
    var title:String?,
    var content:String?,
    var addTime: String?,
    var lastChangeTime: String?
) {
    constructor() : this(null,null,null,null,null)
}