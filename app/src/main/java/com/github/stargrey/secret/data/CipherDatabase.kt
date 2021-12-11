package com.github.stargrey.secret.data

import android.provider.BaseColumns

object CipherDatabase {
    // 存储账号和密码
    object CipherTable : BaseColumns {
        const val TABLE_NAME = "ciphertable"

        const val COLUMN_TITLE = "title" // 存储的标题
        const val COLUMN_IMAGE = "image" // 存储 应用或网站 的icon(如果有)

        const val COLUMN_USERNAME = "username" // 账号名
        const val COLUMN_PASSWORD = "password" // 密码
        const val COLUMN_WEBSITE = "website" // 账号密码对应的网站
        const val COLUMN_NOTES = "notes" // 备注
        const val COLUMN_TAG = "tag" // 标签

        const val COLUMN_CUSTOM = "custom" // 用户自定义字段,如 注册邮箱 : example@example.com
        const val COLUMN_ATTACHMENT = "attachment" // 附件,比如:图片,文档;使用 base64 转存

        const val COLUMN_ADDTIME = "addtime" // 添加时间
        const val COLUMN_LASTCHANGETIME = "lastchangetime" // 最后修改时间

    }

    // 存储文本记录
    object CipherNotes : BaseColumns {
        const val TABLE_NAME = "ciphertext"

        const val COLUMN_TITLE = "title" // 标题
        const val COLUMN_TEXT = "text" // 内容

        const val COLUMN_ADDTIME = "addtime" // 添加时间
        const val COLUMN_LASTCHANGETIME = "lastchangetime" // 最后修改时间
    }

    // 存储卡包,例如银行卡,身份证等
    object CipherCard : BaseColumns {
        const val TABLE_NAME = "ciphercard"

        const val COLUMN_TITLE = "title" // 卡名
        const val COLUMN_IMAGE = "image" // 卡图

        const val COLUMN_CARDNUMBER = "cardnumber" // 卡号
        const val COLUMN_CARDPASSWORD = "cardpassword" // 密码
        const val COLUMN_NOTES = "notes" // 备注

        const val COLUMN_ADDTIME = "addtime" // 添加时间
        const val COLUMN_LASTCHANGETIME = "lastchangetime" // 最后修改时间
    }
}