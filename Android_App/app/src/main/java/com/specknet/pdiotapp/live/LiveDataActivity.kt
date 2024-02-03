package com.specknet.pdiotapp.live

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.ToggleButton
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.tabs.TabLayout
import com.specknet.pdiotapp.R
import com.specknet.pdiotapp.utils.Constants
import com.specknet.pdiotapp.utils.DatabaseHelper
import com.specknet.pdiotapp.utils.RESpeckLiveData
import com.specknet.pdiotapp.utils.RespeckClassificationTask1
import com.specknet.pdiotapp.utils.RespeckClassificationTask2
import com.specknet.pdiotapp.utils.RespeckClassificationTask3
import kotlin.collections.ArrayList
import java.text.SimpleDateFormat
import java.util.Date

class LiveDataActivity : AppCompatActivity() {

    // global graph variables
    lateinit var dataSet_res_accel_x: LineDataSet
    lateinit var dataSet_res_accel_y: LineDataSet
    lateinit var dataSet_res_accel_z: LineDataSet

    private lateinit var toggleRecordingButton: ToggleButton
    lateinit var cancelRecordingButton: Button

    var time = 0f
    lateinit var allRespeckData: LineData

    lateinit var respeckChart: LineChart

    var prediction = ""


    // global broadcast receiver so we can unregister it
    lateinit var respeckLiveUpdateReceiver: BroadcastReceiver
    lateinit var looperRespeck: Looper
    var isRecording = false
    val filterTestRespeck = IntentFilter(Constants.ACTION_RESPECK_LIVE_BROADCAST)


    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    lateinit var currentTime: String
    var i = 0




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_data)

        val RespeckClassification1 = RespeckClassificationTask1(this)
        val RespeckClassification2 = RespeckClassificationTask2(this)
        val RespeckClassification3 = RespeckClassificationTask3(this)
        val db = DatabaseHelper(this)
        val classificationTab = findViewById<TabLayout>(R.id.tabLayout)

        toggleRecordingButton = findViewById(R.id.toggle_recording_button)
        cancelRecordingButton = findViewById(R.id.cancel_recording_button)

        toggleRecordingButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentTime = sdf.format(Date())
                isRecording = true
                enableView(cancelRecordingButton)
            }
            else{
                currentTime = ""
                isRecording = false
                disableView(cancelRecordingButton)
            }
        }
        cancelRecordingButton.setOnClickListener {
            if(isRecording){
                db.deleteData(currentTime)
                isRecording = false
                toggleRecordingButton.isChecked = false
            }
        }

        setupCharts()

        // set up the broadcast receiver
        respeckLiveUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                if(isRecording && prediction != ""){
                    Log.d("RECORDING-DATA", prediction)
                    db.insertData(prediction)
                }

                Log.i("thread", "I am running on thread = " + Thread.currentThread().name)

                val action = intent.action

                if (action == Constants.ACTION_RESPECK_LIVE_BROADCAST) {

                    val liveData =
                        intent.getSerializableExtra(Constants.RESPECK_LIVE_DATA) as RESpeckLiveData
                    Log.d("Live", "onReceive: liveData = " + liveData)

                    // get all relevant intent contents
                    val x = liveData.accelX
                    val y = liveData.accelY
                    val z = liveData.accelZ

                    val gyro_x = liveData.gyro.x
                    val gyro_y = liveData.gyro.y
                    val gyro_z = liveData.gyro.z

                    RespeckClassification1.addData(x,y,z)
                    RespeckClassification2.addData(x,y,z)
                    RespeckClassification3.addData(x,y,z, gyro_x, gyro_y, gyro_z)

                    when (classificationTab.selectedTabPosition) {
                        0 -> {
                            Log.d("TAB SELECTION", "1")
                            // First tab selected
                            if(RespeckClassification1.index == 125){
                                Log.d("PREDICTING", "Predicting Data")
                                prediction = RespeckClassification1.classifyData()

                                runOnUiThread {
                                    // Update the predictionLabel with the prediction value
                                    val predictionLabel = findViewById<TextView>(R.id.predictionLabel)
                                    predictionLabel.text = "Prediction: $prediction"
                                    setPredictionBackground(prediction)
                                }
                            }
                        }
                        1 -> {
                            Log.d("TAB SELECTION", "2")
                            // Second tab selected
                            if(RespeckClassification2.index == 100) {
                                Log.d("PREDICTING", "Predicting Data")
                                prediction = RespeckClassification2.classifyData()

                                runOnUiThread {
                                    // Update the predictionLabel with the prediction value
                                    val predictionLabel = findViewById<TextView>(R.id.predictionLabel)
                                    predictionLabel.text = "Prediction: $prediction"
                                    setPredictionBackground(prediction)
                                }
                            }
                        }
                        2 -> {
                            Log.d("TAB SELECTION", "3")
                            // Second tab selected
                            if(RespeckClassification3.index == 100) {
                                Log.d("PREDICTING", "Predicting Data")
                                prediction = RespeckClassification3.classifyData()

                                runOnUiThread {
                                    // Update the predictionLabel with the prediction value
                                    val predictionLabel = findViewById<TextView>(R.id.predictionLabel)
                                    predictionLabel.text = "Prediction: $prediction"
                                    setPredictionBackground(prediction)
                                }
                            }
                        }
                    }

                    time += 1
                    updateGraph("respeck", x, y, z)
                }
            }

        }


        // register receiver on another thread
        val handlerThreadRespeck = HandlerThread("bgThreadRespeckLive")
        handlerThreadRespeck.start()
        looperRespeck = handlerThreadRespeck.looper
        val handlerRespeck = Handler(looperRespeck)
        this.registerReceiver(respeckLiveUpdateReceiver, filterTestRespeck, null, handlerRespeck)

    }

    private fun setPredictionBackground(prediction: String) {
        val animationSelector = findViewById<pl.droidsonroids.gif.GifImageView>(R.id.animation_selector)

        val backgroundResourceId = when (prediction) {
            // TASK 1
            "Sitting/standing" -> R.drawable.sitting_standing_png
            "Lying-Left" -> R.drawable.lying_left_png
            "Lying-Right" -> R.drawable.lying_right_png
            "Lying-Back" -> R.drawable.lying_back_gif
            "Lying-Stomach" -> R.drawable.lying_stomach_png
            "Normal-Walking" -> R.drawable.normal_walking_gif
            "Running" -> R.drawable.running_gif
            "Descending-Stairs" -> R.drawable.descending_stairs_gif
            "Ascending-Stairs" -> R.drawable.ascending_stairs_gif
            "Shuffle-Walking" -> R.drawable.shuffle_walking_gif
            "Mics-Movement" -> R.drawable.mics_movement_gif

            // TASK 2
            "Sitting/Standing-Breathing-Normal" -> R.drawable.sitting_standing_png
            "Lying-Left-Breathing-Normal" -> R.drawable.lying_left_png
            "Lying-Right-Breathing-Normal" -> R.drawable.lying_right_png
            "Lying-Back-Breathing-Normal" -> R.drawable.lying_back_gif
            "Lying-Stomach-Breathing-Normal" -> R.drawable.lying_stomach_png
            "Sitting/Standing-Coughing" -> R.drawable.sitting_standing_png
            "Lying-Left-Coughing" -> R.drawable.lying_left_png
            "Lying-Right-Coughing" -> R.drawable.lying_right_png
            "Lying-Back-Coughing" -> R.drawable.lying_back_gif
            "Lying-Stomach-Coughing" -> R.drawable.lying_stomach_png
            "Sitting/Standing-Hyperventilating" -> R.drawable.sitting_standing_png
            "Lying-Left-Hyperventilating" -> R.drawable.lying_left_png
            "Lying-Right-Hyperventilating" -> R.drawable.lying_right_png
            "Lying-Back-Hyperventilating" -> R.drawable.lying_back_gif
            "Lying-Stomach-Hyperventilating" -> R.drawable.lying_stomach_png

            // TASK 3
            "Sitting/Standing-Breathing-Normal" -> R.drawable.sitting_standing_png
            "Lying-Left-Breathing-Normal" -> R.drawable.lying_left_png
            "Lying-Right-Breathing-Normal" -> R.drawable.lying_right_png
            "Lying-Back-Breathing-Normal" -> R.drawable.lying_back_gif
            "Lying-Stomach-Breathing-Normal" -> R.drawable.lying_stomach_png
            "Sitting/Standing-Coughing" -> R.drawable.sitting_standing_png
            "Lying-Left-Coughing" -> R.drawable.lying_left_png
            "Lying-Right-Coughing" -> R.drawable.lying_right_png
            "Lying-Back-Coughing" -> R.drawable.lying_back_gif
            "Lying-Stomach-Coughing" -> R.drawable.lying_stomach_png
            "Sitting/Standing-Hyperventilating" -> R.drawable.sitting_standing_png
            "Lying-Left-Hyperventilating" -> R.drawable.lying_left_png
            "Lying-Right-Hyperventilating" -> R.drawable.lying_right_png
            "Lying-Back-Hyperventilating" -> R.drawable.lying_back_gif
            "Lying-Stomach-Hyperventilating" -> R.drawable.lying_stomach_png
            "Sitting/Standing-Other" -> R.drawable.sitting_standing_png
            "Lying-Left-Other" -> R.drawable.lying_left_png
            "Lying-Right-Other" -> R.drawable.lying_right_png
            "Lying-Back-Other" -> R.drawable.lying_back_gif
            "Lying-Stomach-Other" -> R.drawable.lying_stomach_png

            else -> R.color.transparent // Use a transparent background if the prediction doesn't match any case
        }
        if (backgroundResourceId != R.color.transparent) {
            Log.d("GIF", "Stage 1")
            animationSelector.setBackgroundResource(backgroundResourceId)
        } else {
            Log.d("GIF", "Stage 2")
            // If the background is transparent, set it to null or any other default state
            animationSelector.setBackgroundResource(0)
        }
    }


    private fun disableView(view: View) {
        view.isClickable = false
        view.isEnabled = false
    }

    private fun enableView(view: View) {
        view.isClickable = true
        view.isEnabled = true
    }

    fun setupCharts() {
        respeckChart = findViewById(R.id.respeck_chart)

        // Respeck
        time = 0f
        val entries_res_accel_x = ArrayList<Entry>()
        val entries_res_accel_y = ArrayList<Entry>()
        val entries_res_accel_z = ArrayList<Entry>()

        dataSet_res_accel_x = LineDataSet(entries_res_accel_x, "Accel X")
        dataSet_res_accel_y = LineDataSet(entries_res_accel_y, "Accel Y")
        dataSet_res_accel_z = LineDataSet(entries_res_accel_z, "Accel Z")

        dataSet_res_accel_x.setDrawCircles(false)
        dataSet_res_accel_y.setDrawCircles(false)
        dataSet_res_accel_z.setDrawCircles(false)

        dataSet_res_accel_x.setColor(ContextCompat.getColor(this, R.color.red))
        dataSet_res_accel_y.setColor(ContextCompat.getColor(this, R.color.green))
        dataSet_res_accel_z.setColor(ContextCompat.getColor(this, R.color.blue))

        val dataSetsRes = ArrayList<ILineDataSet>()
        dataSetsRes.add(dataSet_res_accel_x)
        dataSetsRes.add(dataSet_res_accel_y)
        dataSetsRes.add(dataSet_res_accel_z)

        allRespeckData = LineData(dataSetsRes)
        respeckChart.data = allRespeckData
        respeckChart.invalidate()
    }

    fun updateGraph(graph: String, x: Float, y: Float, z: Float) {
        if (graph == "respeck") {
            dataSet_res_accel_x.addEntry(Entry(time, x))
            dataSet_res_accel_y.addEntry(Entry(time, y))
            dataSet_res_accel_z.addEntry(Entry(time, z))

            runOnUiThread {
                allRespeckData.notifyDataChanged()
                respeckChart.notifyDataSetChanged()
                respeckChart.invalidate()
                respeckChart.setVisibleXRangeMaximum(150f)
                respeckChart.moveViewToX(respeckChart.lowestVisibleX + 40)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(respeckLiveUpdateReceiver)
        looperRespeck.quit()
    }
}