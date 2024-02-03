package com.specknet.pdiotapp.utils

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class RespeckClassificationTask3(private val context: Context) {
    val activityList = listOf(
        "Sitting/Standing-Breathing-Normal",
        "Lying-Left-Breathing-Normal",
        "Lying-Right-Breathing-Normal",
        "Lying-Back-Breathing-Normal",
        "Lying-Stomach-Breathing-Normal",
        "Sitting/Standing-Coughing",
        "Lying-Left-Coughing",
        "Lying-Right-Coughing",
        "Lying-Back-Coughing",
        "Lying-Stomach-Coughing",
        "Sitting/Standing-Hyperventilating",
        "Lying-Left-Hyperventilating",
        "Lying-Right-Hyperventilating",
        "Lying-Back-Hyperventilating",
        "Lying-Stomach-Hyperventilating",
        "Sitting/Standing-Other",
        "Lying-Left-Other",
        "Lying-Right-Other",
        "Lying-Back-Other",
        "Lying-Stomach-Other"
    )

    val bufferSize = 4  // Size of float32 in bytes
    val inputBuffer = ByteBuffer.allocateDirect(bufferSize * 1 * 100 * 6 * 1)
    var index = 0

    init {
        inputBuffer.order(ByteOrder.nativeOrder())
    }

    fun addData(accel_x: Float, accel_y: Float, accel_z: Float, gyro_x: Float, gyro_y: Float, gyro_z: Float) {
        Log.d("ADDING_DATA", "$accel_x, $accel_y, $accel_z")
        if (index == 100) {
            // Delete the first 15 elements and shift the remaining data to the left
            inputBuffer.position(15 * 6 * bufferSize)
            val remainingData = inputBuffer.slice()
            inputBuffer.clear()
            inputBuffer.put(remainingData)
            index -= 15
        }

        // Calculate the position to write to in the ByteBuffer
        val position = (index * 6) * bufferSize

        // Write the sensor data to the ByteBuffer
        inputBuffer.putFloat(position, accel_x)
        inputBuffer.putFloat(position + bufferSize, accel_y)
        inputBuffer.putFloat(position + 2 * bufferSize, accel_z)
        inputBuffer.putFloat(position + 3 * bufferSize, gyro_x)
        inputBuffer.putFloat(position + 4 * bufferSize, gyro_y)
        inputBuffer.putFloat(position + 5 * bufferSize, gyro_z)

        // Increment the index
        index++
    }

    fun clearData(){
        inputBuffer.clear()
    }

    fun classifyData(): String {
        val assetManager = context.assets
        val modelFileName = "trained_model_task3-gyro.tflite"

        val outputBuffer = ByteBuffer.allocateDirect(bufferSize * 1 * 20)
        outputBuffer.order(ByteOrder.nativeOrder())

        Log.d("LOADING", "Loading the Model")
        val fileDescriptor = assetManager.openFd(modelFileName)
        val inputStream = fileDescriptor.createInputStream()
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        val tfliteModel: ByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

        Log.d("LOADING", "Loaded the Interpreter")
        val interpreter = Interpreter(tfliteModel)

        Log.d("CLASSIFYING", "Classifying the data")
        interpreter.run(inputBuffer, outputBuffer)

        Log.d("DECODING OUTPUT", "Decoding the Output Buffer")
        val outputArray = FloatArray(bufferSize * 1 * 20)
        for (i in 0 until 20) {
            outputArray[i] = outputBuffer.getFloat(i * 4)
        }

        Log.d("RETURNING OUTPUT", "Returning the Prediction")
        val maxIndex = outputArray.indices.maxByOrNull { outputArray[it] }
        return activityList[maxIndex!!]
    }

}