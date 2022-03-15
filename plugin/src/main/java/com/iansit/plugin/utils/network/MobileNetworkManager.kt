package com.iansit.plugin.utils.network

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.ScanResult
import android.os.Build
import android.provider.Settings
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.appcompat.app.AppCompatActivity
import com.iansit.plugin.bean.BeanMobileNetwork
import com.iansit.plugin.bean.BeanWifiData
import com.iansit.plugin.unityPlugin
import com.iansit.plugin.utils.network.listener.IOMobileNetworkListener
import com.iansit.plugin.utils.network.listener.IOWifiListener
import org.json.JSONArray
import org.json.JSONObject

/**
 * WIFI 및 모바일 네트워크 정보 가져오기
 */
class MobileNetworkManager(private val context: Context) {

    companion object {
        val REQUIRED_PERMISSION_CELLULAR = arrayOf(
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.READ_PHONE_STATE"
        )

        val REQUIRED_PERMISSION_WIFI = arrayOf(
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.CHANGE_WIFI_STATE"
        )

        val REQUIRED_PERMISSION_RELEASE = arrayOf(
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.CHANGE_WIFI_STATE",
            "android.permission.READ_PHONE_STATE",
        )
    }

    /**
     * 상용화용 네트워크 정보 가져오기
     */
    fun getReleaseNetworkInfo(mobileNetworkListener: IOMobileNetworkListener) {
        val wifiManager = WifiManager(context)
        val cellularManager = CellularManager(context)
        // 모바일 네트워크 및 와이파이 필수 권한 확인
        if (cellularManager.checkPermissions() && wifiManager.checkPermissions()) {
            // 인터넷 및 GPS 권한 확인
            if (cellularManager.checkGps()) {
                if (cellularManager.checkInternet()) {
                    val networkList = ArrayList<Any>()
                    try {
                        // 모바일 네트워크 데이터 확인
                        for (data in cellularManager.getData(2)) {
                            networkList.add(data)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        mobileNetworkListener.canNotCheckMobileNetwork()
                    }

                    // WIFI 호출
                    getWifiInfo(
                        wifiManager,
                        networkList,
                        mobileNetworkListener
                    )
                } else {
                    mobileNetworkListener.disableInternet()

                }
            } else {
                mobileNetworkListener.disableGps()

            }
        } else {
            mobileNetworkListener.denidedNeworkPermission()
        }
    }

    /**
     * WIFI 정보 가져오기
    WIFI 제한사항
    안드로이드 배터리 수명을 늘리기 위해 안드로이드 와이파이 검색을 하는 주기를 제한함
    Android 8 / 8.1
    백그라운드 앱은 30분 간격으로 1회 스캔 가능
    Android 9이상
    각 포그라운드 앱은 2분 간격으로 4회 스캔할 수 있습니다. 이 경우, 단시간에 여러 번의 스캔이 가능하게 됩니다.
    백그라운드 앱은 모두 합쳐서 30분 간격으로 1회 스캔할 수 있습니다.
     */
    private fun getWifiInfo(
        wifiManager: WifiManager,
        networkList: ArrayList<Any>,
        mobileNetworkListener: IOMobileNetworkListener
    ) {
        try {
            wifiManager.scanStart(4, object : IOWifiListener {
                override fun scanSuccess(wifiData: BeanWifiData) {
                    networkList.add(wifiData)
                }

                override fun scanFailure(results: List<ScanResult>?) {
                    mobileNetworkListener.wifiSearchCountOver()
                }

                override fun scanEnded() {
                    val cellularManager = CellularManager(context)
                    try {
                        // 모바일 네트워크 데이터 확인
                        for (data in cellularManager.getData(2)) {
                            networkList.add(data)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        mobileNetworkListener.canNotCheckMobileNetwork()
                    }

                    val dataJson = dataFormatToJson(networkList, context)
                    mobileNetworkListener.successFindInfo(dataJson)
                    // WIFI Receiver 사용 후 등록해제
                    wifiManager.dispose()
                }
            })

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getAndroidId(context: Context):String{
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    /**
     * Sim Operator 가져오
     */
    @SuppressLint("MissingPermission")
    private fun getSimOperator(context: Context): String {
        //for dual sim mobile
        val localSubscriptionManager = SubscriptionManager.from(context)
        return if (localSubscriptionManager.activeSubscriptionInfoCount > 1) {
            //if there are two sims in dual sim mobile
            val localList: List<*> = localSubscriptionManager.activeSubscriptionInfoList
            val simInfo = localList[0] as SubscriptionInfo
//            val simInfo1 = localList[1] as SubscriptionInfo
            simInfo.displayName.toString()
            //                val sim2 = simInfo1.displayName.toString()
        } else {
            //if there is 1 sim in dual sim mobile
            val tManager = context
                .getSystemService(AppCompatActivity.TELEPHONY_SERVICE) as TelephonyManager
            tManager.networkOperatorName
        }
    }

    /**
     * 데이터 JSON형식으로 포맷
     */
    private fun dataFormatToJson(dataList: java.util.ArrayList<Any>, context: Context): JSONObject {
//        cal = Calendar.getInstance()
//        endDate = sdf.format(cal!!.time)
//        binding.tvDate.text = "Start Scan : $startDate\nEnd Scan : $endDate"

        val json = JSONObject()

        val jsonGenieverse = JSONObject()
        jsonGenieverse.put("home_id", "")
        jsonGenieverse.put("meas_mode", 0)
        jsonGenieverse.put("meas_proc", 0)
        jsonGenieverse.put("position_idx_A",JSONArray())
        json.put("Com_Genieverse", jsonGenieverse)

        val jsonMRFH = JSONObject()

        jsonMRFH.put("android_id", getAndroidId(context))
        jsonMRFH.put("sim_operator", getSimOperator(context))
        jsonMRFH.put("android_api", Build.VERSION.SDK_INT)

        json.put("Com_MRFH", jsonMRFH)

        val lteNetworks = JSONArray()
        val nrNetworks = JSONArray()
        val wifiConn = JSONArray()
        val wifiScan = JSONArray()
        for (data in dataList) {
            when (data) {
                is BeanMobileNetwork -> {
                    val networks = JSONObject()
                    networks.put("meas_idx", data.meas_idx)
                    networks.put("data_idx", data.data_idx)
                    networks.put("meas_time", data.meas_time)
                    networks.put("cell_id", data.CELL_ID)
                    networks.put("arfcn", data.ARFCN)
                    networks.put("pci", data.PCI)
                    networks.put("rsrp", data.RSRP)
                    networks.put("rsrq", data.RSRQ)
                    networks.put("sinr", data.SINR)
                    networks.put("cqi", data.CQI)
                    networks.put("mcs", data.MCS)

                    if (data.currentNetworkType == "LTE") {
                        lteNetworks.put(networks)
                    } else {
                        nrNetworks.put(networks)
                    }
                }
                is BeanWifiData -> {
                    val wifi = JSONObject()
                    wifi.put("meas_idx", data.meas_idx)
                    wifi.put("data_idx", data.data_idx)
                    wifi.put("meas_time", data.meas_time)
                    wifi.put("bssid", data.BSSID)
                    wifi.put("ssid", data.SSID)
                    wifi.put("freq", data.frequency)
                    wifi.put("bw", data.bandWidth)
                    wifi.put("ch", data.channel)
                    wifi.put("rssi", data.RSSI)
                    wifi.put("cinr", data.CINR)
                    wifi.put("mcs", data.MCS)
                    wifi.put("standard", data.standard)

                    if (data.isConnected) {
                        wifiConn.put(wifi)
                    } else {
                        wifiScan.put(wifi)
                    }

                }
            }
        }

        val networkData = JSONObject()

        networkData.put("LTE", lteNetworks)
        networkData.put("5G", nrNetworks)
        networkData.put("WiFi_Conn", wifiConn)
        networkData.put("WiFi_Scan", wifiScan)

        json.put("Data", networkData)
        return json
    }

}