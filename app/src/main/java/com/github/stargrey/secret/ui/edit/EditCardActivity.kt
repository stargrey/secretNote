package com.github.stargrey.secret.ui.edit

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doAfterTextChanged
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.github.stargrey.secret.R
import com.github.stargrey.secret.common.Constants
import com.github.stargrey.secret.bean.CardData
import com.github.stargrey.secret.bean.ImageData
import com.github.stargrey.secret.utils.CipherDataUtils
import com.github.stargrey.secret.utils.GetSecureRandomStr
import com.github.stargrey.secret.utils.getImageUtil
import com.blankj.utilcode.util.TimeUtils
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.util.SmartGlideImageLoader
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class EditCardActivity : AppCompatActivity() {

    lateinit var ID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editcard)
        supportActionBar?.title = "编辑卡片"
        // 初始化各编辑框
        initEditText()

        ID = intent.getStringExtra("ID") ?: ""
        if (ID.isNotEmpty()) {
            initEditTextContent(ID)
        }

        // 设置保存按钮
        with(findViewById<Button>(R.id.addContentBtn)) {
            setOnClickListener {
                if (!titleEdittext.text.isNullOrEmpty() &&
                    !cardNumber.text.isNullOrEmpty()
                ) {
                    updateCardData()
                    val resultIntent = Intent()
                    resultIntent.putExtra(Constants.shouldRefreshList, true)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                } else {
                    Toast.makeText(this@EditCardActivity, "请输入标题和卡号", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private var waitToCloseDataBase : Job? = null
    override fun onPause() {
        super.onPause()
        waitToCloseDataBase = GlobalScope.launch {
            delay(3000)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        waitToCloseDataBase?.cancel()
    }

    override fun onBackPressed() {
        // TODO : 优化,未修改时直接返回
        MaterialDialog(this).show {
            message(text = "是否保存?")
            positiveButton(text = "保存") {
                if (!titleEdittext.text.isNullOrEmpty() &&
                    !cardNumber.text.isNullOrEmpty()
                ) {
                    updateCardData()
                    val resultIntent = Intent()
                    resultIntent.putExtra(Constants.shouldRefreshList, true)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                } else {
                    Toast.makeText(this@EditCardActivity, "请输入标题和卡号", Toast.LENGTH_SHORT).show()
                }
            }
            negativeButton(text = "取消") { super.onBackPressed() }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_editpwd_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> { onBackPressed();return true }
            R.id.action_attachment -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "image/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
                requestAttachment.launch(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private var imageViewList = ArrayList<ImageData>()

    private var requestAttachment = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK){
            var imageBean = getImageUtil(result.data, contentResolver)
            inflateAttachmentImageView(imageBean)
        }
    }
    /*
     * 添加 图片附件 的布局
     */
    private fun inflateAttachmentImageView(imageBean: ImageData, addImgViewToList: Boolean = true){
        if (addImgViewToList) {
            imageViewList.add(imageBean)
        }
        // 图片大小,如: 123KB
        val imgSizeInfo = run {
            var imgSizeNum = imageBean.size?.toDouble() ?: 0.0
            var sizeType = "B"
            if (imgSizeNum > 1000) {imgSizeNum /= 1000; sizeType = "KB"}
            if (imgSizeNum > 1000) {imgSizeNum /= 1000; sizeType = "MB"}
            String.format("%.2f",imgSizeNum) + sizeType
        }

        val imageLayout = layoutInflater.inflate(R.layout.item_image_layout,null)

        with(imageLayout){
            val imageView = findViewById<ImageView>(R.id.imageView)
            Glide.with(context).asDrawable().load(imageBean.bytes).into(imageView)
            // 点击图片浏览大图
            imageView.setOnClickListener {
                // 自带了 浏览大图 和 保存图片 的功能,有时间的话或许应该重做一个...
                XPopup.Builder(context).asImageViewer(imageView,imageBean.bytes,
                    SmartGlideImageLoader()
                )
                    .show()
            }
            // 设置及修改图片名称
            findViewById<EditText>(R.id.imageDesEditText).apply {
                setText("${imageBean.name}")
                doAfterTextChanged { text ->
                    imageBean.name = text.toString()
                }
            }
            // 设置图片大小
            findViewById<TextView>(R.id.imageSize).setText(imgSizeInfo)
            // 删除该 imageView
            findViewById<ImageButton>(R.id.deleteBtn).setOnClickListener {
                contentLinearLayout.removeView(imageLayout)
                imageViewList.remove(imageBean)
            }
        }
        contentLinearLayout.addView(imageLayout)
    }

    private lateinit var titleEdittext: EditText
    private lateinit var cardNumber: EditText
    private lateinit var cardPasswordLayout: ConstraintLayout
    private lateinit var cardPassword: TextInputEditText
    private lateinit var notesEditText: EditText
    private lateinit var contentLinearLayout: LinearLayout

    private fun updateCardData() {
        val cardData = CardData()
        cardData.title = titleEdittext.text.toString()
        cardData.cardnumber = cardNumber.text.toString()
        cardData.cardpassword = cardPassword.text.toString()
        cardData.notes = notesEditText.text.toString()

        cardData.image = Json.encodeToString(imageViewList)

        cardData.addtime = TimeUtils.getNowString()
        cardData.lastchangetime = TimeUtils.getNowString()

        // DatabaseHandle.insertCardData(cardData)
        CipherDataUtils.insertCardData(cardData)
    }

    private fun initEditText() {
        titleEdittext = findViewById(R.id.edittext_title)
        cardNumber = findViewById(R.id.editText_cardNumber)
        cardPasswordLayout = findViewById(R.id.edittextLayout_cardpassword)
        with(cardPasswordLayout){
            findViewById<TextView>(R.id.titleTV).setText("密码")
            findViewById<ImageButton>(R.id.imageBtn).apply {
                background = getDrawable(R.drawable.icon_random)
                setOnClickListener { showRandomPWDialog() }
            }
            cardPassword = findViewById(R.id.editText)
        }

        notesEditText = findViewById(R.id.edittext_note)

        contentLinearLayout = findViewById(R.id.content_LinearLayout)
    }

    private fun initEditTextContent(ID: String) {
        val cardData = CipherDataUtils.getCardDataByID(ID)
        titleEdittext.setText(cardData.title)
        cardNumber.setText(cardData.cardnumber)
        cardPassword.setText(cardData.cardpassword)
        notesEditText.setText(cardData.notes)

        cardData.image?.let {
            imageViewList = Json.decodeFromString(it)
        }
        imageViewList.forEach {
            inflateAttachmentImageView(it,false)
        }
    }


    // 显示密码的部件
    private lateinit var randomPasswdStr: EditText
    // 初始密码配置
    private var passwordConfig = 2
    private var excludeConfusion = true
    private var passwordLength = 6
    private fun generatePasswd(){
        randomPasswdStr.setText(GetSecureRandomStr
            .randomString(passwordLength,passwordConfig,excludeConfusion))
    }

    /*
     * 弹出 随机生成密码 弹窗
     */
    private fun showRandomPWDialog(){
        MaterialDialog(this).show {
            customView(R.layout.dialog_securerandom)
            // 设置点击弹窗外不关闭
            cancelOnTouchOutside(false)

            val seekBar = findViewById<RangeSeekBar>(R.id.seekbar_dialog)
            // 设置默认密码位数
            seekBar.setProgress(passwordLength.toFloat())
            // 设置指示器初始值为整数
            seekBar.leftSeekBar.setIndicatorText(seekBar.leftSeekBar.progress.toInt().toString())
            // 初始化并填入一个随机密码
            randomPasswdStr = findViewById(R.id.randomStr_dialog)
            generatePasswd()

            // 设置各按钮点击事件
            findViewById<ImageButton>(R.id.reRandomBtn_dialog).setOnClickListener {
                generatePasswd()
            }
            findViewById<Button>(R.id.cancelBtn_dialog).setOnClickListener {
                dismiss()
            }
            findViewById<Button>(R.id.confirmBtn_dialog).setOnClickListener {
                cardPassword.text = randomPasswdStr.text
                dismiss()
                // TODO 复制到剪切板
            }
            // 设置各复选框点击事件
            findViewById<CheckBox>(R.id.uppercase_checkBox).apply {
                isChecked = passwordConfig and 8 != 0
                setOnClickListener {
                    passwordConfig = passwordConfig.xor(8)
                    // 如果这是唯一勾选的项,且取消勾选时: 取消这次操作,提示 "至少选择一项"
                    if (passwordConfig == 0){
                        passwordConfig = passwordConfig.xor(8)
                        isChecked = !isChecked
                        Toast.makeText(this@EditCardActivity,"至少选择一项",Toast.LENGTH_SHORT).show()
                    } else {
                        generatePasswd()
                    }
                }
            }
            findViewById<CheckBox>(R.id.lowercase_checkBox).apply {
                isChecked = passwordConfig and 4 != 0
                setOnClickListener {
                    passwordConfig = passwordConfig.xor(4)
                    if (passwordConfig == 0){
                        passwordConfig = passwordConfig.xor(4)
                        isChecked = !isChecked
                        Toast.makeText(this@EditCardActivity,"至少选择一项",Toast.LENGTH_SHORT).show()
                    } else {
                        generatePasswd()
                    }
                }
            }
            findViewById<CheckBox>(R.id.numeric_checkBox).apply {
                isChecked = passwordConfig and 2 != 0
                setOnClickListener {
                    passwordConfig = passwordConfig.xor(2)

                    if (passwordConfig == 0){
                        passwordConfig = passwordConfig.xor(2)
                        isChecked = !isChecked
                        Toast.makeText(this@EditCardActivity,"至少选择一项",Toast.LENGTH_SHORT).show()
                    } else {
                        generatePasswd()
                    }
                }
            }
            findViewById<CheckBox>(R.id.spcial_checkBox).apply {
                isChecked = passwordConfig and 1 != 0
                setOnClickListener {
                    passwordConfig = passwordConfig.xor(1)

                    if (passwordConfig == 0){
                        passwordConfig = passwordConfig.xor(1)
                        isChecked = !isChecked
                        Toast.makeText(this@EditCardActivity,"至少选择一项",Toast.LENGTH_SHORT).show()
                    } else {
                        generatePasswd()
                    }
                }
            }
            findViewById<CheckBox>(R.id.exclude_checkBox).apply {
                isChecked = excludeConfusion
                setOnClickListener {
                    excludeConfusion = !excludeConfusion
                    generatePasswd()
                }
            }
            // 滑动设置条时生成随机密码
            seekBar.setOnRangeChangedListener(object : OnRangeChangedListener {
                var tempLeftValue = -1
                override fun onRangeChanged(
                    view: RangeSeekBar?,
                    leftValue: Float,
                    rightValue: Float,
                    isFromUser: Boolean
                ) {
                    if(tempLeftValue != leftValue.toInt()) {
                        tempLeftValue = leftValue.toInt()
                        passwordLength = seekBar.leftSeekBar.progress.toInt()
                        generatePasswd()
                    }
                    seekBar.leftSeekBar.setIndicatorText(seekBar.leftSeekBar.progress.toInt().toString())
                }
                // 这两个为开始滑动和结束滑动的事件,这里不需要
                override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {}
                override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {}

            })
        }
    }

}