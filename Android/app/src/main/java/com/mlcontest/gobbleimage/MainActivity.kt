/**
 * Created by Srikanth on 15-May-2020.
 */

package com.mlcontest.gobbleimage

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import androidx.appcompat.app.AppCompatActivity
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import androidx.annotation.UiThread
import com.divyanshu.draw.widget.DrawView
import com.mlcontest.gobbleimage.tflite.Classifier

import kotlinx.android.synthetic.main.activity_main.*

open class MainActivity : AppCompatActivity() {

    private var gobbleImageView: DrawView? = null
    private var nextImageButton: Button? = null
    private var gobbleImageClassifier: Classifier? = null

    private var imageSizeXAxis = 0
    private var imageSizeYAxis = 0

    private var predictedTextView: TextView? = null
    private var backgroundHandler: Handler? = null
    private var inferenceThreadHandler: HandlerThread? = null
    private var model = Classifier.Model.FLOAT_MOBILENET
    private var device = Classifier.Device.CPU
    private var numThreads = -1

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        model = Classifier.Model.FLOAT_MOBILENET
        device = Classifier.Device.CPU
        numThreads = -1

        gobbleImageView = findViewById(R.id.gobbleimage_view)
        nextImageButton = findViewById(R.id.clear_button)
        predictedTextView = findViewById(R.id.predicted_text)

        nextImageButton?.setOnClickListener {
            gobbleImageView?.clearCanvas()
            predictedTextView?.text = getString(R.string.gobble_image_prediction_text_placeholder)
        }

        gobbleImageView?.setOnTouchListener { _, event ->
            gobbleImageView?.onTouchEvent(event)

            if (event.action == MotionEvent.ACTION_UP) {
                gobbleImage()
            }

            true
        }

        recreateGobbleImageClassifier(model, device, numThreads)
    }

    @Synchronized
    override fun onResume() {
        super.onResume()
        inferenceThreadHandler = HandlerThread("inference")
        inferenceThreadHandler!!.start()
        backgroundHandler = Handler(inferenceThreadHandler!!.looper)
    }

    @Synchronized
    override fun onPause() {
        inferenceThreadHandler!!.quitSafely()
        try {
            inferenceThreadHandler!!.join()
            inferenceThreadHandler = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
        }
        super.onPause()
    }

    override fun onDestroy() {
        gobbleImageClassifier?.close()
        super.onDestroy()
    }

    private fun gobbleImage() {
        val bitmap = gobbleImageView?.getBitmap()

        if ((bitmap != null) && (gobbleImageClassifier !=null)) {
            runInBackground(
                Runnable {
                    if (gobbleImageClassifier != null) {
                        val results: List<Classifier.Recognition> =
                            gobbleImageClassifier!!.recognizeImage(bitmap, 0)

                        runOnUiThread {
                            showPredictionText(results)
                        }
                    }
                })
        }
    }

    private fun recreateGobbleImageClassifier(model: Classifier.Model, device: Classifier.Device, numThreads: Int) {
        if (gobbleImageClassifier != null) {
            gobbleImageClassifier!!.close()
            gobbleImageClassifier = null
        }

        gobbleImageClassifier = Classifier.create(this, model, device, numThreads)

        imageSizeXAxis = gobbleImageClassifier!!.imageSizeX
        imageSizeYAxis = gobbleImageClassifier!!.imageSizeY
    }

    @Synchronized
    private fun runInBackground(r: Runnable?) {
        if (backgroundHandler != null) {
            backgroundHandler!!.post(r)
        }
    }

    @UiThread
    private fun showPredictionText(results: List<Classifier.Recognition?>?) {
        if (results != null && results.size >= 3) {
            var gobbledImages = ""
            for (recognition in results) {
                if (recognition != null) {
                    if (recognition.title != null) {
                        gobbledImages = if (gobbledImages == "") {
                            recognition.title
                        } else {
                            gobbledImages.plus(",").plus(recognition.title)
                        }
                    }
                }
                predictedTextView?.text = gobbledImages
            }
        }
    }
}