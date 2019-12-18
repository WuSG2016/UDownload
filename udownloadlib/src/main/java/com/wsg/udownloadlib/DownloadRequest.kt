package com.wsg.udownloadlib

import android.content.Context
import android.content.IntentFilter
import android.util.Log
import com.wsg.common.Logger
import com.wsg.udownloadlib.db.DownloadDatabaseOpenHelper
import com.wsg.udownloadlib.utils.Utils
import java.lang.ref.WeakReference

/**
 *  @author WuSG
 *  @date : 2019-12-11 11:14
 */
class DownloadRequest private constructor(
    var downloadUrl: String,
    var mParentFilePath: String,
    var mFileName: String,
    var mDownloadResourceCallback: IDownloadResourceCallback?,
    var mDownTaskCallBack: IDownTaskCallBack?
) {

    class Builder : INetworkListener {
        private var downloadUrl: String? = null
        private var mParentFilePath: String? = null
        private var mFileName: String? = null
        private var mDownloadResourceCallback: IDownloadResourceCallback? = null
        private var mDownTaskCallBack: IDownTaskCallBack? = null
        private var weakReference: WeakReference<Context>? = null
        private var networkBroadcastReceiver: NetworkBroadcastReceiver? = null
        private val logTag = "downloadLog"

        init {
            this.mParentFilePath = Constants.DEFAULT_PATH
        }

        fun downloadPath(path: String): Builder {
            this.mParentFilePath = path
            return this
        }

        fun network(context: Context): Builder {
            weakReference = WeakReference(context)
            registerNetworkReceiver(context)
            return this
        }

        fun url(url: String): Builder {
            this.downloadUrl = url
            return this
        }

        fun downloadResourceCallback(callback: IDownloadResourceCallback): Builder {
            this.mDownloadResourceCallback = callback
            return this
        }

        fun downloadTaskCallback(callBack: IDownTaskCallBack): Builder {
            this.mDownTaskCallBack = callBack
            return this
        }

        fun fileName(fileName: String): Builder {
            this.mFileName = fileName
            return this
        }

        fun builder(): DownloadRequest {
            checkNotNull(downloadUrl) { "downloadUrl == null" }
            checkNotNull(weakReference?.get()) { "context == null" }
            Logger.init()
            Logger.addLogFile(logTag)
            DownloadDatabaseOpenHelper.init(weakReference?.get()!!)
            if (mFileName == null) {
                this.mFileName = downloadUrl.let { Utils.getStringMd5(downloadUrl!!) }
            }
            return DownloadRequest(
                downloadUrl!!,
                mParentFilePath!!,
                mFileName!!,
                mDownloadResourceCallback!!,
                mDownTaskCallBack!!
            )
        }

        override fun onNetworkState(state: Boolean) {
            when (state) {
                false -> {
                    Log.e("DownloadRequest", "网络已断开")
                }
                else -> {
                    Log.e("DownloadRequest", "网络已连接")
                    //"查询数据库是否包含未下载的数据"
                    DownloadManager.queryDatabaseToDownload(true)
                }
            }
        }

        /**
         * 网络广播注册
         */
        private fun registerNetworkReceiver(context: Context) {
            weakReference = WeakReference(context)
            val filter = IntentFilter()
            filter.addAction(NetworkBroadcastReceiver.NETWORK_ACTION)
            networkBroadcastReceiver = NetworkBroadcastReceiver()
            NetworkBroadcastReceiver.listener = this
            weakReference?.get()?.registerReceiver(networkBroadcastReceiver, filter)
        }

    }
}