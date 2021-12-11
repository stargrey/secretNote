package com.github.stargrey.secret.ui.edit

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.github.stargrey.secret.R
import com.github.stargrey.secret.common.Constants
import com.github.stargrey.secret.bean.NotesData
import com.github.stargrey.secret.utils.CipherDataUtils
import com.blankj.utilcode.util.EncodeUtils
import com.blankj.utilcode.util.TimeUtils
import jp.wasabeef.richeditor.RichEditor

class EditNotesActivity : AppCompatActivity() {
    private val REQUEST_IMAGE_GET = 10
    private lateinit var richEditor: RichEditor
    private lateinit var titleEdit: EditText
    lateinit var ID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editnotes)
        supportActionBar?.title = "编辑记录"
        // 初始化编辑界面
        initRichEditor()
        // 初始化操作栏
        initButtons()

        ID = intent.getStringExtra("ID") ?: ""
        if (ID.isNotEmpty()) {
            val notesData = CipherDataUtils.getNotesDataByID(ID)
            richEditor.html = notesData.content
            titleEdit.setText(notesData.title)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_editnote_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed();return true
            }
            R.id.action_delete -> {
                MaterialDialog(this).show {
                    message(text = "是否确认删除?")
                    positiveButton(text = "确认") {
                        if (ID.isEmpty()) super.finish()
                        CipherDataUtils.deleteNoteDataByID(ID)
                        val resultIntent = Intent()
                        resultIntent.putExtra(Constants.shouldRefreshList, true)
                        setResult(RESULT_OK, resultIntent)
                        super.finish()
                    }
                    negativeButton(text = "取消") { dismiss() }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (titleEdit.text.toString().isEmpty() && !richEditor.html.isNullOrEmpty()){
            MaterialDialog(this).show {
                message(text = "是否放弃更改?")
                positiveButton(text = "放弃"){
                    super.finish()
                }
                negativeButton(text = "取消")
            }
        } else super.onBackPressed()
    }

    // 重写 finish,以便在返回或点击完成按钮时保存数据
    override fun finish() {
        val title = titleEdit.text.toString()
        if (title.isEmpty() && !richEditor.html.isNullOrEmpty()){
            Toast.makeText(this,"请输入标题",Toast.LENGTH_SHORT).show()
            return
        }
        if (title.isNotEmpty()) {
            val notesData = NotesData(
                null,
                title,
                richEditor.html,
                TimeUtils.getNowString(),
                TimeUtils.getNowString()
            )

            if (ID.isEmpty()) {
                // DatabaseHandle.insertNotesData(notesData)
                CipherDataUtils.insertNoteData(notesData)
            } else {
                CipherDataUtils.updateNotesDataByID(ID, notesData)
            }
            val resultIntent = Intent()
            resultIntent.putExtra(Constants.shouldRefreshList, true)
            setResult(RESULT_OK, resultIntent)
        }
        super.finish()
    }

    private fun initRichEditor() {
        richEditor = findViewById(R.id.richEditor)
        richEditor.setPadding(20, 0, 20, 0)
        richEditor.loadCSS("file:///android_asset/richeditor.css")
        richEditor.setPlaceholder("输入内容...")
        titleEdit = findViewById(R.id.titleTV)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            insertImage(data)
        }
    }

    private fun initButtons() {
        findViewById<ImageButton>(R.id.addPicture).setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(intent, REQUEST_IMAGE_GET)
        }
        findViewById<TextView>(R.id.done).setOnClickListener {
            this.finish()
        }
    }

    private fun insertImage(data: Intent?) {
        val photoUri = data?.data
        val file = photoUri?.let { contentResolver.openInputStream(photoUri) }
        val fileBytes = file?.readBytes()
        val imageBase64 = EncodeUtils.base64Encode2String(fileBytes)
        val fileType = photoUri?.let { contentResolver.getType(photoUri) }
        val insertImageBase64 = "data:${fileType};base64,${imageBase64}"

        var filename: String = ""

        val cursor = photoUri?.let { contentResolver.query(photoUri, null, null, null, null, null) }

        with(cursor) {
            while (this?.moveToNext() == true) {
                filename = getString(getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME))
            }
        }
        richEditor.insertImage(insertImageBase64, filename)
    }
}