package com.wsg.udownloadlib.db

/**
 *  @author WuSG
 *  @date : 2019-11-28 16:50
 */
object DownloadRecordContract {

    const val TABLE_NAME = "DownloadInfoTable"//表格名字
    const val URL_MD5 = "url_md5"//每行一个id，便于后续操作
    const val FILE_NAME = "file_name"//给自己的某一列数据取名字
    const val CURRENT_PROGRESS = "currentProgress"
    const val COUNT_LENGTH = "countLength"
    const val PERCENTAGE = "percentage"
    const val DOWNLOAD_STATE = "download_state"
    const val SUBTYPE = "subType"
    const val DOWNLOAD_URL = "download_url"
    const val PARENT_FILE = "parent_file"


    const val DOWNLOAD_RUNNABLE_TABLE_NAME = "DownloadRunnableInfoTable"//表格名字
    const val START = "start"
    const val END = "end"
    const val THREAD_ID = "threadId"
    const val THREAD_STATUS="thread_status"
}