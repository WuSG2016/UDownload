package com.wsg.udownloadlib

import android.util.Log
import com.wsg.udownloadlib.db.DownloadDatabaseOpenHelper

import java.io.*

/** 下载线程
 *  @author WuSG
 *  @date : 2019-11-07 10:05
 */
data class DownloadRunnable(
    var file: File,
    var start: Long,
    var end: Long,
    var downloadInfo: DownloadInfo,
    var currentProcess: Long,
    var downloadTask: DownloadTask,
    var threadID: Int,
    var callback: IDownloadRunnableCallback?
) : Runnable {
    private var statusCode = -1
    private val downloadRunnableInfo =
        DownloadRunnableInfo(downloadInfo.urlMd5!!, start, end, currentProcess, threadID, 0)

    companion object {
        /**
         * 暂停状态
         */
        private const val STATUS_STOP = 2

    }

    private val getDownloadRangeStr = { start: Long, end: Long ->
        require(!(start < 0 && end < 0)) { "start or end less than 0" }
        "bytes=$start-$end"
    }
    private val buffSize = 8912
    override fun run() {
        Log.e("DownloadRunnable", "线程${this} 开始-$start<--->结束-$end")
        val response = DownloadManager.syncRequestUrl(
            downloadInfo.downloadUrl,
            getDownloadRangeStr,
            start,
            end
        )
        val inputStream = response.body!!.byteStream()
        writeFileFromIs(inputStream)
    }

    private fun writeFileFromIs(byteStream: InputStream) {
        callback?.onStart(Thread.currentThread().name)
        var randomAccessFile: RandomAccessFile? = null
        try {
            randomAccessFile = RandomAccessFile(file, "rwd")
            randomAccessFile.seek(start)
            val data = ByteArray(buffSize)
            var len: Int
            do {
                len = byteStream.read(data, 0, buffSize)
                if (statusCode == STATUS_STOP) {
                    updateErrorRunnable()
                    return
                }
                if (len != -1) {
                    randomAccessFile.write(data, 0, len)
                    currentProcess += len
                    callback?.onProgress(this.toString(), len.toLong(), file.absolutePath)
                } else {
                    break
                }
            } while (true)
            downloadRunnableInfo.currentProcess = this.currentProcess
            downloadRunnableInfo.threadStatus = 1
            callback?.onSuccess(downloadRunnableInfo)
        } catch (e: IOException) {
            updateErrorRunnable()
            callback?.onFail(Thread.currentThread().name, "IOException")
        } finally {
            try {
                byteStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                randomAccessFile?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    /**
     * 更新或插入线程错误信息
     */
    private fun updateErrorRunnable() {
        downloadRunnableInfo.currentProcess = this.currentProcess
        DownloadDatabaseOpenHelper.INSTANCE.insertErrorInfo(downloadRunnableInfo)
        downloadInfo.downloadState = 0
        downloadInfo.currentProgress = downloadTask.mProgress.get()
        DownloadDatabaseOpenHelper.INSTANCE.insert(downloadInfo)
        DownloadDispatcher.instance.recyclerTask(downloadTask)

    }

    fun stop() {
        statusCode = STATUS_STOP
    }


}