package com.github.stargrey.secret.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.github.stargrey.secret.data.DatabaseHandle
import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2KtResult
import com.lambdapioneer.argon2kt.Argon2Mode

object PasswordUtil {
    private val argon2Kt = Argon2Kt()

    /*
     * 获取 密码+盐 的哈希
     * 注意,请使用专门的 "密码哈希算法" 而不是 "哈希算法"
     */
    fun getHashResult(password : String, salt : String) : Argon2KtResult {
        /*
         * Argon2 的配置参数
         * 请在了解这些参数的情况下才对其修改
         *
         * 依据 Argon2Kt 作者博客,在 Google Pixel 3 (CPU : 骁龙845) 上, 以下参数
         * mode = Argon2Mode.ARGON2_ID, tCostInIterations = 1, mCostInKibibyte = 37888
         * 约花费大约 60 毫秒时间
         */
        return argon2Kt.hash(
            mode = Argon2Mode.ARGON2_ID,
            password = password.toByteArray(),
            salt = salt.toByteArray(),
            tCostInIterations = 5,
            mCostInKibibyte = 65536,
            parallelism = 2,
            hashLengthInBytes = 32
        )
    }
    /*
     * 验证密码是否正确
     * verifyPassword : 待验证密码
     * password : 正确密码
     */
    fun verifyPassWord(password: String,verifyPassword: String): Boolean{

        val verificationResult : Boolean = argon2Kt.verify(
            mode = Argon2Mode.ARGON2_ID,
            encoded = password,
            password = verifyPassword.toByteArray()
        )
        return verificationResult

    }

 /*
  * 验证密码是否正确
  * context
  * verifyPassword : 待验证密码
  */
    fun verifyPassWord(context: Context,verifyPassword: String): Boolean{
        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        val mainKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

        val sharedPrefsFile: String = "encryptedSharedPres"
        val encryptSharedPrefs = EncryptedSharedPreferences.create(
            sharedPrefsFile,
            mainKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val passwordHashResult = encryptSharedPrefs
            .getString("passwordHashResult",null) ?: throw Throwable("password is null")

        return verifyPassWord(passwordHashResult,verifyPassword)
    }

    /*
     * 捕获数据库异常来验证密码是否正确,使用该方法无需 密码哈希结果 , 即无需将 密码哈希结果 存储在设备上
     */
    fun verifyPassWordByDB(context: Context,verifyPassword: String): Boolean {
        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        val mainKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

        val sharedPrefsFile: String = "encryptedSharedPres"
        val encryptSharedPrefs = EncryptedSharedPreferences.create(
            sharedPrefsFile,
            mainKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val salt = encryptSharedPrefs
            .getString("salt",null) ?: throw Throwable("salt is null")

        val hashResult = getHashResult(verifyPassword,salt)
        return DatabaseHandle.verifyPassword(context,hashResult.encodedOutputAsString())
    }

}