package com.github.stargrey.secret.ui.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.iterator
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.github.stargrey.secret.R
import com.github.stargrey.secret.common.Constants
import com.github.stargrey.secret.data.SettingData
import com.github.stargrey.secret.ui.SetPasswordActivity
import com.github.stargrey.secret.utils.PasswordUtil
import com.bitvale.switcher.SwitcherX
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.impl.InputConfirmPopupView

class SettingFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLayoutsDescribe(view)
        initLayoutsOnClickListener(view)
    }

    private fun initLayoutsDescribe(view: View){
        val settingSharedPref = context?.getSharedPreferences(Constants.user_setting,Context.MODE_PRIVATE)
        val linearLayout = view.findViewById<LinearLayout>(R.id.setting_LinearLayout)
        for (layout in linearLayout){
            val settingData = when (layout.id){
                R.id.setPasswordHint -> SettingData.SetPasswordHint
                R.id.changePassword -> SettingData.ChangePassword
                R.id.useFingerprint -> SettingData.UseFingerprint
                R.id.notStorePassword -> SettingData.NotStorePassword
                R.id.useAndroidBiometric -> SettingData.UseAndroidBiometric
                R.id.secureScreen -> SettingData.SecureScreen
                R.id.closeDBWhenPause -> SettingData.CloseDataBaseWhenPause
                R.id.hideInTask -> SettingData.HideInTask
                else -> SettingData.DefaultData
            }
            // 设置选项的描述文字
            layout.findViewById<TextView>(R.id.setting_text).text = settingData.describe
            // 设置选项的开关,如果有
            layout.findViewById<SwitcherX>(R.id.setting_switch).apply {
                // 如果该选项无需开关,将开关隐藏
                if (!settingData.isSwitch) visibility = View.GONE
                // 依据设置文件配置开关初始状态
                setChecked(settingSharedPref?.getBoolean(settingData.name,settingData.defaultSwitch) == true)

                setOnClickListener {
                    setChecked(!isChecked)

                    switchImmediately(settingData, isChecked)

                    if (settingSharedPref != null) {
                        with(settingSharedPref.edit()){
                            putBoolean(settingData.name,isChecked)
                            commit()
                        }
                    }
                }
            }
        }
    }

    /*
     * 有开关的选项点击时弹出说明
     * 无开关的选项点击时设置其事件
     */
    private fun initLayoutsOnClickListener(view: View){
        val linearLayout = view.findViewById<LinearLayout>(R.id.setting_LinearLayout)

        for (layout in linearLayout){
            when (layout.id){
                R.id.setPasswordHint -> {
                    layout.setOnClickListener {
                        val sharedPreferences = requireContext().getSharedPreferences(Constants.user_setting,Context.MODE_PRIVATE)
                        (XPopup.Builder(requireContext())
                            .asInputConfirm("设置密码提示", null){ text ->
                                sharedPreferences.edit()
                                    .putString("passwordPrompt",text)
                                    .apply()
                            } as InputConfirmPopupView).apply {
                            inputContent = sharedPreferences
                                .getString("passwordPrompt","")
                            show()
                        }
                    }
                }
                R.id.changePassword -> {
                    layout.setOnClickListener {
                        XPopup.Builder(requireContext()).asInputConfirm("修改密码","请验证密码"){
                                verifyPassword ->
                            if (PasswordUtil.verifyPassWord(requireContext(),verifyPassword)){
                                val intent = Intent(requireContext(), SetPasswordActivity::class.java)
                                intent.putExtra("isChangePassword",true)
                                startActivity(intent)
                            } else {
                                Toast.makeText(requireContext(),"密码错误",Toast.LENGTH_SHORT).show()
                            }
                        }.show()
                    }
                }
                R.id.useFingerprint -> {
                }
                R.id.notStorePassword -> {
                    // TODO 未实现,暂时隐藏
                    layout.visibility = View.GONE
                    layout.setOnClickListener {
                        MaterialDialog(requireContext()).show {
                            title(text = "不要将密码存储在设备中")
                            message(text = """
                                 |   使用如指纹解锁,面容解锁时,需要存储密码以打开数据库,开启后会使其不可用并删除已存储的密码
                                 |   注: 请放心,应用不会直接存储您的密码,而是将 (密码+盐) 的哈希存储在 Android 加密库中,即便有 root 权限,可以读取本应用的所有数据,也无法拿到原始密码
                            """.trimMargin())
                        }
                    }
                }
                R.id.useAndroidBiometric -> {
                    // TODO 未实现,暂时隐藏
                    layout.visibility = View.GONE
                }
                R.id.secureScreen -> {
                    layout.setOnClickListener {
                        MaterialDialog(requireContext()).show {
                            message(
                                text = """
                                开启后会阻止对该应用的截屏以及录屏
                                """.trimIndent()
                                )
                            positiveButton(text = "确定")
                        }
                    }
                }
                R.id.closeDBWhenPause -> {
                    // TODO 未实现,暂时隐藏
                    layout.visibility = View.GONE
                }
                R.id.hideInTask -> {
                    // TODO 未实现,暂时隐藏
                    layout.visibility = View.GONE
                }
            }

        }
    }

    /*
     * 设置需要立即改变的开关事件,如: 禁止截屏录屏
     */
    private fun switchImmediately(settingData: SettingData,isChecked: Boolean) {
         when(settingData){
            SettingData.SecureScreen -> {
                if (!isChecked) {
                    activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
            else -> {}
        }
    }
}