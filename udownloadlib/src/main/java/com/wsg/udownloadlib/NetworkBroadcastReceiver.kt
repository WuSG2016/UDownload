package com.wsg.udownloadlib

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo

/**
 * 网络监听
 */
class NetworkBroadcastReceiver : BroadcastReceiver() {

    companion object {
        var listener: INetworkListener? = null
        const val NETWORK_ACTION = "android.net.conn.CONNECTIVITY_CHANGE"
    }

    /**
     * 网络是否连接
     */

    private fun checkNet(context: Context): Boolean {
        try {
            val connectivity:ConnectivityManager = context.getSystemService (Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val info = connectivity.activeNetworkInfo
            if (info != null && info.isConnected) {
                if (info.state == NetworkInfo.State.CONNECTED) {
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return false
    }


    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            val netWorkState = checkNet(context!!)
            listener?.onNetworkState(netWorkState)
        }

    }


}