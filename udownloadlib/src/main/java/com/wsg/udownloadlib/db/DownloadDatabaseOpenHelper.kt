package com.wsg.udownloadlib.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.wsg.common.Logger
import com.wsg.udownloadlib.DownloadInfo
import com.wsg.udownloadlib.DownloadRunnableInfo
import org.jetbrains.anko.db.*
import java.lang.ref.WeakReference

/**
 *  @author WuSG
 *  @date : 2019-12-04 15:16
 */
@Suppress("IMPLICIT_CAST_TO_ANY")
class DownloadDatabaseOpenHelper private constructor(ctx: Context) :
    ManagedSQLiteOpenHelper(
        ctx,
        DATABASE_NAME, null, DATABASE_VERSION
    ), ISQLiteHelper<DownloadInfo> {
    private val logTag = "downloadLog"

    @Synchronized
    override fun insert(downloadBean: DownloadInfo) {
        use {
            //先查询 如果存在 再更新
            val downloadList = query(false, downloadBean = downloadBean)
            if (downloadList.isNullOrEmpty()) {
                Logger.otherTagLog("DownloadDbManager", "插入数据->${downloadBean}", logTag)
                val values = ContentValues()
                values.put(DownloadRecordContract.URL_MD5, downloadBean.urlMd5)
                values.put(DownloadRecordContract.FILE_NAME, downloadBean.fileName)
                values.put(DownloadRecordContract.DOWNLOAD_URL, downloadBean.downloadUrl)
                values.put(DownloadRecordContract.CURRENT_PROGRESS, downloadBean.currentProgress)
                values.put(DownloadRecordContract.COUNT_LENGTH, downloadBean.countLength)
                values.put(DownloadRecordContract.DOWNLOAD_STATE, downloadBean.downloadState)
                values.put(DownloadRecordContract.PERCENTAGE, downloadBean.percentage)
                values.put(DownloadRecordContract.SUBTYPE, downloadBean.subType)
                values.put(DownloadRecordContract.PARENT_FILE, downloadBean.parentFile)
                insert(DownloadRecordContract.TABLE_NAME, null, values)
            } else {
                //数据库存在 更新数据
                Logger.otherTagLog("DownloadDbManager", "更新数据->${downloadBean}", logTag)
                update(
                    DownloadRecordContract.TABLE_NAME,
                    DownloadRecordContract.COUNT_LENGTH to downloadBean.countLength,
                    DownloadRecordContract.CURRENT_PROGRESS to downloadBean.currentProgress,
                    DownloadRecordContract.DOWNLOAD_STATE to downloadBean.downloadState,
                    DownloadRecordContract.PERCENTAGE to downloadBean.percentage,
                    DownloadRecordContract.PARENT_FILE to downloadBean.parentFile
                ).whereSimple("${DownloadRecordContract.URL_MD5}=?", downloadBean.urlMd5!!)
                    .exec()
            }
        }

    }

    @Synchronized
    override fun query(isAllBean: Boolean, downloadBean: DownloadInfo?): List<DownloadInfo>? {
        var downloadInfoList: List<DownloadInfo>? = null
        when (isAllBean) {
            true -> {
                use {
                    downloadInfoList = select(DownloadRecordContract.TABLE_NAME)
                        .parseList(classParser())
                }
            }
            else -> {
                downloadBean?.let {
                    use {
                        downloadInfoList = select(DownloadRecordContract.TABLE_NAME)
                            .whereSimple(
                                "${DownloadRecordContract.URL_MD5}=?",
                                downloadBean.urlMd5!!
                            )
                            .parseList(classParser())
                    }

                }
            }
        }
        return downloadInfoList
    }

    /**
     * 插入和更新错误信息
     */
    fun insertErrorInfo(downloadRunnableInfo: DownloadRunnableInfo) {
        val downloadRunnableInfoList =
            queryErrorInfo(false, downloadRunnableInfo.threadId, downloadRunnableInfo.urlMd5)
        if (downloadRunnableInfoList.isNullOrEmpty()) {
            //插入数据
            Logger.otherTagLog("DownloadDbManager", "插入线程数据->${downloadRunnableInfo}", logTag)

            use {
                val values = ContentValues()
                values.put(DownloadRecordContract.URL_MD5, downloadRunnableInfo.urlMd5)
                values.put(
                    DownloadRecordContract.CURRENT_PROGRESS,
                    downloadRunnableInfo.currentProcess
                )
                values.put(DownloadRecordContract.START, downloadRunnableInfo.start)
                values.put(DownloadRecordContract.END, downloadRunnableInfo.end)
                values.put(DownloadRecordContract.THREAD_ID, downloadRunnableInfo.threadId)
                values.put(DownloadRecordContract.THREAD_STATUS, downloadRunnableInfo.threadStatus)
                insert(DownloadRecordContract.DOWNLOAD_RUNNABLE_TABLE_NAME, null, values)
            }
        } else {
            //更新数据
            Logger.otherTagLog("DownloadDbManager", "更新线程数据->${downloadRunnableInfo}", logTag)
            use {
                update(
                    DownloadRecordContract.DOWNLOAD_RUNNABLE_TABLE_NAME,
                    DownloadRecordContract.START to downloadRunnableInfo.start,
                    DownloadRecordContract.URL_MD5 to downloadRunnableInfo.urlMd5,
                    DownloadRecordContract.CURRENT_PROGRESS to downloadRunnableInfo.currentProcess,
                    DownloadRecordContract.END to downloadRunnableInfo.end,
                    DownloadRecordContract.THREAD_ID to downloadRunnableInfo.threadId,
                    DownloadRecordContract.THREAD_STATUS to downloadRunnableInfo.threadStatus
                ).whereArgs(
                    "(${DownloadRecordContract.THREAD_ID} = {${DownloadRecordContract.THREAD_ID}}) and (${DownloadRecordContract.URL_MD5} = {${DownloadRecordContract.URL_MD5}})",
                    DownloadRecordContract.THREAD_ID to downloadRunnableInfo.threadId,
                    DownloadRecordContract.URL_MD5 to downloadRunnableInfo.urlMd5
                ).exec()

            }
        }
    }

    /**
     * 查询错误线程信息
     */
    fun queryErrorInfo(
        isAllBean: Boolean,
        threadId: Int = 0,
        urlMd5: String
    ): List<DownloadRunnableInfo>? {
        var downloadRunnableInfoList: List<DownloadRunnableInfo>? = null
        when (isAllBean) {
            true -> {
                use {
                    downloadRunnableInfoList =
                        select(DownloadRecordContract.DOWNLOAD_RUNNABLE_TABLE_NAME)
                            .whereSimple(
                                "${DownloadRecordContract.URL_MD5}=?",
                                urlMd5
                            )
                            .parseList(classParser())
                }
            }
            else -> {
                if (urlMd5.isNotEmpty() && threadId > 0) {
                    use {
                        downloadRunnableInfoList =
                            select(DownloadRecordContract.DOWNLOAD_RUNNABLE_TABLE_NAME)
                                .whereArgs(
                                    "(${DownloadRecordContract.THREAD_ID} = {${DownloadRecordContract.THREAD_ID}}) and (${DownloadRecordContract.URL_MD5} = {${DownloadRecordContract.URL_MD5}})",
                                    DownloadRecordContract.THREAD_ID to threadId,
                                    DownloadRecordContract.URL_MD5 to urlMd5
                                )
                                .parseList(classParser())
                    }
                }
            }
        }
        return downloadRunnableInfoList
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "DownloadDataBase.db"
        private var weakReference: WeakReference<Context>? = null
        fun init(ctx: Context) {
            weakReference = WeakReference(ctx)
        }

        val INSTANCE: DownloadDatabaseOpenHelper by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            DownloadDatabaseOpenHelper(weakReference?.get()!!)
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Here you create tables
        db.createTable(
            DownloadRecordContract.TABLE_NAME, true,
            Pair(DownloadRecordContract.DOWNLOAD_URL, TEXT),
            Pair(DownloadRecordContract.URL_MD5, TEXT + PRIMARY_KEY + UNIQUE),
            Pair(DownloadRecordContract.FILE_NAME, TEXT),
            Pair(DownloadRecordContract.CURRENT_PROGRESS, INTEGER),
            Pair(DownloadRecordContract.COUNT_LENGTH, INTEGER),
            Pair(DownloadRecordContract.PERCENTAGE, TEXT),
            Pair(DownloadRecordContract.DOWNLOAD_STATE, INTEGER),
            Pair(DownloadRecordContract.SUBTYPE, TEXT),
            Pair(DownloadRecordContract.PARENT_FILE, TEXT)
        )
        /**
         * 错误信息表格
         */
        db.createTable(
            DownloadRecordContract.DOWNLOAD_RUNNABLE_TABLE_NAME, true,
            Pair(DownloadRecordContract.URL_MD5, TEXT),
            Pair(DownloadRecordContract.START, INTEGER),
            Pair(DownloadRecordContract.END, INTEGER),
            Pair(DownloadRecordContract.CURRENT_PROGRESS, INTEGER),
            Pair(DownloadRecordContract.THREAD_ID, INTEGER),
            Pair(DownloadRecordContract.THREAD_STATUS, INTEGER)
        )

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Here you can upgrade tables, as usual
        db.dropTable(DownloadRecordContract.TABLE_NAME, true)
    }

    override fun update(downloadBean: DownloadInfo) {
    }

    override fun delete(downloadBean: DownloadInfo) {
    }
}

