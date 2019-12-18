package com.wsg.udownloadlib

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 *  @author WuSG
 *  @date : 2019-11-06 17:51
 */
class DownloadThreadFactory : ThreadFactory {
    private val poolNumber = AtomicInteger(1)
    private var group: ThreadGroup? = Thread.currentThread().threadGroup
    private val threadNumber = AtomicInteger(1)
    private var namePrefix: String

    init {
        namePrefix = "pool-" +
                poolNumber.getAndIncrement() +
                "-downloadThread-"
    }
    override fun newThread(r: Runnable): Thread {
        val t = Thread(
            group, r,
            namePrefix + threadNumber.getAndIncrement(),
            0
        )
        if (t.isDaemon) {
            t.isDaemon = false
        }
        if (t.priority != Thread.NORM_PRIORITY) {
            t.priority = Thread.NORM_PRIORITY
        }
        return t
    }
}