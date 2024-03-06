package com.android.zebraassistrtmp.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.android.zebraassistrtmp.ui.CloseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RtmpBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "ACTION_EXIT") {
            context?.let { ctx ->
                //RtmpService.stopService(ctx)
                context.sendBroadcast(Intent("STOP_STREAM"))
            }
        }
    }
}