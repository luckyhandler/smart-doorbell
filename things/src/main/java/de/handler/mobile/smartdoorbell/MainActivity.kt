package de.handler.mobile.smartdoorbell

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Base64
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import kotlinx.android.synthetic.main.activity_main.*


/**
 * Skeleton of an Android Things activity.
 *
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * val service = PeripheralManagerService()
 * val mLedGpio = service.openGpio("BCM6")
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
 * mLedGpio.value = true
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 *
 */
class MainActivity : Activity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var cameraThread: HandlerThread
    private lateinit var cloudThread: HandlerThread
    private lateinit var cameraHandler: Handler
    private lateinit var cloudHandler: Handler


    private val cameraThreadString = "CAMERA_THREAD"
    private val cloudThreadString = "CLOUD_THREAD"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this.applicationContext)
        database = FirebaseDatabase.getInstance()

        cameraThread = HandlerThread(cameraThreadString)
        cameraThread.start()
        cameraHandler = Handler(cameraThread.looper)

        cloudThread = HandlerThread(cloudThreadString)
        cloudThread.start()
        cloudHandler = Handler(cloudThread.looper)

        checkPermissionAndOpenCam()
    }

    private fun checkPermissionAndOpenCam() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 1)
        } else {
            openCamera()
        }

        doorbell_button.setOnClickListener({
            DoorbellCamera.takePicture()
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        DoorbellCamera.shutDown()

        cameraThread.quitSafely()
        cloudThread.quitSafely()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?) {
        if (requestCode == 1
                && grantResults != null
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            checkPermissionAndOpenCam()
        }
    }

    private fun openCamera() {
        DoorbellCamera.initializeCam(this, cameraHandler, ImageReader.OnImageAvailableListener {
            val image = it.acquireLatestImage()
            // get image bytes
            val imageBuf = image.planes[0].buffer
            val imageBytes = ByteArray(imageBuf.remaining())
            imageBuf.get(imageBytes)
            image.close()
            onPictureTaken(imageBytes)
        })
    }

    private fun onPictureTaken(imageBytes: ByteArray) {
        val log = database.getReference("logs").push()
        val imageStr = Base64.encodeToString(imageBytes, Base64.NO_WRAP or Base64.URL_SAFE)

        setImageToPreview(imageStr)

        log.child("image").setValue(imageStr)
        log.child("timestamp").setValue(ServerValue.TIMESTAMP)
    }

    private fun setImageToPreview(encodedImage: String) {
        object : Thread() {
            override fun run() {
                try {
                    runOnUiThread {
                        val imageByteArray = Base64.decode(encodedImage, Base64.NO_WRAP or Base64.URL_SAFE)
                        val image: Bitmap? = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
                        preview_image_view.setImageBitmap(image)

                        val handler = Handler()
                        handler.postDelayed(object : Runnable {
                            override fun run() {
                                //Do something after 5000ms
                                preview_image_view.setImageResource(R.drawable.ic_avatar)
                            }
                        }, 5000)
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()
    }
}
