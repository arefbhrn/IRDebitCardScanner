package ir.arefdev.irdebitcardscanner

import android.hardware.Camera
import android.os.Handler
import android.os.Looper
import android.util.Log

internal class CameraThread : Thread() {

    private var listener: OnCameraOpenListener? = null

    @Synchronized
    fun startCamera(listener: OnCameraOpenListener) {
        this.listener = listener
        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
        (this as Object).notify()
    }

    @Synchronized
    private fun waitForOpenRequest(): OnCameraOpenListener {
        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
        while (this.listener == null) {
            try {
                (this as Object).wait()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        val listener = this.listener!!
        this.listener = null
        return listener
    }

    override fun run() {
        while (true) {
            val listener = waitForOpenRequest()

            var camera: Camera? = null
            try {
                camera = Camera.open()
            } catch (e: Exception) {
                Log.e("CameraThread", "failed to open Camera")
                e.printStackTrace()
            }

            val resultCamera = camera
            Handler(Looper.getMainLooper()).post {
                listener.onCameraOpen(resultCamera)
            }
        }
    }
}
