package com.wsg.udownloadlib

import com.wsg.udownloadlib.utils.Utils

/**
 *  @author WuSG
 *  @date : 2019-11-28 11:42
 */
data class DownloadInfo(
    var downloadUrl: String,
    /**
     * 作为唯一标示
     */
    var urlMd5: String? = null,

    var fileName: String? = null,

    /**
     * 当前进度
     */
    var currentProgress: Long = 0L,
    /**
     * 总下载量
     */
    var countLength: Long = 0L,
    /**
     * 下载百分比
     */
    var percentage: String = "0",
    /**
     *  下载状态 -1 未开始 1 成功 0失败
     */
    var downloadState: Int = -1,

    /**
     * 文件的格式
     */
    var subType: String? = null,
    /**
     * 文件存放目录
     */
    var parentFile: String? = null
) {


    private val md5String = { url: String ->
        if (url.isNotEmpty()) {
            Utils.getStringMd5(url)
        } else {
            null
        }
    }

    init {
        if (downloadUrl.isNotEmpty()) {
            this.urlMd5 = md5String(downloadUrl)
        }
    }


}
