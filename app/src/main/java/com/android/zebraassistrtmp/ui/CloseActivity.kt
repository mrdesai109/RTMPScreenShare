package com.android.zebraassistrtmp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity

class CloseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finishAffinity()
    }
}