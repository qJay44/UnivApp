package edu.muiv.univapp.utils

import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.Thread.UncaughtExceptionHandler
import java.text.SimpleDateFormat
import java.util.*

class CodeInspectionHelper {

    companion object {

        private const val TAG = "CodeInspectionHelper"
        private const val MEASURE_TIME_SIZE_LIMIT = 50

        fun newInstance() = CodeInspectionHelper()

        // If need to measure time for single usage
        inline fun measureTimeMillis(block: () -> Unit): String {
            val start = System.currentTimeMillis()
            block()
            return "Execution time - ${System.currentTimeMillis() - start} ms\n"
        }

        // If need to measure time for single usage (return type of the given function is not Unit)
        inline fun <T> measureTimeMillis(loggingFunction: (String) -> Unit, function: () -> T): T {
            val startTime = System.currentTimeMillis()
            val result: T = function.invoke()
            loggingFunction.invoke("Execution time - ${System.currentTimeMillis() - startTime} ms\n")

            return result
        }
    }

    private var measureTimeTotal = 0L
    private val measuredTime: Queue<Long> = LinkedList()

    fun measureTimeMillis(block: () -> Unit): String {
        val start = System.currentTimeMillis()
        block()
        val timePassed = System.currentTimeMillis() - start

        if (measuredTime.size < MEASURE_TIME_SIZE_LIMIT) measuredTime.add(timePassed)
        else measuredTime.remove()

        measuredTime.forEach { time ->
            measureTimeTotal += time
        }

        val averageTime: Float = (measureTimeTotal / measuredTime.size).toFloat()
        measureTimeTotal = 0L

        return "Average execution time - $averageTime ms"
    }

    fun <T> measureTimeMillis(loggingFunction: (String) -> Unit, function: () -> T): T {
        val startTime = System.currentTimeMillis()
        val result: T = function.invoke()
        val timePassed = System.currentTimeMillis() - startTime

        if (measuredTime.size < MEASURE_TIME_SIZE_LIMIT) measuredTime.add(timePassed)
        else measuredTime.remove()

        measuredTime.forEach { time ->
            measureTimeTotal += time
        }

        val averageTime: Float = (measureTimeTotal / measuredTime.size).toFloat()
        measureTimeTotal = 0L

        loggingFunction.invoke("Average execution time - $averageTime ms")

        return result
    }

    class CustomizedExceptionHandler(private val path: String) : UncaughtExceptionHandler {
        // Getting the default exception handler
        // that's executed when uncaught exception terminates a thread
        private val defaultUEH = Thread.getDefaultUncaughtExceptionHandler()

        override fun uncaughtException(t: Thread, e: Throwable) {
            // Write a printable representation of this Throwable
            // The StringWriter gives the lock used synchronize access to this writer

            val stringBuffSync = StringWriter()
            val printWriter = PrintWriter(stringBuffSync)
            e.printStackTrace(printWriter)
            val stacktrace = stringBuffSync.toString()

            printWriter.close()

            writeToFile(stacktrace)

            // Used only to prevent from any code getting executed
            defaultUEH?.uncaughtException(t, e)
        }

        private fun writeToFile(currentStacktrace: String) {
            try {
                // Gets the Android external storage directory & create new folder "Crash_Reports"
                val dir = File(path, "Crash_Reports")
                if (!dir.exists()) dir.mkdirs()

                val df = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.FRANCE)
                val date = Date()
                val filename = df.format(date) + ".STACKTRACE"

                Log.d(TAG, "writeToFile: $dir/$filename")
                Log.d(TAG, "stacktrace: $currentStacktrace")

                // Write the file into the folder
                val reportFile = File(dir, filename)

                FileWriter(reportFile).apply {
                    append(currentStacktrace)
                    flush()
                    close()
                }
            } catch (e: Exception) {
                e.message?.let { Log.e("ExceptionHandler", it) }
            }
        }
    }
}
