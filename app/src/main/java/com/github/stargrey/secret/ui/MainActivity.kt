package com.github.stargrey.secret.ui

import android.app.ActivityManager
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.github.stargrey.secret.R
import com.github.stargrey.secret.common.Constants
import com.github.stargrey.secret.data.DatabaseHandle
import com.github.stargrey.secret.data.SettingData
import com.github.stargrey.secret.ui.fragment.CollectionFragmentAdapter
import com.github.stargrey.secret.ui.fragment.SearchOrSoftDataImpl
import kotlinx.coroutines.delay
import nl.joery.animatedbottombar.AnimatedBottomBar

class MainActivity : AppCompatActivity() {
    lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.activity_toolbar))

        if (getSharedPreferences(Constants.user_setting,Context.MODE_PRIVATE)
                .getBoolean(SettingData.SecureScreen.name,true)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE) // 禁止截屏
        }

        val viewPager = findViewById<ViewPager2>(R.id.viewpage2)
        viewPager.adapter = CollectionFragmentAdapter(this)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewPagerPosition = position
                invalidateOptionsMenu()
            }
        })
        val animatedBottomBar = findViewById<AnimatedBottomBar>(R.id.animateBottomBar)
        animatedBottomBar.setupWithViewPager2(viewPager)

    }

    override fun onPause() {
        super.onPause()
        /*
        if (getSharedPreferences(Constants.user_setting, MODE_PRIVATE)
                .getBoolean(SettingData.CloseDataBaseWhenPause.name,true)) {
            DatabaseHandle.closeDataBase()
            finish()
        }
         */

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_soft -> {
                CollectionFragmentAdapter.getFragment(viewPagerPosition).run {
                    // TODO 完善排序方法
                    if (this as? SearchOrSoftDataImpl != null) {
                        this.softOrder = when {
                            this.softOrder.contains("ASC") -> this.softOrder.replace("ASC","DESC")
                            this.softOrder.contains("DESC") -> this.softOrder.replace("DESC","ASC")
                            else -> this.softOrder
                        }
                        refreshData()
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_toolbar_menu,menu)
        val searchView = menu?.findItem(R.id.action_search)?.actionView as SearchView
        searchView.findViewById<EditText>(R.id.search_src_text).apply {
            setTextColor(Color.WHITE)
            // FIXME 修复概率出现的第一次点击时闪烁问题
            doAfterTextChanged { text ->
                CollectionFragmentAdapter.getFragment(viewPagerPosition)?.run {
                    if (this as? SearchOrSoftDataImpl != null) {
                        this.searchString = text.toString()
                        refreshData()
                    }
                }
            }
        }
        return true
    }
    private var viewPagerPosition = 0
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.action_soft)?.isVisible = true
        menu?.findItem(R.id.action_search)?.isVisible = true
        when (viewPagerPosition){
            CollectionFragmentAdapter.HOME_PAGE -> supportActionBar?.title = "密码箱"
            CollectionFragmentAdapter.NOTES_PAGE -> supportActionBar?.title = "记录箱"
            CollectionFragmentAdapter.CARD_PAGE -> supportActionBar?.title = "卡包"
            CollectionFragmentAdapter.SETTINGS_PAGE -> {
                supportActionBar?.title = "设置"
                menu?.findItem(R.id.action_soft)?.isVisible = false
                menu?.findItem(R.id.action_search)?.isVisible = false
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }
}