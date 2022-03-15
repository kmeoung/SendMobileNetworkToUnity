package com.iansit.plugin

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.gson.annotations.SerializedName
import com.iansit.plugin.utils.network.CellularManager
import com.iansit.plugin.utils.network.MobileNetworkManager
import com.iansit.plugin.utils.network.listener.IOMobileNetworkListener
import com.unity3d.player.UnityPlayer
import java.util.*
import kotlin.collections.ArrayList


class unityPlugin {


    private var context: Context? = null

    companion object {
        private const val REQUEST_PERMISSION_GRANT = 3000
        private const val REQUEST_STORAGE_PERMISSION_GRANT = 3001
        private const val TAG = "MAIN_ACTIVITY_TAG"

        private const val TYPE_CELLULAR = 0
        private const val TYPE_WIFI = 1

        private const val REQUEST_PERMISSIONS = "권한을 허용해주셔야 정상적으로 앱 이용이 가능합니다."
        private const val CONFIRM = "확인"
        private const val CAN_NOT_CHECK_NETWORK = "상용망 정보를 확인할 수 없습니다."
        private const val ACTIVE_MOBILE_NETWORK = "모바일 네트워크를 활성화한 후 다시 시도해주세요."
        private const val ACTIVE_LOCATION_INFORMATION = "위치 정보 설정을 활성화한 후 다시 시도해주세요."
        private const val PLEASE_WAIT_2MINUTE = "2분뒤에 다시 시도해주세요"
        private const val PLEASE_ON_OFF_WIFI = "원활한 진행을 위해 와이파이를 껏다가 켜주시기 바랍니다."

        @JvmStatic
        private var m_instance: unityPlugin? = null

        @JvmStatic
        fun instance(): unityPlugin? {
            if (m_instance == null) {
                m_instance = unityPlugin()
            }
            return m_instance
        }
    }


    private fun setContext(context: Context) {
        this.context = context
    }


    private fun ShowToast(toastStr: String) {
        Toast.makeText(context ?: UnityPlayer.currentActivity, toastStr, Toast.LENGTH_LONG).show()
    }

    private fun AndroidVersionCheck(objName: String, objMethod: String) {
        UnityPlayer.UnitySendMessage(
            objName,
            objMethod,
            "My Android Version: " + Build.VERSION.RELEASE
        )
    }

    private fun getNetworkInfo() {
        Toast.makeText(context,"find network data...",Toast.LENGTH_LONG).show()
        val networkManager = MobileNetworkManager(context!!)
        networkManager.getReleaseNetworkInfo(object : IOMobileNetworkListener {
            override fun denidedNeworkPermission() {
                ActivityCompat.requestPermissions(
                    UnityPlayer.currentActivity, MobileNetworkManager.REQUIRED_PERMISSION_RELEASE,
                    REQUEST_PERMISSION_GRANT
                )
            }

            override fun disableGps() {
                Toast.makeText(context, ACTIVE_LOCATION_INFORMATION, Toast.LENGTH_SHORT)
                    .show()
            }

            override fun disableInternet() {
                Toast.makeText(
                    context,
                    ACTIVE_MOBILE_NETWORK,
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun wifiSearchCountOver() {
                Toast.makeText(context,PLEASE_ON_OFF_WIFI,Toast.LENGTH_SHORT).show()
                userWifiSet()
            }

            override fun successFindInfo(jsonString: String, dataList: ArrayList<Any>?) {
                Toast.makeText(context,"send json data",Toast.LENGTH_SHORT).show()
                UnityPlayer.UnitySendMessage("AndroidPlugin","getData",jsonString)
            }

            override fun canNotCheckMobileNetwork() {
                Toast.makeText(context, CAN_NOT_CHECK_NETWORK, Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun userWifiSet() {
        // todo : 상태바 표시
        val panelIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent(Settings.Panel.ACTION_WIFI)
        } else {
            Intent(Settings.ACTION_SETTINGS)
        }
        UnityPlayer.currentActivity.startActivity(panelIntent)

    }
}