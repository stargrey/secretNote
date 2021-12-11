package com.github.stargrey.secret.ui.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.github.stargrey.secret.R
import com.github.stargrey.secret.common.Constants
import com.github.stargrey.secret.data.DatabaseHandle
import com.github.stargrey.secret.bean.CardData
import com.github.stargrey.secret.ui.edit.EditCardActivity
import com.github.stargrey.secret.utils.CipherDataUtils
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CardsFragment : Fragment() , SearchOrSoftDataImpl {
    lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cards,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化列表
        initRecyclerView(view)

        with(view){
            val button = findViewById<FloatingActionButton>(R.id.floatingActionButton)
            button.setOnClickListener {
                val intent = Intent(context, EditCardActivity::class.java)
                val addNewCard_resultCode = 1
                startActivityForResult(intent,addNewCard_resultCode)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 如果有添加新的数据就刷新列表
        if (resultCode == AppCompatActivity.RESULT_OK){
            val isAddNewContent = data?.getBooleanExtra(Constants.shouldRefreshList,false)
            if(isAddNewContent == true){
                recyclerView.models = getCardsData()
            }
        }
    }

    override var searchString = ""
    override var softOrder = "title ASC"
    override fun refreshData(){
        recyclerView.models = getCardsData()
    }

    private fun initRecyclerView(view: View){
        with(view){
            recyclerView = findViewById(R.id.recyclerView_fragment_cards)
            recyclerView.linear().setup {
                addType<CardData>(R.layout.item_card_recyclerview)
                onBind {
                    val cardData = getModel<CardData>()

                    findView<TextView>(R.id.titleTV).text = cardData.title
                    findView<TextView>(R.id.cardNumber_layout).text = cardData.cardnumber
                }
                onClick(R.id.item_card_recyclerview){
                    getModel<CardData>().ID?.let { showSelectionData(it) }
                }
            }.models = getCardsData()
        }
    }

    private fun getCardsData() : MutableList<Any>{
        return CipherDataUtils.getCardsData(orderBy = softOrder)
            .filter {
                if (it as? CardData != null)
                    it.title?.contains(searchString,true) ?: false
                else false
            }.toMutableList()
    }

    // 用于在子 dialog 弹出后关闭父dialog
    private lateinit var fatherMaterialDialog: MaterialDialog
    private fun showSelectionData(ID: String){
        fatherMaterialDialog = MaterialDialog(requireContext()).show {
            customView(R.layout.dialog_card_info)

            val cardData = CipherDataUtils.getCardDataByID(ID)

            findViewById<TextView>(R.id.titleTV).text = cardData.title

            with(findViewById<ConstraintLayout>(R.id.cardNumber_layout)){
                val cardNumberTitle = findViewById<TextView>(R.id.titleTV).apply {
                    text = "卡号"
                }
                val cardNumberContent = findViewById<TextView>(R.id.contentTV).apply {
                    text = cardData.cardnumber
                }
                findViewById<ImageView>(R.id.copyBtn).setOnClickListener {
                    val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(
                        ClipData.newPlainText(
                            "plain text",
                            cardNumberContent.text
                        )
                    )
                    Toast.makeText(
                        context,
                        cardNumberTitle.text.toString() + " 已复制",
                        Toast.LENGTH_SHORT
                    ).show()
                    // TODO 增加过一段时间清除剪切板内容的功能
                }
            }

            with(findViewById<ConstraintLayout>(R.id.cardPasswd_layout)){
                val cardPasswordTitle = findViewById<TextView>(R.id.titleTV).apply {
                    text = "密码"
                }
                val cardPasswordContent = findViewById<TextView>(R.id.contentTV).apply {
                    text = cardData.cardpassword
                }
                findViewById<ImageView>(R.id.copyBtn).setOnClickListener {
                    val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(
                        ClipData.newPlainText(
                            "plain text",
                            cardPasswordContent.text
                        )
                    )
                    Toast.makeText(
                        context,
                        cardPasswordTitle.text.toString() + " 已复制",
                        Toast.LENGTH_SHORT
                    ).show()
                    // TODO 增加过一段时间清除剪切板内容的功能
                }
            }

            findViewById<Button>(R.id.deleteBtn_dialog).setOnClickListener {
                MaterialDialog(requireContext()).show {
                    message(text = "是否确认删除?")

                    positiveButton(text = "删除") {
                        DatabaseHandle.deleteCardDataByID(ID)
                        recyclerView.models = getCardsData()
                        fatherMaterialDialog.dismiss()
                    }
                    negativeButton(text = "取消")
                }
            }

            findViewById<Button>(R.id.editBtn_dialog).setOnClickListener {
                val intent = Intent(context, EditCardActivity::class.java)
                val addNewContent_resultCode = 1
                intent.putExtra("ID",ID)
                startActivityForResult(intent, addNewContent_resultCode)
                dismiss()
            }
        }
    }
}