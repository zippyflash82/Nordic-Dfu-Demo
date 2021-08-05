package com.example.dfudemo

import android.app.Activity
import no.nordicsemi.android.dfu.DfuBaseService

class DfuService : DfuBaseService() {
    override fun getNotificationTarget(): Class<out Activity>? {
        return NotificationActivity::class.java
    }

    override fun isDebug(): Boolean {
        return true
    }
}