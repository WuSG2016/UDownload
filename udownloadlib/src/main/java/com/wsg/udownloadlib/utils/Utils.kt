package com.wsg.udownloadlib.utils

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 *  @author WuSG
 *  @date : 2019-11-25 14:55
 */
class Utils {
    companion object {
        /**
         * 获取MD5值
         */
        fun getStringMd5(str: String): String {
            try {
                val instance: MessageDigest = MessageDigest.getInstance("MD5")
                val digest: ByteArray = instance.digest(str.toByteArray())
                val sb = StringBuffer()
                for (b in digest) {
                    val i: Int = b.toInt() and 0xff
                    var hexString = Integer.toHexString(i)
                    if (hexString.length < 2) {
                        hexString = "0$hexString"
                    }
                    sb.append(hexString)
                }
                return sb.toString()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
            return ""
        }
    }

}