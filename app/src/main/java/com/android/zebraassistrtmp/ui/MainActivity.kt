package com.android.zebraassistrtmp.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.projection.MediaProjectionManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.android.zebraassistrtmp.R
import com.android.zebraassistrtmp.service.RtmpService
import com.android.zebraassistrtmp.util.SharedPreferencesUtil
import com.android.zebraassistrtmp.util.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var mediaProjectionManager: MediaProjectionManager

    @Inject
    lateinit var sharedPreferencesUtil: SharedPreferencesUtil


    val rtmpET: EditText by lazy {
        findViewById(R.id.rtmpURLEt)
    }

    val streamBt: Button by lazy {
        findViewById(R.id.streamBt)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        val intentFilter = IntentFilter("STOP_STREAM")
        val receiverFlags = ContextCompat.RECEIVER_NOT_EXPORTED
        ContextCompat.registerReceiver(this, broadcastReceiver, intentFilter, receiverFlags)
        println(mainViewModel.toString())
        if (!Utils.checkPermissions(this)) {
            Utils.requestPermissions(this)
        }
        rtmpET.setText(RtmpService.url)
        if (RtmpService.url.isEmpty()) {
            rtmpET.setText(sharedPreferencesUtil.baseURL ?: "")
        }
        var currState = mainViewModel.streamingState.value
        if (currState) {
            rtmpET.isEnabled = false
            streamBt.text = getString(R.string.stop)
            streamBt.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
        } else {
            rtmpET.isEnabled = true
            streamBt.text = getString(R.string.start)
            streamBt.setBackgroundColor(ContextCompat.getColor(this, R.color.purple))
        }
        lifecycleScope.launch {
            mainViewModel.streamingState.collect {
                currState = it
                withContext(Dispatchers.Main) {
                    if (currState) {
                        sharedPreferencesUtil.baseURL = rtmpET.text.toString()
                        rtmpET.isEnabled = false
                        streamBt.text = getString(R.string.stop)
                        streamBt.setBackgroundColor(
                            ContextCompat.getColor(
                                this@MainActivity,
                                R.color.green
                            )
                        )
                    } else {
                        rtmpET.isEnabled = true
                        streamBt.text = getString(R.string.start)
                        streamBt.setBackgroundColor(
                            ContextCompat.getColor(
                                this@MainActivity,
                                R.color.purple
                            )
                        )
                    }
                }
            }
        }
        streamBt.setOnClickListener {
            if (currState) {
                //stream on
                mainViewModel.stopStream()
            } else {
                if (rtmpET.text.toString().isNotEmpty()) {
                    requestScreenCapture.launch(mediaProjectionManager.createScreenCaptureIntent())
                } else {
                    Toast.makeText(this, "URL Empty", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "STOP_STREAM") {
                mainViewModel.stopStream()
            }
        }
    }

    //screen capture
    private val requestScreenCapture =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val resultCode = result.resultCode
            if (resultCode != RESULT_OK) {
                return@registerForActivityResult
            }
            val data = result.data ?: return@registerForActivityResult
            lifecycleScope.launch {
                delay(750)
                //val lMediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
                startForegroundService()
                mainViewModel.start(rtmpET.text.toString(), result)
            }
        }

    private fun startForegroundService() {
        val serviceIntent = Intent("Start").also {
            it.setClass(this, RtmpService::class.java)
        }
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun stopForegroundService() {
        val serviceIntent = Intent("Stop").also {
            it.setClass(this, RtmpService::class.java)
        }
        ContextCompat.startForegroundService(this, serviceIntent)
    }
}