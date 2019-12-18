package com.wsg.udownloadlib

import android.os.Environment

/**
 *  @author WuSG
 *  @date : 2019-12-03 17:44
 */
object Constants {
    /**
     * APP视频缓存路径
     */
    val VIDEO_PATH = Environment.getExternalStorageDirectory().path.plus("/video/")
    /**
     * 缓存图片地址
     */
    val IMAGE_PATH = Environment.getExternalStorageDirectory().path.plus("/image/")

    val DEFAULT_PATH = Environment.getExternalStorageDirectory().path.plus("/")
}