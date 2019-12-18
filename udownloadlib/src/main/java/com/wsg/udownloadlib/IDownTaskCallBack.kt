package com.wsg.udownloadlib

/**
 *  @author WuSG
 *  @date : 2019-12-06 17:46
 */
interface IDownTaskCallBack {
    /**
     * 异步请求接口的响应信息
     */
    fun onDownTask(downloadTask: DownloadTask?, msg: String)
}