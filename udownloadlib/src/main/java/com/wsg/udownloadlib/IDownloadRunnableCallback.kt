package com.wsg.udownloadlib

import java.io.File

/**
 *  @author WuSG
 *  @date : 2019-11-07 17:07
 */
interface IDownloadRunnableCallback {
    fun onStart(threadName:String)
    fun onFail(threadName:String,error: String?)
    fun onProgress(threadName: String,progress: Long,pathName:String)
    fun onSuccess(downloadRunnableInfo: DownloadRunnableInfo)
}