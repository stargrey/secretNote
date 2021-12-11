package com.github.stargrey.secret.data

enum class SettingData(val describe: String, val isSwitch: Boolean,val defaultSwitch: Boolean = true) {
    // 设置密码提示
    SetPasswordHint("设置密码提示",false),
    // 更改密码
    ChangePassword("更改密码",false),

    /*
     * 以下的是开关选项
     */
    // 使用安卓生物验证
    UseAndroidBiometric("使用安卓生物验证",true),
    // 使用指纹解锁
    UseFingerprint("使用指纹解锁",true),
    NotStorePassword("不存储密码",true,false),
    // 禁止截屏录屏
    SecureScreen("禁止截屏录屏",true),
    // 进入后台时关闭数据库
    CloseDataBaseWhenPause("进入后台时关闭数据库",true,true),
    // 从后台任务中隐藏
    HideInTask("从后台任务中隐藏",true),
    // 用于 when 中 else 字段返回,以省略判空语句,若显示这个则代表出错
    DefaultData("ERROR",false)
}