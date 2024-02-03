package com.specknet.pdiotapp.utils

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class RespeckClassificationTask2(private val context: Context) {
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
    )

    val bufferSize = 4  // Size of float32 in bytes
    val inputBuffer = ByteBuffer.allocateDirect(bufferSize * 1 * 100 * 3 * 1)
    var index = 0

    init {
        inputBuffer.order(ByteOrder.nativeOrder())
    }

    fun addData(accel_x: Float, accel_y: Float, accel_z: Float) {
        Log.d("ADDING_DATA", "$accel_x, $accel_y, $accel_z")
        if (index == 100) {
            // Delete the first 15 elements and shift the remaining data to the left
            inputBuffer.position(15 * 3 * bufferSize)
            val remainingData = inputBuffer.slice()
            inputBuffer.clear()
            inputBuffer.put(remainingData)
            index -= 15
        }

        // Calculate the position to write to in the ByteBuffer
        val position = (index * 3) * bufferSize

        // Write the sensor data to the ByteBuffer
        inputBuffer.putFloat(position, accel_x)
        inputBuffer.putFloat(position + bufferSize, accel_y)
        inputBuffer.putFloat(position + 2 * bufferSize, accel_z)

        // Increment the index
        index++
    }

    fun clearData(){
        inputBuffer.clear()
    }

    fun classifyData(): String {
        val assetManager = context.assets
        val modelFileName = "trained_model_task2.tflite"

        val outputBuffer = ByteBuffer.allocateDirect(bufferSize * 1 * 15)
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
        val outputArray = FloatArray(bufferSize * 1 * 15)
        for (i in 0 until 15) {
            outputArray[i] = outputBuffer.getFloat(i * 4)
        }

        Log.d("RETURNING OUTPUT", "Returning the Prediction")
        val maxIndex = outputArray.indices.maxByOrNull { outputArray[it] }
        return activityList[maxIndex!!]
    }

}