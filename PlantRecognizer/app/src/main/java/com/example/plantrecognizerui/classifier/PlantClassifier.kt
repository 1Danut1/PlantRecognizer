package com.example.plantrecognizerui.classifier

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.io.InputStream
import java.nio.channels.FileChannel

class PlantClassifier(private val context: Context) {

    val model: Interpreter by lazy {
        val modelFile = context.assets.openFd("plant_model.tflite")
        val inputStream = FileInputStream(modelFile.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = modelFile.startOffset
        val declaredLength = modelFile.declaredLength
        Interpreter(fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength))
    }

    val labels: List<String> by lazy {
        context.assets.open("labels.txt").bufferedReader().readLines()
    }

    private val imageSize = 160

    fun classify(inputStream: InputStream): Pair<String, Float> {
        try {
            val original = BitmapFactory.decodeStream(inputStream)
                ?: return "Imagine invalidă" to 0f

            val rgbBitmap = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(rgbBitmap)
            canvas.drawBitmap(original, 0f, 0f, null)

            Log.d("BitmapCheck", "W: ${rgbBitmap.width}, H: ${rgbBitmap.height}, Config: ${rgbBitmap.config}")

            val tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(rgbBitmap)

            val processor = ImageProcessor.Builder()
                .add(ResizeOp(imageSize, imageSize, ResizeOp.ResizeMethod.BILINEAR))
                .add(NormalizeOp(0f, 255f)) // corect pentru Keras cu rescale=1./255
                .build()

            val input = processor.process(tensorImage)

            val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, labels.size), DataType.FLOAT32)
            model.run(input.buffer, outputBuffer.buffer.rewind())

            val output = outputBuffer.floatArray
            Log.d("ModelOutput", "Raw output: ${output.joinToString(", ", "[", "]")}")

            val maxIdx = output.indices.maxByOrNull { output[it] } ?: -1
            val label = labels.getOrNull(maxIdx) ?: "Necunoscut"
            val confidence = output.getOrNull(maxIdx) ?: 0f

            Log.d("Prediction", "Label: $label, Confidence: $confidence")

            return Pair(label, confidence)

        } catch (e: Exception) {
            Log.e("ClassifierError", "Eroare la clasificare: ${e.message}")
            return "Eroare la analiză" to 0f
        }
    }
}
fun PlantClassifier.classifyTopK(inputStream: InputStream, topK: Int = 3): List<Pair<String, Float>> {
    return try {
        val raw = classifyWithRawOutput(inputStream)
        raw.sortedByDescending { it.second }.take(topK)
    } catch (e: Exception) {
        listOf("Error" to 0f)
    }
}

fun PlantClassifier.classifyWithRawOutput(inputStream: InputStream): List<Pair<String, Float>> {
    val original = BitmapFactory.decodeStream(inputStream) ?: return listOf("Invalid image" to 0f)

    val rgbBitmap = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(rgbBitmap)
    canvas.drawBitmap(original, 0f, 0f, null)

    val tensorImage = TensorImage(DataType.FLOAT32)
    tensorImage.load(rgbBitmap)

    val processor = ImageProcessor.Builder()
        .add(ResizeOp(160, 160, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(0f, 255f))
        .build()

    val input = processor.process(tensorImage)
    val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, labels.size), DataType.FLOAT32)
    model.run(input.buffer, outputBuffer.buffer.rewind())
    val output = outputBuffer.floatArray

    return labels.mapIndexed { index, label -> label to output.getOrElse(index) { 0f } }
}