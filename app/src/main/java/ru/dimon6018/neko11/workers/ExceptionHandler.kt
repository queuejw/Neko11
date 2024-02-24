package ru.dimon6018.neko11.workers

import android.content.Context
import android.content.Intent
import android.util.Log
import ru.dimon6018.neko11.ui.activities.NekoCrash
import kotlin.system.exitProcess


class ExceptionHandler : Thread.UncaughtExceptionHandler {

    private var cntxt: Context? = null

    fun setContext(context: Context?) {
        cntxt = context
    }
    override fun uncaughtException(t: Thread, e: Throwable) {
        Log.e("Neko11", "Detected critical error. See: ${e.stackTraceToString()}")
        openErrorActivity(e)
    }
    private fun openErrorActivity(e: Throwable) {
        val startAppIntent = Intent(cntxt, NekoCrash::class.java)
        startAppIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startAppIntent.putExtra("stacktrace",e.stackTraceToString())
        cntxt?.startActivity(startAppIntent)
        exitProcess(1)
    }
}
