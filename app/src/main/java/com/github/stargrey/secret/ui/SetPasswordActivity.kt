package com.github.stargrey.secret.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.github.stargrey.secret.R
import com.github.stargrey.secret.common.Constants
import com.github.stargrey.secret.data.DatabaseHandle
import com.github.stargrey.secret.utils.GetSecureRandomStr
import com.github.stargrey.secret.utils.PasswordUtil


class SetPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setpassword)
        supportActionBar?.title = "设置密码"
        initEncryptSharePrefs()
        initLayout()
    }

    private lateinit var setPasswordEditText : EditText
    private lateinit var confirmPasswordEditText : EditText
    private lateinit var setPasswordPromptEditText : EditText
    private fun initLayout(){
        setPasswordEditText = findViewById(R.id.setPasswordEdittext)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEdittext)
        setPasswordPromptEditText = findViewById(R.id.setPasswordPromptEdittext)

        val isChangePassword = intent.getBooleanExtra("isChangePassword",false)
        if (isChangePassword) {
            getSharedPreferences(Constants.user_setting, MODE_PRIVATE).run {
                setPasswordPromptEditText.setText(
                this.getString("passwordPrompt",""))
            }
        }

        findViewById<Button>(R.id.doneBtn).setOnClickListener {
            if (setPasswordEditText.text.isNullOrEmpty()){
                Toast.makeText(this,"请输入密码",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (confirmPasswordEditText.text.isNullOrEmpty()) {
                Toast.makeText(this,"请确认密码",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (setPasswordEditText.text.toString() != confirmPasswordEditText.text.toString()){
                Toast.makeText(this,"两次输入的密码不同,请重新输入",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // TODO 加入对密码字符的校验,如密码不应包含空格等等
            applyPassword(isChangePassword)

            val resultIntent = Intent()
            setResult(RESULT_OK,resultIntent)
            finish()

        }
    }

    private fun applyPassword(isChangePassword: Boolean) {
        val password = setPasswordEditText.text.toString()
        val salt = GetSecureRandomStr.randomString(8,15,false)
        val passwordHashResult = PasswordUtil.getHashResult(password, salt)

        // 将 编码后的哈希 写入到文件中
        with(encryptSharedPrefs.edit()){
            // passwordHashResult.encodedOutputAsString() : 编码后的字符串,字符串中包含有:
            // argon2 的配置信息; 盐 ; 哈希结果
            putString("passwordHashResult",passwordHashResult.encodedOutputAsString())
            putString("salt",salt)
            apply()
        }

        if (isChangePassword) DatabaseHandle.changePassword(passwordHashResult.encodedOutputAsString())

        // 将 已经设置密码 写入到文件中
        getSharedPreferences(Constants.app_config, MODE_PRIVATE).edit()
            .putBoolean("hasSetPassword",true)
            .apply()
        // 将 密码提示 写入到文件中
        getSharedPreferences(Constants.user_setting, MODE_PRIVATE).edit()
            .putString("passwordPrompt",setPasswordPromptEditText.text.toString())
            .apply()

    }

    private lateinit var encryptSharedPrefs: SharedPreferences
    private fun initEncryptSharePrefs() {
        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        val mainKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

        val sharedPrefsFile: String = "encryptedSharedPres"
        encryptSharedPrefs = EncryptedSharedPreferences.create(
            sharedPrefsFile,
            mainKeyAlias,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}