package com.wsg.udownloadlib

/**
 *  @author WuSG
 *  @date : 2019-12-05 14:20
 */
data class DownloadRunnableInfo(
    var urlMd5: String,
    var start: Long,
    var end: Long,
    var currentProcess: Long,
    var threadId: Int,
    /**
     * 线程状态 1 成功 0失败 -1 暂停
     */
    var threadStatus: Int
)