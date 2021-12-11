package com.github.stargrey.secret.utils

import java.security.SecureRandom

object GetSecureRandomStr {
    const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
    const val NUMERIC = "0123456789"
    // 特殊字符
    const val SPECIAL = "!@#$%^&*_-+=<>?/"
    // 易混淆字符
    const val CONFUSION = "ilIL10oO"
    /*
     * 生成一个安全的随机密码
     * length : 生成密码长度
     * config : 密码包含的字符集, config 必须在 0~15 之间
     * excludeConfusion : 排除易混淆字符
     */
    fun randomString(length: Int, config: Int, excludeConfusion: Boolean): String{
        var symbols = ""
        // 依据配置添加字符串包含的字符集
        var tempConfig = config
        while (tempConfig != 0){
            when {
                tempConfig !in 1..15 -> throw IllegalArgumentException("config must be in 1~15")
                (tempConfig and 8) != 0 -> {symbols += UPPERCASE; tempConfig = tempConfig xor 8}
                (tempConfig and 4) != 0 -> {symbols += LOWERCASE; tempConfig = tempConfig xor 4}
                (tempConfig and 2) != 0 -> {symbols += NUMERIC; tempConfig = tempConfig xor 2}
                (tempConfig and 1) != 0 -> {symbols += SPECIAL; tempConfig = tempConfig xor 1}
            }
        }

        var randomString = ""
        val secureRandom = SecureRandom()

        while (randomString.length < length){
            var tempLetter = symbols[secureRandom.nextInt(symbols.length)]
            if (excludeConfusion){
                while (CONFUSION.contains(tempLetter)){
                    tempLetter = symbols[secureRandom.nextInt(symbols.length)]
                }
            }
            randomString += tempLetter
        }
        return randomString
    }
}