package com.iansit.plugin.utils.network.listener

import android.net.wifi.ScanResult
import com.iansit.plugin.bean.BeanWifiData

interface IOWifiListener {
    // Scan 성공
    fun scanSuccess(wifiData : BeanWifiData)
    // Scan 실패
    fun scanFailure(results : List<ScanResult>?)
    // Scan 종료
    fun scanEnded()
}