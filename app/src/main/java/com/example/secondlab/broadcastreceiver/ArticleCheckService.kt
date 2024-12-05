package com.example.secondlab.broadcastreceiver

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.example.secondlab.SHARED_PREFS_NAME

class ArticleCheckService : Service() {

    private val handler = Handler()
    private val checkInterval: Long = 20 * 1000 // 6 часов  6 * 60 * 60 * 100

    override fun onCreate() {
        super.onCreate()
        Log.d("ArticleCheckService", "Service started")
        handler.post(checkTask)
    }

    private val checkTask = object : Runnable {
        override fun run() {
            checkForArticleUpdates()
            handler.postDelayed(this, checkInterval)
        }
    }

    private fun checkForArticleUpdates() {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val lastUpdateTimestamp = sharedPreferences.getLong("last_update_time", 0)
        val currentTime = System.currentTimeMillis()

        // Проверяем, прошло ли больше 24 часов с момента последнего добавления статьи
        if (currentTime - lastUpdateTimestamp > 40 * 1000) {
            Log.d("ArticleCheckService", "No articles added for more than 24 hours")

            // Отправляем broadcast для напоминания
            val intent = Intent(this, ReminderReceiver::class.java)
            sendBroadcast(intent)
        } else {
            Log.d("ArticleCheckService", "Articles are up-to-date")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkTask)
        Log.d("ArticleCheckService", "Service stopped")
    }
}
