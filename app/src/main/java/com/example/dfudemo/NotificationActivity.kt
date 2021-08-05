package com.example.dfudemo

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class NotificationActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isTaskRoot) {
            Intent(this, MainActivity::class.java).apply {
                if (intent != null && intent.extras != null)
                    this.putExtras(intent.extras!!)
                startActivity(this)
            }
        }
        finish()
    }

}