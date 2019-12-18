@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.wsg.udownloadlib

import android.util.Log
import com.wsg.common.Logger
import com.wsg.udownloadlib.db.DownloadDatabaseOpenHelper
import com.wsg.udownloadlib.utils.FileUtils

import okhttp3.*

import java.io.*


/**
 *  @author WuSG
 *  @date : 2019-11-04 16:42
 */
object DownloadManager {

    internal fun queryDatabaseToDownload(isQueryAll: Boolean = false, downloadInfo: DownloadInfo? = null) {
        val downloadResourceInfoList =
            DownloadDatabaseOpenHelper.INSTANCE.query(isQueryAll, downloadInfo)
        if (downloadResourceInfoList != null && downloadResourceInfoList.isNotEmpty()) {
            for (d in downloadResourceInfoList) {
                val parentPath = getParentPath(d)
                val file = File(parentPath?.plus(d.fileName).plus(".").plus(d.subType))
                Logger.devLog(msg = "fileName1-->${file.absolutePath}")
                mCallback?.let {
                    val downloadTask = checkDownloadInfo(file, d, mCallback!!, false)
                    mResponseUrlCallBack?.let {
                        mResponseUrlCallBack!!.onDownTask(downloadTask, " query Database success")
                    }
                }
            }
        }
    }


    private var okHttpClient: OkHttpClient = OkHttpClient()
    private val videoSet = setOf("mp4", "wav", "avi", "mpeg", "3gp")
    private val imageSet = setOf("png", "jpeg", "jpg")
    private var mCallback: IDownloadResourceCallback? = null
    private var mResponseUrlCallBack: IDownTaskCallBack? = null
    private const val logTag = "downloadLog"


    private fun asyncCallUrl(url: String): Call {
        val request = Request.Builder()
            .url(url)
            .build()
        return okHttpClient.newCall(request)
    }

    internal fun syncRequestUrl(
        url: String,
        getRangeStr: (start: Long, end: Long) -> String,
        start: Long,
        end: Long
    ): Response {
        //Range 请求头格式Range: bytes=start-end
        val request = Request.Builder()
            .url(url)
            .addHeader("Range", getRangeStr(start, end))
            .build()
        return okHttpClient.newCall(request).execute()
    }

    /**
     * 下载资源文件
     */
    fun downloadResource(downloadRequest: DownloadRequest) {
        this.mCallback = downloadRequest.mDownloadResourceCallback
        this.mResponseUrlCallBack = downloadRequest.mDownTaskCallBack
        val downloadInfo = DownloadInfo(downloadRequest.downloadUrl)
        downloadInfo.parentFile = downloadRequest.mParentFilePath
        downloadInfo.fileName = downloadRequest.mFileName
        requestUrlContentLength(
            downloadInfo,
            mCallback!!,
            mResponseUrlCallBack!!
        )
    }

    private fun requestUrlContentLength(
        downloadInfo: DownloadInfo?,
        callback: IDownloadResourceCallback,
        downloadTaskCallBack: IDownTaskCallBack
    ) {
        asyncCallUrl(url = downloadInfo!!.downloadUrl).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onDownloadFail("Network error--${e.message}")
                downloadTaskCallBack.onDownTask(null, e.message!!)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body
                val contentLength = responseBody!!.contentLength()
                val subType = responseBody.contentType()!!.subtype
                downloadInfo.subType = subType
                downloadInfo.countLength = contentLength
                Logger.otherTagLog(
                    "onResponse", "contentLength--$contentLength---subType--$subType", logTag
                )
                if (contentLength <= 0) {
                    callback.onDownloadFail("response.body().contentLength() error-$contentLength")
                } else {
                    val parentPath = getParentPath(downloadInfo)
                    val file = File(parentPath?.plus(downloadInfo.fileName).plus(".").plus(subType))
                    Logger.otherTagLog("file_path", file.absolutePath, logTag)
                    val downloadTask = checkDownloadInfo(file, downloadInfo, callback, true)
                    if (downloadTask != null) {
                        downloadTaskCallBack.onDownTask(downloadTask, "request url success!")
                    }
                }
            }

        })

    }


    private fun getParentPath(downloadInfo: DownloadInfo): String? {
        return when {
            videoSet.contains(downloadInfo.subType) -> {
                return if (downloadInfo.parentFile == null) {
                    Constants.VIDEO_PATH
                } else {
                    downloadInfo.parentFile
                }
            }
            imageSet.contains(downloadInfo.subType) -> {
                return if (downloadInfo.parentFile == null) {
                    Constants.IMAGE_PATH
                } else {
                    downloadInfo.parentFile
                }
            }
            else -> null
        }
    }

    private fun checkDownloadInfo(
        file: File,
        downloadInfo: DownloadInfo?,
        callback: IDownloadResourceCallback,
        isNeedQuery: Boolean
    ): DownloadTask? {
        if (FileUtils.checkFileIsExists(file)) {
            //数据库查询是否下载完成
            if (isNeedQuery) {
                Logger.otherTagLog(
                    msg = "DownloadInfo_file_name-->${downloadInfo!!.fileName}",
                    logTag = logTag
                )
                val downloadInfoList =
                    DownloadDatabaseOpenHelper.INSTANCE.query(false, downloadBean = downloadInfo)
                Logger.otherTagLog(msg = "size-->${downloadInfoList!!.size}", logTag = logTag)
                if (downloadInfoList.isNullOrEmpty()) {
                    //文件存在 但是数据库没找到 重新下载
                    file.delete()
                    file.createNewFile()
                    Logger.devLog(msg = "删除文件->${file.absolutePath},重新下载!")
                    return DownloadDispatcher.instance.startDownload(file, downloadInfo, callback)
                } else {
                    for (d in downloadInfoList) {
                        if (d.downloadState <= 0) {
                            //没下载成功 继续分发线程去下载
                            return createErrorDownloadTask(d, file, callback)
                        } else {
                            Logger.otherTagLog(
                                msg = "file 已经下载成功-->" + file.absolutePath,
                                logTag = logTag
                            )
                        }
                    }
                }
            } else {
                if (downloadInfo!!.downloadState <= 0) {
                    //没下载成功 继续分发线程去下载
                    return createErrorDownloadTask(downloadInfo, file, callback)
                } else {
                    Logger.otherTagLog(msg = "file2 已经下载成功-->" + file.absolutePath, logTag = logTag)
                }
            }

        } else {
            Log.e("file", "${file.absolutePath}文件不存在！")
            return DownloadDispatcher.instance.startDownload(file, downloadInfo!!, callback)
        }
        return null
    }


    private fun createErrorDownloadTask(
        downloadInfo: DownloadInfo,
        file: File,
        callback: IDownloadResourceCallback
    ): DownloadTask? {
        return DownloadDispatcher.instance.againDownload(file, downloadInfo, callback)
    }


}

