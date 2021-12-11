package com.github.stargrey.secret.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.github.stargrey.secret.R
import com.github.stargrey.secret.common.Constants
import com.github.stargrey.secret.bean.NotesData
import com.github.stargrey.secret.ui.edit.EditNotesActivity
import com.github.stargrey.secret.utils.CipherDataUtils
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NotesFragment : Fragment() , SearchOrSoftDataImpl {

    lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notes,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化列表
        initRecyclerView(view)
        // 设置浮动按钮点击事件
        with(view) {
            val button = findViewById<FloatingActionButton>(R.id.floatingActionButton)
            button.setOnClickListener {
                val intent = Intent(context, EditNotesActivity::class.java)
                val addNewContent_resultCode = 1;
                startActivityForResult(intent, addNewContent_resultCode)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 如果有添加新的数据就刷新列表
        if (resultCode == AppCompatActivity.RESULT_OK){
            val isAddNewContent = data?.getBooleanExtra(Constants.shouldRefreshList,false)
            if(isAddNewContent == true){
                recyclerView.models = getNotesData()
            }
        }
    }

    override var searchString: String = ""
    override var softOrder: String = "title ASC"
    override fun refreshData() {
        recyclerView.models = getNotesData()
    }

    private fun initRecyclerView(view: View) {
        with(view) {
            recyclerView = findViewById<RecyclerView>(R.id.recyclerView_fragment_notes)

            recyclerView.linear().setup {
                addType<NotesData>(R.layout.item_notes_recyclerview)
                onBind {
                    findView<TextView>(R.id.titleTV).text = getModel<NotesData>().title

                    var content = getModel<NotesData>().content ?: ""
                    // 将所有 <br> 替换为换行
                    content = "<br>".toRegex().replace(content.toString(),"\n")
                    // 将所有图片字符替换为 [图片]
                    val pattern = "<img[^>]*>".toRegex()
                    content = pattern.replace(content.toString(),"[图片]\n")
                    // TODO 裁剪一下字符串比较好
                    findView<TextView>(R.id.contentTV).text = content
                }
                onClick(R.id.item_notes_recyclerView) {
                    val intent = Intent(context, EditNotesActivity::class.java)
                    intent.putExtra("ID",getModel<NotesData>().ID)
                    // TODO 改用别的方法启动
                    val addNewContentResultCode = 1
                    startActivityForResult(intent, addNewContentResultCode)
                }
            }.models = getNotesData()
        }

    }
    private fun getNotesData(): MutableList<Any>{
        return CipherDataUtils.getNotesData(orderBy = softOrder).filter {
            if (it as? NotesData != null)
                it.title?.contains(searchString) ?: false
            else false
        }.toMutableList()
    }
}