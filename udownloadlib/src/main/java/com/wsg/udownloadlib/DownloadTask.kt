package com.wsg.udownloadlib

import android.util.Log
import com.wsg.common.Logger
import com.wsg.udownloadlib.db.DownloadDatabaseOpenHelper
import java.io.File
import java.text.DecimalFormat
import java.util.ArrayList
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong


/**
 *  @author WuSG
 *  @date : 2019-11-13 14:10
 */
class DownloadTask(
    private val threadSize: Int,
    private val file: File,
    private val downloadInfo: DownloadInfo,
    private val callback: IDownloadResourceCallback
) : IDownloadRunnableCallback {
    override fun onSuccess(downloadRunnableInfo: DownloadRunnableInfo) {
        //更新线程的信息
        DownloadDatabaseOpenHelper.INSTANCE.insertErrorInfo(downloadRunnableInfo)
        mSuccessRunnableNumber.getAndIncrement()
        if (mSuccessRunnableNumber.get() == threadSize) {
            callback.onDownloadFinish(file.absolutePath)
            Logger.devLog(msg = "DownloadTask-->" + "${file.name} download success")
            //更新数据库
            downloadInfo.downloadState = 1
            DownloadDatabaseOpenHelper.INSTANCE.insert(downloadInfo)
            DownloadDispatcher.instance.recyclerTask(this@DownloadTask)
        }
    }


    override fun onStart(threadName: String) {

    }

    override fun onFail(threadName: String, error: String?) {
        callback.onDownloadFail(error!!)
    }

    override fun onProgress(threadName: String, progress: Long, pathName: String) {
        mProgress.getAndAdd(progress)
        val currentProgress = mProgress.get()
        val p = ((currentProgress.toDouble() / downloadInfo.countLength.toDouble()) * 100)
        val df = DecimalFormat("0.00")
        val p2 = df.format(p)
        downloadInfo.percentage = p2
        Log.e("onProgress", "下载百分比-->$p2%--currentProgress--:$currentProgress")
        callback.onDownloadProgress(threadName, currentProgress, p2.toDouble(), pathName)
    }

    private val mSuccessRunnableNumber: AtomicInteger = AtomicInteger()
    val mProgress: AtomicLong = AtomicLong()
    private val downloadRunnableList = ArrayList<DownloadRunnable>()
    /**
     * 根据文件名称去判断是否为同一个任务
     */
    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other is DownloadTask) {
            if (other.file.name == this.file.name) {
                return true
            }
        }
        return false
    }

    override fun hashCode(): Int {
        var result = 17
        result = 31 * result + file.hashCode()
        return result
    }

    /**
     * 开始下载
     */
    fun start(downloadTask: DownloadTask) {
        val contentLength = downloadInfo.countLength
        if (contentLength > 0) {
            for (i in 0 until threadSize) {
                //先计算单个线程下载数
                val threadDownloadContentLength = contentLength / threadSize
                val start = i * threadDownloadContentLength
                val end = if (i == threadSize - 1) {
                    contentLength - 1
                } else {
                    start + threadDownloadContentLength - 1
                }
                //创建下载线程去下载
                val downloadRunnable =
                    DownloadRunnable(file, start, end, downloadInfo, 0, downloadTask, i + 1, this)
                DownloadDispatcher.instance.mExecutorService!!.execute(downloadRunnable)
                downloadRunnableList.add(downloadRunnable)
            }
        }

    }

    fun executeErrorRunnable(
        downloadTask: DownloadTask,
        errorRunnableInfoList: MutableList<DownloadRunnableInfo>
    ) {
        val downloadInfoList = DownloadDatabaseOpenHelper.INSTANCE.query(false, downloadInfo)
        for (downloadInfo in downloadInfoList!!) {
            mProgress.set(downloadInfo.currentProgress)
            Log.e("download", "已经下载了->>" + downloadInfo.currentProgress)
        }
        for (downloadRunnableInfo in errorRunnableInfoList) {
            //创建下载线程去下载
            val dr = DownloadRunnable(
                file,
                downloadRunnableInfo.start + downloadRunnableInfo.currentProcess,
                downloadRunnableInfo.end,
                downloadInfo,
                0,
                downloadTask,
                downloadRunnableInfo.threadId,
                this
            )
            DownloadDispatcher.instance.mExecutorService!!.execute(dr)
            downloadRunnableList.add(dr)
        }

    }

    /**
     * 停止下载
     */
    fun stop() {
        Log.e("downloadTask", "stop")
        for (downloadRunnable in downloadRunnableList) {
            downloadRunnable.stop()
        }
    }

    /**
     * 恢复下载
     */
    fun resume() {
        Log.e("downloadTask", "resume")
        DownloadManager.queryDatabaseToDownload(false, downloadInfo)
    }


}