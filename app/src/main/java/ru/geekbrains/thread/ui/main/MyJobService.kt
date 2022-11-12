package ru.geekbrains.thread.ui.main


import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.util.Log
import java.util.*
import java.util.concurrent.TimeUnit

private const val TAG = "MyJobService"
const val DURATION = "DURATION"

class MyJobService : JobService() {

    override fun onStartJob(params: JobParameters?): Boolean {
        val arg = params?.extras?.getInt(DURATION) ?: 10
        Log.d(TAG, "onStartJob start duration: " + arg)
        startCalculations(arg)
        val broadcastIntent = Intent(TEST_BROADCAST_INTENT_FILTER)
        broadcastIntent.putExtra(THREADS_FRAGMENT_BROADCAST_EXTRA, "Job Service take " + arg + " sec")
        sendBroadcast(broadcastIntent)
        Log.d(TAG, "onStartJob finish")
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "onStopJob")
        return true
    }

    private fun startCalculations(seconds: Int): String {
        val date = Date()
        var diffInSec: Long
        do {
            val currentDate = Date()
            val diffInMs: Long = currentDate.time - date.time
            diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs)
        } while (diffInSec < seconds)
        return diffInSec.toString()
    }
}
