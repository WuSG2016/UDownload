package com.wsg.udownloadlib

/**
 *  @author WuSG
 *  @date : 2019-11-04 14:55
 */
interface IDownloadResourceCallback {

    fun onDownloadStart()

    fun onDownloadProgress(threadName:String,progress: Long,percentage:Double,pathName:String)

    fun onDownloadFinish(path: String)

    fun onDownloadFail(errorInfo: String)
}