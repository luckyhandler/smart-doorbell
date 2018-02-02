package de.handler.mobile.smartdoorbell

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.util.Log

object DoorbellCamera {
    private const val IMAGE_WIDTH = 320
    private const val IMAGE_HEIGHT = 240
    private const val MAX_IMAGES = 1

    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null

    fun initializeCam(context: Context,
                      backgroundHandler: Handler,
                      imageAvailableListener: ImageReader.OnImageAvailableListener) {
        if (cameraDevice != null) {
            return
        }

        val cameraManager = context.getSystemService(CAMERA_SERVICE) as CameraManager
        val cameraIds = cameraManager.cameraIdList
        if (cameraIds.isEmpty()) return
        val cameraId = cameraIds[0]

        imageReader = ImageReader.newInstance(IMAGE_WIDTH, IMAGE_HEIGHT, ImageFormat.JPEG, MAX_IMAGES)
        imageReader!!.setOnImageAvailableListener(imageAvailableListener, backgroundHandler)

        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice?) {
                cameraDevice = camera
            }

            override fun onDisconnected(camera: CameraDevice?) {
                cameraDevice = null
            }

            override fun onError(camera: CameraDevice?, error: Int) {
                cameraDevice?.close()
            }
        }, backgroundHandler)
    }

    fun takePicture() {
        if (cameraDevice == null) {
            return
        }
        cameraDevice!!.createCaptureSession(
                mutableListOf(imageReader?.surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigureFailed(session: CameraCaptureSession?) {

                    }

                    override fun onConfigured(session: CameraCaptureSession?) {
                        if (cameraDevice == null) {
                            return
                        }
                        captureSession = session
                        triggerImageCapture()
                    }
                }, null)
    }

    fun shutDown() {
        cameraDevice?.close()
    }

    private fun triggerImageCapture() {
        try {
            if (cameraDevice == null) {
                return
            }
            val captureBuilder: CaptureRequest.Builder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(imageReader?.surface)
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
            captureSession?.capture(captureBuilder.build(), object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(session: CameraCaptureSession?, request: CaptureRequest?, result: TotalCaptureResult?) {
                    if (session != null) {
                        session.close()
                        captureSession = null
                    }
                }
            }, null)
        } catch (cameraAccessException: CameraAccessException) {
            Log.d(TAG, "camera capture exception")
        }
    }
}