package com.iansit.plugin.utils.network.listener

import org.json.JSONObject

interface IOMobileNetworkListener {
    // WIFI & Mobile Network 권한이 없음
    fun denidedNeworkPermission()
    // GPS 비활성화 상태
    fun disableGps()
    // Internet 비활성화 상태
    fun disableInternet()
    // WIFI 호출 횟수 초과함
    fun wifiSearchCountOver()
    // 정보 조회 성공
    fun successFindInfo(json: JSONObject? = null)
    // 모바일 네트워크를 확인할 수 없음
    fun canNotCheckMobileNetwork()
}