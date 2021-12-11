package com.github.stargrey.secret.ui

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.github.stargrey.secret.R
import com.github.stargrey.secret.common.Constants
import com.github.stargrey.secret.data.DatabaseHandle
import com.github.stargrey.secret.data.SettingData
import com.github.stargrey.secret.utils.InstalledApp
import com.github.stargrey.secret.utils.PasswordUtil
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.lxj.xpopup.XPopup
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

class StartActivity : AppCompatActivity() {
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private lateinit var appConfigSharedPrefs : SharedPreferences
    private lateinit var userSettingSharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        // 隐藏 ActionBar,以及将布局填充到全屏并设置状态栏透明
        supportActionBar?.hide()
        // 设置状态栏透明;布局填充全屏
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        window.statusBarColor = Color.TRANSPARENT

        DatabaseHandle.init(applicationContext)

        appConfigSharedPrefs = getSharedPreferences(Constants.app_config, MODE_PRIVATE)
        userSettingSharedPrefs = getSharedPreferences(Constants.user_setting, MODE_PRIVATE)
        // 是否是第一次进入应用
        var isFirstEnter = appConfigSharedPrefs.getBoolean("isFirstEnter",true)
        // TODO 加入应用介绍页面

        initEncryptSharePrefs()

        initLayout()

        GlobalScope.launch {
            InstalledApp.initInstalledAppData()
        }
        // 检查是否已经设置了密码
        val hasSetPassword = appConfigSharedPrefs.getBoolean("hasSetPassword",false)
        if (!hasSetPassword){
            requestPassword()
        }

        // 如果已经设置密码且启动指纹验证,则验证指纹
        if (hasSetPassword &&
            userSettingSharedPrefs.getBoolean(SettingData.UseFingerprint.name,SettingData.UseFingerprint.defaultSwitch)) {
            verifyFingerprint()
        }

    }

    private lateinit var inputPasswordLayout: TextInputLayout
    private lateinit var inputPasswordEditText: TextInputEditText
    private lateinit var confirmBtn: MaterialButton
    private lateinit var useFingerprintBtn: MaterialButton
    private fun initLayout(){
        inputPasswordLayout = findViewById(R.id.inputPasswordLayout)

        inputPasswordEditText = findViewById(R.id.inputPasswordEdittext)


        findViewById<TextView>(R.id.showPasswordHint).setOnClickListener {
            XPopup.Builder(this).asConfirm("密码提示",userSettingSharedPrefs.getString("passwordPrompt",""),{}).show()
        }

        confirmBtn = findViewById(R.id.confirmPasswordBtn)
        confirmBtn.apply {
            setOnClickListener {
                if (inputPasswordEditText.text.toString().isNotEmpty())
                    verifyPassWord(inputPasswordEditText.text.toString())
                else
                    Toast.makeText(context,"请输入密码",Toast.LENGTH_SHORT).show()
            }
        }

        useFingerprintBtn = findViewById(R.id.useFingerprintBtn)
        useFingerprintBtn.apply {
            if (!userSettingSharedPrefs.getBoolean(SettingData.UseFingerprint.name,
                    SettingData.UseFingerprint.defaultSwitch))
            {
                this.visibility = View.INVISIBLE
            }
            setOnClickListener {
                verifyFingerprint()
            }
        }

        val backGround = findViewById<ImageView>(R.id.backGround)
        val inputStream = assets.open("Light_Sky_Blue_BG.png")
        backGround.setImageDrawable( Drawable.createFromStream(inputStream,"backGround"))
        backGround.scaleType = ImageView.ScaleType.CENTER_CROP
        inputStream.close()
    }


    /*
     * 如果未设置密码,跳转到设置密码界面,获取密码后保存密码
     */
    private fun requestPassword(){
        val requestPassword = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK){
                onVerifySucceeded()
            }
        }
        val intent = Intent(this,SetPasswordActivity::class.java)
        requestPassword.launch(intent)
    }

    private lateinit var encryptSharedPrefs: SharedPreferences
    private fun initEncryptSharePrefs() {
        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        val mainKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

        val sharedPrefsFile = "encryptedSharedPres"
        encryptSharedPrefs = EncryptedSharedPreferences.create(
            sharedPrefsFile,
            mainKeyAlias,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun onVerifySucceeded(){
        val passwordHashResult = encryptSharedPrefs
            .getString("passwordHashResult",null) ?: throw Throwable("passwordHashResult is null")
        DatabaseHandle.openDataBase(passwordHashResult)
        val intent = Intent(this@StartActivity, MainActivity::class.java)
        startActivity(intent)
        this@StartActivity.finish()
    }

    private fun onVerifyFailed(){
        // TODO 完善这个方法
        Toast.makeText(applicationContext,"密码错误",Toast.LENGTH_SHORT).show()
    }

    private fun verifyFingerprint(){
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this,executor,
        object  : BiometricPrompt.AuthenticationCallback(){
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onVerifySucceeded()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(this@StartActivity,"取消验证",Toast.LENGTH_SHORT).show()
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("指纹验证")
            .setSubtitle("使用指纹验证登录")
            .setNegativeButtonText("使用密码登录")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun verifyPassWord(verifyPassword: String){
        val verificationResult = PasswordUtil.verifyPassWordByDB(applicationContext,verifyPassword)

        if (verificationResult){
            onVerifySucceeded()
        } else {
            onVerifyFailed()
        }
    }

}