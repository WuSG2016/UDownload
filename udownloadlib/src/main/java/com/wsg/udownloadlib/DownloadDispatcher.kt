package com.wsg.udownloadlib


import android.util.Log
import com.wsg.common.Logger
import com.wsg.udownloadlib.db.DownloadDatabaseOpenHelper
import java.io.File
import java.util.concurrent.*
import kotlin.collections.ArrayList


/**
 *  @author WuSG
 *  @date : 2019-11-06 17:41
 */
class DownloadDispatcher private constructor() {

    private val cpuCount = Runtime.getRuntime().availableProcessors()

    private val threadSize = 3.coerceAtLeast((cpuCount - 1).coerceAtMost(5))
    //核心线程数
    private val corePoolSize = threadSize
    //线程池
    var mExecutorService: ExecutorService? = null

    private val mDownloadTaskList = ArrayList<DownloadTask>()


    @Synchronized
    private fun executorService(): ExecutorService? {
        if (mExecutorService == null) {
            mExecutorService = ThreadPoolExecutor(
                corePoolSize, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                SynchronousQueue<Runnable>(), DownloadThreadFactory()
            )
        }
        return mExecutorService
    }

    init {
        mExecutorService = executorService()
    }

    fun startDownload(
        file: File,
        downloadInfo: DownloadInfo,
        callback: IDownloadResourceCallback
    ): DownloadTask {
        //分发任务
        val downloadTask = DownloadTask(threadSize, file, downloadInfo, callback)
        //开始校验任务是否存在
        if (checkDownloadTask(downloadTask, false, null)) {
            callback.onDownloadStart()
        }
        return downloadTask
    }

    private fun checkDownloadTask(
        downloadTask: DownloadTask,
        isAgain: Boolean,
        errorDownloadRunnableInfoList: MutableList<DownloadRunnableInfo>?
    ): Boolean {
        if (!mDownloadTaskList.contains(downloadTask)) {
            if (isAgain) {
                downloadTask.executeErrorRunnable(downloadTask, errorDownloadRunnableInfoList!!)
                Logger.devLog(msg = "error size-->${errorDownloadRunnableInfoList.size}")
            } else {
                downloadTask.start(downloadTask)
            }
            Log.e("checkDownloadTask", "任务不存在")
            mDownloadTaskList.add(downloadTask)
            return true
        } else {
            Logger.devLog(msg = "任务已经存在！不执行")
        }
        return false
    }

    fun againDownload(
        file: File,
        downloadInfo: DownloadInfo,
        callback: IDownloadResourceCallback
    ): DownloadTask? {
        val errorDownloadRunnableInfoList = getErrorRunnableInfoToDatabase(downloadInfo)
        val downloadTask =
            DownloadTask(errorDownloadRunnableInfoList.size, file, downloadInfo, callback)
        if (checkDownloadTask(downloadTask, true, errorDownloadRunnableInfoList)) {
            callback.onDownloadStart()
            return downloadTask
        }
        return null
    }

    /***
     * 从数据查询数据
     */

    private fun getErrorRunnableInfoToDatabase(
        downloadInfo: DownloadInfo
    ): MutableList<DownloadRunnableInfo> {
        val backDownloadRunnableInfoList: MutableList<DownloadRunnableInfo> = ArrayList()
        val downloadRunnableInfoList =
            DownloadDatabaseOpenHelper.INSTANCE.queryErrorInfo(true, 0, downloadInfo.urlMd5!!)
        if (!downloadRunnableInfoList.isNullOrEmpty()) {
            for (downloadRunnableInfo in downloadRunnableInfoList) {
                if (downloadRunnableInfo.threadStatus >= 1) {
                    Log.e("-->", "--已经下载完成!d---${downloadRunnableInfo}")
                } else {
                    Log.e("-->", "--!d---${downloadRunnableInfo}")
                    backDownloadRunnableInfoList.add(downloadRunnableInfo)
                }
            }
        }
        return backDownloadRunnableInfoList

    }

    fun recyclerTask(downloadTask: DownloadTask) {
        if (mDownloadTaskList.contains(downloadTask)) {
            mDownloadTaskList.remove(downloadTask)
        }
    }

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            DownloadDispatcher()
        }
    }
}

