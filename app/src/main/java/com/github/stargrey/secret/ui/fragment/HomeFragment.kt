package com.github.stargrey.secret.ui.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.iterator
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.github.stargrey.secret.R
import com.github.stargrey.secret.data.DatabaseHandle
import com.github.stargrey.secret.bean.CipherData
import com.github.stargrey.secret.ui.edit.EditPasswdActivity
import com.github.stargrey.secret.utils.CipherDataUtils
import com.bumptech.glide.Glide
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.github.stargrey.secret.bean.ImageData
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.util.SmartGlideImageLoader
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class HomeFragment : Fragment() , SearchOrSoftDataImpl {

    lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化列表
        initRecyclerView(view)

        with(view) {
            val button = findViewById<FloatingActionButton>(R.id.floatingActionButton)
            button.setOnClickListener {
                val intent = Intent(context, EditPasswdActivity::class.java)
                val addNewContent_resultCode = 1;
                startActivityForResult(intent, addNewContent_resultCode)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 如果有添加新的数据就刷新列表
        if (resultCode == AppCompatActivity.RESULT_OK){
            val isAddNewContent = data?.getBooleanExtra("isRefreshList",false)
            if(isAddNewContent == true){
                recyclerView.models = getAccountData()
            }
        }
    }

    override var searchString: String = ""
    override var softOrder: String = "title ASC"

    override fun refreshData() {
        recyclerView.models = getAccountData()
    }

    private fun initRecyclerView(view: View) {
        with(view) {
            recyclerView = findViewById<RecyclerView>(R.id.recyclerView_fragment_home)

            recyclerView.linear().setup {
                addType<CipherData>(R.layout.item_home_recyclerview)
                onBind {
                    // 获取模型
                    val cipherData = getModel<CipherData>()

                    findView<TextView>(R.id.item_title_textview).text =
                        cipherData.TITLE
                    findView<TextView>(R.id.item_username_textview).text =
                        cipherData.USERNAME
                    val imageStr = cipherData.IMAGE
                    // TODO
                    // FIXME
                    // val imageByte = EncodeUtils.base64Decode(imageStr)
                    val imageView = findView<ImageView>(R.id.item_imageview)
                    Glide.with(context)
                        .asBitmap()
                        .load(imageStr)
                        .into(imageView)

                }
                onClick(R.id.item_home_recyclerView) {
                    showSelectionData(getModel<CipherData>().ID ?: "")
                }
            }.models = getAccountData()
        }
    }

    private var imageViewList = ArrayList<ImageData>()
    private lateinit var contentLinearLayout : LinearLayout
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
                // 仅查看,不允许修改图片名称
                isFocusable = false
            }
            // 设置图片大小
            findViewById<TextView>(R.id.imageSize).setText(imgSizeInfo)
            // 仅查看,不允许删除图片
            findViewById<ImageButton>(R.id.deleteBtn).visibility = View.GONE
        }
        contentLinearLayout.addView(imageLayout)
    }

    /*
     * 用于展示选中的账号密码数据
     * ID : 需要查询的账号字段的 ID
     */
    private lateinit var fatherMaterialDialog : MaterialDialog // 用于嵌套显示 Dialog 时,子 Dialog 能取消显示父 Dialog
    // TODO 嵌套弹窗有显示问题,需要优化或更换库
    private fun showSelectionData(ID: String){
        fatherMaterialDialog = MaterialDialog(requireContext(), BottomSheet()).show {
            // 包含自定义布局
            customView(R.layout.bottom_sheet_dialog)
            // 底部弹窗圆角
            cornerRadius(15f)
            // 获取对应账号数据
            val cipherData = CipherDataUtils.getAccountDataByID(ID)

            findViewById<TextView>(R.id.titleTV_bottomSheet).text = cipherData.TITLE

            // 删除账号密码
            findViewById<ImageButton>(R.id.deleteBtn_bottomSheet).setOnClickListener {
                XPopup.Builder(requireContext()).asConfirm(null,"是否确认删除?"){
                    DatabaseHandle.deleteAccountDataByID(ID)
                    recyclerView.models = getAccountData()
                    fatherMaterialDialog.dismiss()
                }.show()
                /* 使用 MaterialDialog 会有闪烁问题
                MaterialDialog(requireContext()).show {

                    message(text = "是否确认删除?")

                    positiveButton(text = "删除") {
                        DatabaseHandle.deleteAccountDataByID(ID)
                        recyclerView.models = getData()
                        fatherMaterialDialog.dismiss()
                    }
                    negativeButton(text = "取消")
                }
                 */
            }
            // 编辑账号密码
            findViewById<ImageButton>(R.id.editBtn_bottomSheet).setOnClickListener {
                val intent = Intent(context, EditPasswdActivity::class.java)
                val addNewContent_resultCode = 1
                intent.putExtra("isUpdata",true)
                intent.putExtra("ID",ID)

                startActivityForResult(intent, addNewContent_resultCode)
                dismiss()
            }

            contentLinearLayout = findViewById<LinearLayout>(R.id.content_LinearLayout)

            for (subLayout in contentLinearLayout){
                // 这个布局与其他布局不同,需要排除
                if (subLayout.id == R.id.time_TV) continue

                val titleTextView: TextView
                val contentTextView: TextView
                val copyBtn: ImageView

                with(subLayout){
                    titleTextView = findViewById(R.id.titleTV)
                    contentTextView = findViewById(R.id.contentTV)
                    copyBtn = findViewById(R.id.copyBtn)
                    copyBtn.setOnClickListener {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("plain text",contentTextView.text))
                        Toast.makeText(context,titleTextView.text.toString() + " 已复制, 15 秒后清除",Toast.LENGTH_SHORT).show()
                        GlobalScope.launch {
                            delay(15000)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                                clipboard.clearPrimaryClip()
                            else clipboard.setPrimaryClip(ClipData.newPlainText("plain text",""))
                        }
                    }
                }
                when (subLayout.id) {
                    R.id.usernameTV_bottomSheet -> {
                        titleTextView.text = "账号"
                        contentTextView.text = cipherData.USERNAME
                    }
                    R.id.passwordTV_bottomSheet -> {
                        titleTextView.text = "密码"
                        contentTextView.text = cipherData.PASSWORD
                    }
                    R.id.websiteTV_bottomSheet -> {
                        titleTextView.text = "网址"
                        if (!cipherData.WEBSITE.isNullOrEmpty())
                            contentTextView.text = cipherData.WEBSITE
                        else subLayout.visibility = View.GONE
                    }
                    R.id.notesTV_bottomSheet -> {
                        titleTextView.text = "备注"
                        if (!cipherData.NOTES.isNullOrEmpty())
                            contentTextView.text = cipherData.NOTES
                        else subLayout.visibility = View.GONE
                    }
                }
            }

            with(findViewById<TextView>(R.id.time_TV)){
                var timeText = """
                    最后修改时间:${cipherData.LASTCHANGETIME}
                    添加时间:${cipherData.ADDTIME}
                """.trimIndent()
                text = timeText
            }
            cipherData.ATTACHMENT?.let {
                imageViewList = Json.decodeFromString(it)
            }
            if (imageViewList.isNotEmpty())
                layoutInflater.inflate(R.layout.segment_dividing_line,contentLinearLayout)

            imageViewList.forEach {
                inflateAttachmentImageView(it,false)
            }
        }
    }
    private fun getAccountData(): MutableList<Any>{
        return CipherDataUtils.getAccountDatas(orderBy = softOrder).filter {
            if (it as? CipherData != null)
                it.TITLE.contains(searchString) ?: false
            else false
        }.toMutableList()
    }
}