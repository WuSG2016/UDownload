package com.wsg.udownload

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.wsg.udownloadlib.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), IDownloadResourceCallback, IDownTaskCallBack {
    private val REQUEST_CODE = 1
    private var mDownloadTask: DownloadTask? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermission()
        download_stop.setOnClickListener {
            mDownloadTask?.stop()
        }
        download_resume.setOnClickListener {
            mDownloadTask?.resume()
        }
        download_url.setOnClickListener {
            downloadTest()
        }
    }


    private fun downloadTest() {
        val downloadRequest = DownloadRequest.Builder()
            .url("https://www.sample-videos.com/video123/mp4/720/big_buck_bunny_720p_20mb.mp4")
            .downloadResourceCallback(this)
            .downloadTaskCallback(this)
            .network(this)
            .builder()
        DownloadManager.downloadResource(downloadRequest)
    }


    private fun requestPermission() {
        val checkSelfPermission = ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.READ_CONTACTS
        )
        if (checkSelfPermission == PackageManager.PERMISSION_GRANTED) {
            //todo :has ready get permission write your code here
        } else {
            //requset permission
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                && permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE
            ) {
                //todo:permission granted
                Toast.makeText(this@MainActivity, "permission       granted", Toast.LENGTH_SHORT)
                    .show()


            } else {
                //todo:permission denied
                Toast.makeText(this@MainActivity, "permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 下载任务的回调 每次暂停和恢复任务不同
     */
    override fun onDownTask(downloadTask: DownloadTask?, msg: String) {
        if (downloadTask != null) {
            mDownloadTask = downloadTask
        }
        Log.e("onResponse", downloadTask.toString())
    }

    override fun onDownloadStart() {
        Log.e("downloadTest", "onDownloadStart")
    }

    override fun onDownloadProgress(
        threadName: String,
        progress: Long,
        percentage: Double,
        pathName: String
    ) {
        runOnUiThread {
            mProgress.progress = percentage.toInt()
        }
    }

    override fun onDownloadFinish(path: String) {
        Log.e("downloadTest", "onFinish")
    }

    override fun onDownloadFail(errorInfo: String) {
        Log.e("downloadTest", "onFail")
    }

}
