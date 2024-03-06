package com.android.zebraassistrtmp.util

import com.pedro.common.ConnectChecker

open class ConnectionChecker : ConnectChecker {
    override fun onAuthError() {
    }

    override fun onAuthSuccess() {
    }

    override fun onConnectionFailed(reason: String) {
    }

    override fun onConnectionStarted(url: String) {
    }

    override fun onConnectionSuccess() {
    }

    override fun onDisconnect() {
    }

    override fun onNewBitrate(bitrate: Long) {
    }
}