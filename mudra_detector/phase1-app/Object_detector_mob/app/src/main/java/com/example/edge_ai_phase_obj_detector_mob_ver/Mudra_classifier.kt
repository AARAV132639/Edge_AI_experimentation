package com.example.edge_ai_phase_obj_detector_mob_ver

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Mudra_classifier(context: Context)
{
    private val interpreter= Interpreter(FileUtil.loadMappedFile(context,"mudra.tflite"))
    private val labels = context.assets.open("labels.txt").bufferedReader().readLines()

    fun classify(bitmap: Bitmap): Pair<String, Float>
    {
        val resized = Bitmap.createScaledBitmap(bitmap, 224,224, true)

        val byteBuffer = ByteBuffer.allocateDirect(224*224*3*4).apply{order(ByteOrder.nativeOrder())}

        //Normalaizng pixels
        val intValues = IntArray(224*224)
        resized.getPixels(intValues, 0,224,0,0,224,224)

        for(pixels in intValues)
        {
            byteBuffer.putFloat(((pixels shr 16) and 0xFF)/255.0f)
            byteBuffer.putFloat(((pixels shr 8) and 0xFF)/255.0f)

        }

        val output = Array(1) { FloatArray(labels.size) }
        interpreter.run(byteBuffer,output)

        val maxIndex = output[0].indices.maxByOrNull{output[0][it]}?:-1

        return if(maxIndex!=-1) Pair(labels[maxIndex],output[0][maxIndex]) else Pair("Unknown", 0f)
    }
}
