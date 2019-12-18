@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.wsg.udownloadlib.utils

import java.io.File
import java.io.IOException

/**
 *  @author WuSG
 *  @date : 2019-12-11 10:06
 */
object FileUtils {

    /**
     *  校验文件
     */
     fun checkFileIsExists(file: File): Boolean {
        if (!file.exists()) {
            if (!file.parentFile.exists())
                file.parentFile.mkdir()
            try {
                file.createNewFile()
                return false
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return true
    }

}