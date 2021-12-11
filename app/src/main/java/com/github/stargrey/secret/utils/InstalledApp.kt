package com.github.stargrey.secret.utils

import com.blankj.utilcode.util.AppUtils

object InstalledApp {
    private lateinit var appsInfo: List<AppUtils.AppInfo>
    var isAppsInfoValid = false
        private set
    /*
     * 初始化用于展示 选择应用 的数据
     * 这一步很耗时,需要放在另一个线程中运行
     */
    fun initInstalledAppData(){
        appsInfo = AppUtils.getAppsInfo()
        isAppsInfoValid = true
    }
    fun getAppsInfo(): List<AppUtils.AppInfo> {
        return appsInfo
    }
}