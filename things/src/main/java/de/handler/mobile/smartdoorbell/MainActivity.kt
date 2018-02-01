package de.handler.mobile.smartdoorbell

import android.app.Activity
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Base64
import com.google.firebase.database.FirebaseDatabase
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

        database = FirebaseDatabase.getInstance()

        cameraThread = HandlerThread(cameraThreadString)
        cameraThread.start()
        cameraHandler = Handler(cameraThread.looper)

        cloudThread = HandlerThread(cloudThreadString)
        cloudThread.start()
        cloudHandler = Handler(cloudThread.looper)

        DoorbellCamera.initializeCam(this, cameraHandler, ImageReader.OnImageAvailableListener {
            val image = it.acquireLatestImage()
            // get image bytes
            val imageBuf = image.planes[0].buffer
            val imageBytes = ByteArray(imageBuf.remaining())
            imageBuf.get(imageBytes)
            image.close()
            onPictureTaken(imageBytes)
        })

        doorbell_button.setOnClickListener({ DoorbellCamera.takePicture() })
    }

    private fun onPictureTaken(imageBytes: ByteArray) {
        val log = database.getReference("logs").push()
        val imageStr = Base64.encodeToString(imageBytes, Base64.NO_WRAP or Base64.URL_SAFE)
        log.child("image").setValue(imageStr)
    }
}
