package com.github.stargrey.secret.bean

data class CardData(
    var ID: String?,
    var title: String?,
    var image: String?,

    var cardnumber: String?,
    var cardpassword: String?,

    var notes: String?,
    var addtime: String?,
    var lastchangetime: String?
){
    constructor(): this(null,null,null,null,null,null,null,null)
}
