package com.wsg.udownloadlib.db


/**
 *  @author WuSG
 *  @date : 2019-12-03 15:59
 */
interface ISQLiteHelper<T> {
    fun insert(downloadBean: T)
    fun update(downloadBean: T)
    fun query(isAllBean: Boolean = false, downloadBean: T? = null):List<T>?
    fun delete(downloadBean: T)
}