package com.github.stargrey.secret.ui.edit

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toBitmap
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.github.stargrey.secret.R
import com.github.stargrey.secret.bean.CipherData
import com.github.stargrey.secret.bean.ImageData
import com.github.stargrey.secret.utils.CipherDataUtils
import com.github.stargrey.secret.utils.GetSecureRandomStr
import com.github.stargrey.secret.utils.InstalledApp
import com.github.stargrey.secret.utils.getImageUtil
import com.blankj.utilcode.util.*
import com.bumptech.glide.Glide
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.util.SmartGlideImageLoader
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList

class EditPasswdActivity : AppCompatActivity() {

    // 各个编辑框的布局
    private lateinit var contentLinearLayout: LinearLayout
    private lateinit var titleEditLayout: ConstraintLayout
    private lateinit var usernameEditLayout: ConstraintLayout
    private lateinit var passwordEditLayout: ConstraintLayout
    private lateinit var websiteEditLayout: ConstraintLayout
    private lateinit var notesEditLayout: ConstraintLayout

    private lateinit var cipherData: CipherData

    private lateinit var appsInfo : List<AppUtils.AppInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editpassword)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "编辑账号"

        // 初始化各编辑框名字
        initTextViewName()

        // 判断是否修改已存储的密码,如果是:初始化各编辑框内文字
        if (intent.getBooleanExtra("isUpdata",false)){
            intent.getStringExtra("ID")?.let {
                cipherData = CipherDataUtils.getAccountDataByID(it)
                initEditTextContent() }
        }

        // 设置保存按钮点击事件
        val button = findViewById<Button>(R.id.addContentBtn)
        button.setOnClickListener {
            // 仅在标题和用户名不为空时保存
            if (!titleEditLayout.findViewById<EditText>(R.id.editText).text.isNullOrEmpty() &&
                !usernameEditLayout.findViewById<EditText>(R.id.editText).text.isNullOrEmpty()) {
                updatePassword()
                // 返回值告诉列表需要刷新以显示新添加条目
                val resultIntent = Intent()
                resultIntent.putExtra("isRefreshList", true)
                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                 Toast.makeText(this,"请输入标题和用户名",Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_editpwd_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home -> {onBackPressed();return true}
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

    override fun onBackPressed() {
        // TODO : 优化,未修改时直接返回
        MaterialDialog(this).show {

            message(text = "是否保存?")

            positiveButton(text = "保存") {
                // 仅在标题和用户名不为空时保存
                if (!titleEditLayout.findViewById<EditText>(R.id.editText).text.isNullOrEmpty() &&
                    !usernameEditLayout.findViewById<EditText>(R.id.editText).text.isNullOrEmpty()
                ) {
                    updatePassword()
                    // 返回值告诉列表需要刷新以显示新添加条目
                    val resultIntent = Intent()
                    resultIntent.putExtra("isRefreshList", true)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                } else {
                    Toast.makeText(this@EditPasswdActivity,"请输入标题和用户名",Toast.LENGTH_SHORT).show()
                }
            }
            negativeButton(text = "取消") { super.onBackPressed() }
        }
    }


    // 初始化各输入框的标题文字以及各按钮点击事件
    private fun initTextViewName(){
        contentLinearLayout = findViewById(R.id.content_LinearLayout)

        titleEditLayout = findViewById(R.id.title_Edittext)
        titleEditLayout.findViewById<TextView>(R.id.titleTV).setText("标题")
        with(titleEditLayout.findViewById<ImageButton>(R.id.imageBtn)){
            background = AppCompatResources.getDrawable(this@EditPasswdActivity,R.drawable.icon_app)
            setOnClickListener {
                if (InstalledApp.isAppsInfoValid){
                    appsInfo = InstalledApp.getAppsInfo()
                    appsInfo = appsInfo.filterNot { it.isSystem }
                    appsInfo = appsInfo.sortedBy { it.name }
                    showSelectionAPP()
                }
                // TODO

            }
        }

        usernameEditLayout = findViewById(R.id.username_EditText)
        usernameEditLayout.findViewById<TextView>(R.id.titleTV).setText("账号")

        passwordEditLayout = findViewById(R.id.password_Edittext)
        passwordEditLayout.findViewById<TextView>(R.id.titleTV).setText("密码")
        with(passwordEditLayout.findViewById<ImageButton>(R.id.imageBtn)) {
            background = AppCompatResources.getDrawable(this@EditPasswdActivity,R.drawable.icon_random)
                // getDrawable(R.drawable.icon_random)
            setOnClickListener {
                showRandomPWDialog()
            }
        }

        websiteEditLayout = findViewById(R.id.website_Edittext)
        websiteEditLayout.findViewById<TextView>(R.id.titleTV).setText("网站")

        notesEditLayout = findViewById(R.id.notes_Edittext)
        notesEditLayout.findViewById<TextView>(R.id.titleTV).setText("备注")

    }

    // 如果是修改记录,初始化布局内容
    private fun initEditTextContent() {

        titleEditLayout.findViewById<EditText>(R.id.editText).setText(cipherData.TITLE)

        // FIXME
        val bitmap = BitmapFactory.decodeByteArray(cipherData.IMAGE,0,cipherData.IMAGE!!.size)

        titleEditLayout.findViewById<ImageButton>(R.id.imageBtn)
            .background = BitmapDrawable(resources,bitmap)

        /* 使用 Base64 解码图片
        titleEditLayout.findViewById<ImageButton>(R.id.imageBtn)
            .background = ImageUtils.bytes2Drawable(EncodeUtils.base64Decode(cipherData.IMAGE))
         */
        usernameEditLayout.findViewById<EditText>(R.id.editText).setText(cipherData.USERNAME)
        passwordEditLayout.findViewById<EditText>(R.id.editText).setText(cipherData.PASSWORD)
        websiteEditLayout.findViewById<EditText>(R.id.editText).setText(cipherData.WEBSITE)
        notesEditLayout.findViewById<EditText>(R.id.editText).setText(cipherData.NOTES)

        cipherData.ATTACHMENT?.let {
            imageViewList = Json.decodeFromString(it)
        }

        imageViewList.forEach {
            inflateAttachmentImageView(it,false)
        }
    }

    private var requestAttachment = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK){
            var imageBean = getImageUtil(result.data, contentResolver)
            inflateAttachmentImageView(imageBean)
            /*
            imageViewList.add(imageBean)
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
                    XPopup.Builder(context).asImageViewer(imageView,imageBean.bytes,SmartGlideImageLoader())
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
             */
        }
    }

    private var imageViewList = ArrayList<ImageData>()

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
                XPopup.Builder(context).asImageViewer(imageView,imageBean.bytes,SmartGlideImageLoader())
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

    /*
     * 保存账号密码数据
     */
    private fun updatePassword(){
        val isUpdata = intent.getBooleanExtra("isUpdata",false)
        // 如果不是修改密码,生成一个新的数据
        if (!isUpdata) {
            cipherData = CipherData()
        }
        //TODO 添加参数
        cipherData.TITLE = titleEditLayout.findViewById<TextView>(R.id.editText).text.toString()

        // FIXME
        val icon = titleEditLayout.findViewById<ImageButton>(R.id.imageBtn).background.toBitmap()
        val opstream = ByteArrayOutputStream()
        icon.compress(Bitmap.CompressFormat.PNG,100,opstream)
        val iconBytes = opstream.toByteArray()
        cipherData.IMAGE = iconBytes
        // 将 APP icon 转成 base64 存储
        /*
        cipherData.IMAGE = EncodeUtils.base64Encode2String(
            ImageUtils.drawable2Bytes(
                titleEditLayout.findViewById<ImageButton>(R.id.imageBtn).background
            )
        )
         */

        cipherData.USERNAME = usernameEditLayout.findViewById<TextView>(R.id.editText).text.toString()
        cipherData.PASSWORD = passwordEditLayout.findViewById<TextView>(R.id.editText).text.toString()
        cipherData.WEBSITE = websiteEditLayout.findViewById<TextView>(R.id.editText).text.toString()
        cipherData.NOTES = notesEditLayout.findViewById<TextView>(R.id.editText).text.toString()
        cipherData.ATTACHMENT = Json.encodeToString(imageViewList)

        if (!isUpdata) {
            cipherData.ADDTIME = TimeUtils.getNowString()
        }
        cipherData.LASTCHANGETIME = TimeUtils.getNowString()

        if (!isUpdata) {
            // DatabaseHandle.insertAccountData(cipherData)
            CipherDataUtils.insertAccountData(cipherData)
        } else {
            cipherData.ID?.let {
                // DatabaseHandle.updataAccountDataByID(it,cipherData)
                CipherDataUtils.updateAccountDataByID(it,cipherData)
            }
        }

    }

    /*
     * 弹出 选择应用 弹窗
     */
    private fun showSelectionAPP(){
        MaterialDialog(this@EditPasswdActivity,BottomSheet()).show {
            customView(R.layout.dialog_selection_app)

            val recyclerView = findViewById<RecyclerView>(R.id.recyclerView_searchAPP)
            val searchEditText = findViewById<EditText>(R.id.search_TV)
            searchEditText.doAfterTextChanged { text ->
                recyclerView.models = appsInfo.filter { it.name.contains(text.toString()) }
            }

            recyclerView.linear().setup {
                addType<AppUtils.AppInfo>(R.layout.item_app_recyclerview)
                onBind {
                    findView<ImageView>(R.id.app_icon).setImageDrawable(getModel<AppUtils.AppInfo>().icon)
                    findView<TextView>(R.id.app_name).setText(getModel<AppUtils.AppInfo>().name)
                }
            onClick(R.id.item_app_recyclerview){
                titleEditLayout.findViewById<ImageButton>(R.id.imageBtn)
                    .background = getModel<AppUtils.AppInfo>().icon
                titleEditLayout.findViewById<EditText>(R.id.editText).apply {
                    setText(getModel<AppUtils.AppInfo>().name)
                    setSelection(text.length)
                }
                dismiss()
            }
            }.models = appsInfo
        }
    }


    // 显示密码的部件
    private lateinit var randomPasswdStr: EditText
    // 初始密码配置
    private var passwordConfig = 14
    private var excludeConfusion = true
    private var passwordLength = 12
    private fun generatePasswd(){
        randomPasswdStr.setText(GetSecureRandomStr
            .randomString(passwordLength,passwordConfig,excludeConfusion))
    }

    /*
     * 弹出 随机生成密码 弹窗
     */
    private fun showRandomPWDialog(){
        MaterialDialog(this@EditPasswdActivity).show {
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
                passwordEditLayout.findViewById<EditText>(R.id.editText).text =
                    randomPasswdStr.text
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
                        Toast.makeText(this@EditPasswdActivity,"至少选择一项",Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this@EditPasswdActivity,"至少选择一项",Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this@EditPasswdActivity,"至少选择一项",Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this@EditPasswdActivity,"至少选择一项",Toast.LENGTH_SHORT).show()
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