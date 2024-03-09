package com.android.zebraassistrtmp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.android.zebraassistrtmp.R
import com.android.zebraassistrtmp.util.ConnectionChecker
import com.pedro.library.base.DisplayBase
import com.pedro.library.rtmp.RtmpDisplay
import com.pedro.library.rtsp.RtspDisplay
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RtmpService : Service() {

    //service section
    private var isServiceRunning = false
    val streamingState: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private lateinit var mCtx: Context

    private var handler = Handler (Looper.getMainLooper())


    //notification section
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager

    //rtp section
    private var displayBase: DisplayBase? = null
    private val myConnectChecker = object : ConnectionChecker() {
        override fun onConnectionSuccess() {
            CoroutineScope(Dispatchers.IO).launch {
                println("Rushi : Test A : onConnectionSuccess()")
                streamingState.emit(true)
            }
            showToast("Connection Success!")
        }

        override fun onConnectionStarted(url: String) {
            println("Rushi : Test A : onConnectionStarted() : ${url}")
            super.onConnectionStarted(url)
        }

        override fun onDisconnect() {
            super.onDisconnect()
            println("Rushi : Test A : onDisconnect()")
            CoroutineScope(Dispatchers.IO).launch {
                streamingState.emit(false)
            }
            showToast("Disconnected")
        }

        override fun onConnectionFailed(reason: String) {
            println("Rushi : Test A : onConnectionFailed() : $reason")
            CoroutineScope(Dispatchers.IO).launch {
                streamingState.emit(false)
            }
            showToast("Connection fail - $reason")
        }

        override fun onNewBitrate(bitrate: Long) {
            super.onNewBitrate(bitrate)
            println("Rushi : Test A : onNewBitrate() : ${bitrate}")
        }

        override fun onAuthError() {
            super.onAuthError()
            println("Rushi : Test A : onAuthError()")
            showToast("Auth Error")
        }

        override fun onAuthSuccess() {
            super.onAuthSuccess()
            println("Rushi : Test A : onAuthSuccess()")
        }
    }


    fun prepareStreamRtp(incomingUrl: String, resultCode: Int, data: Intent) {
        url = incomingUrl
        if (url.startsWith("rtmp")) {
            displayBase = RtmpDisplay(baseContext, true, myConnectChecker)
            displayBase?.setIntentResult(resultCode, data)
        } else {
            displayBase = RtspDisplay(baseContext, true, myConnectChecker)
            displayBase?.setIntentResult(resultCode, data)
        }
        displayBase?.glInterface?.apply {
            start()
            setForceRender(true)
        }
    }

    fun startStreamRtp() {
        if(!isServiceRunning){
            handleStartService()
        }
        if (displayBase?.isStreaming != true) {
            if (displayBase?.prepareVideo(840,472,1200 * 1024) == true && displayBase?.prepareAudio() == true) {
                try{
                    println("Rushi : CacheSizeCheck : ${displayBase?.streamClient?.getCacheSize()}")
                    println("Rushi : CacheItemsCheck : ${displayBase?.streamClient?.getItemsInCache()}")
                    displayBase?.streamClient?.apply {
                        resetDroppedAudioFrames()
                        resetDroppedVideoFrames()
                        resetSentAudioFrames()
                        resetSentVideoFrames()
                        clearCache()
                    }
                    displayBase?.startStream(url)
                    CoroutineScope(Dispatchers.IO).launch {
                        streamingState.emit(true)
                    }
                }catch (ex : Exception){
                    showToast("Error - $ex")
                    handleStopService()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        setupNotification()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        mCtx = applicationContext
        intent.action?.let { serviceAction ->
            when (serviceAction) {
                "Start" -> handleStartService()
                "Stop" -> handleStopService()
            }
        }
        return START_STICKY
    }

    //Use this method to show toast
    fun showToast(msg: String) {
            handler.post{
                Toast.makeText(mCtx, msg, Toast.LENGTH_LONG).show()
            }
    }

    private fun handleStartService() {
        if (!isServiceRunning) {
            startForeground(1, notificationBuilder.build())
            isServiceRunning = true
        }
    }

    fun handleStopService() {
        displayBase?.stopStream()
        isServiceRunning = false
        displayBase?.stopStream()
        displayBase?.glInterface?.stop()
        displayBase?.streamClient?.apply {
            resetDroppedAudioFrames()
            resetDroppedVideoFrames()
            resetSentAudioFrames()
            resetSentVideoFrames()
            clearCache()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        CoroutineScope(Dispatchers.IO).launch {
            streamingState.emit(false)
        }
    }

    inner class LocalBinder : Binder() {
        fun getService(): RtmpService = this@RtmpService
    }

    private val binder = LocalBinder()
    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    private fun setupNotification() {
        notificationManager = getSystemService(
            NotificationManager::class.java
        )
        val notificationChannel = NotificationChannel(
            "rtmpChannel", "foreground", NotificationManager.IMPORTANCE_HIGH
        )

        val intent = Intent(this, RtmpBroadcastReceiver::class.java).apply {
            action = "ACTION_EXIT"
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        notificationManager.createNotificationChannel(notificationChannel)
        notificationBuilder = NotificationCompat.Builder(
            this, "rtmpChannel"
        ).setSmallIcon(R.mipmap.ic_launcher)
            .addAction(R.drawable.ic_launcher_foreground, "Stop Streaming", pendingIntent).setOngoing(true)
    }

    companion object {
        //    private var url:String = "rtmp://141.11.184.69/live/${UUID.randomUUID()}"
        var url: String = ""
        fun startService(context: Context) {
            Thread {
                context.startForegroundService(Intent(context, RtmpService::class.java).apply {
                    action = "Start"
                })
            }.start()
        }

        fun stopService(context: Context) {
            context.startForegroundService(Intent(context, RtmpService::class.java).apply {
                action = "Stop"
            })
        }

        fun bindService(context: Context, connection: ServiceConnection) {
            context.bindService(
                Intent(context, RtmpService::class.java),
                connection,
                Context.BIND_AUTO_CREATE
            )
        }
    }
}