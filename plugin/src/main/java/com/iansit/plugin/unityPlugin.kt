package com.iansit.plugin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.gson.annotations.SerializedName
import com.iansit.plugin.utils.network.CellularManager
import com.iansit.plugin.utils.network.MobileNetworkManager
import com.iansit.plugin.utils.network.listener.IOMobileNetworkListener
import com.unity3d.player.UnityPlayer
import org.json.JSONObject
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

    private fun getAndroidID():String {
        return Settings.Secure.getString(
            context!!.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    @SuppressLint("MissingPermission")
    private fun getSimOperator():String {
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
            val tManager = context!!
                .getSystemService(AppCompatActivity.TELEPHONY_SERVICE) as TelephonyManager
            tManager.networkOperatorName
        }
    }


    /**
     * 홈트윈 아이디
    측정모드(수동, 자동 - 0 or 1)
    측정 위치 시퀀스 (0 부터 N, MRF 서버에서 부여)
    측정 위치 아이디 (MRF 서버에서 부여)
    측정 위치 X (MRF 서버에서 부여)
    측정 위치  Y (MRF 서버에서 부여)
     */
    private fun getNetworkInfo(
        objName: String,
        homeId: String,
        measurementMode: String,
        measurementSequence: String,
        measurementId:String,
        measurementX:String,
        measurementY:String
    ) {
        Toast.makeText(context, "find network data...", Toast.LENGTH_LONG).show()
        val networkManager = MobileNetworkManager(context!!)
        networkManager.getReleaseNetworkInfo(object : IOMobileNetworkListener {
            override fun denidedNeworkPermission() {
                ActivityCompat.requestPermissions(
                    UnityPlayer.currentActivity, MobileNetworkManager.REQUIRED_PERMISSION_RELEASE,
                    REQUEST_PERMISSION_GRANT
                )
                UnityPlayer.UnitySendMessage(
                    objName,
                    "denidedNeworkPermission",
                    ""
                )
            }

            override fun disableGps() {
                UnityPlayer.UnitySendMessage(
                    objName,
                    "disableGps",
                    ""
                )
            }

            override fun disableInternet() {
                UnityPlayer.UnitySendMessage(
                    objName,
                    "disableInternet",
                    ""
                )
            }

            override fun wifiSearchCountOver() {
                UnityPlayer.UnitySendMessage(
                    objName,
                    "wifiSearchCountOver",
                    ""
                )
            }

            override fun successFindInfo(json: JSONObject?) {
                if(json != null){
                    json.put("Home_Id",homeId)
                    json.put("measurement_Mode",measurementMode)
                    json.put("measurement_Id",measurementId)
                    json.put("measurement_Sequence",measurementSequence)
                    json.put("measurement_X",measurementX)
                    json.put("measurement_Y",measurementY)
                    UnityPlayer.UnitySendMessage(objName, "getNetworkSuccess", json.toString())
                }

            }

            override fun canNotCheckMobileNetwork() {
                UnityPlayer.UnitySendMessage(
                    objName,
                    "canNotCheckMobileNetwork",
                    ""
                )
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