package com.example.edge_ai_phase_obj_detector_mob_ver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmark
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import com.google.mediapipe.tasks.vision.core.RunningMode

import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker

class HandleLnadmarksExtractor(context: Context)
{
    private val handLandmarker: HandLandmarker

    init{
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("hand_landmarker.task")
            .build()

        val options =
            HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.IMAGE)
                .setNumHands(1)
                .build()

        handLandmarker= HandLandmarker.createFromOptions(context, options)
    }

    fun extract(bitmap: Bitmap): HandData?{
        val mpImage = BitmapImageBuilder(bitmap).build()

        val result = handLandmarker.detect(mpImage)

        if(result.landmarks().isEmpty()) {
            return null
        }

        val landmarks = result.landmarks()[0]

        //adding RectF function
        var minX= Float.MAX_VALUE
        var minY = Float.MIN_VALUE

        var maxX= Float.MIN_VALUE
        var maxY= Float.MAX_VALUE

        val input = FloatArray(63)

        var index =0

        for(lm in landmarks)
        {
            input[index++] = lm.x()
            input[index++]= lm.y()
            input[index++]= lm.z()

            minX = minOf(minX, lm.x())
            minY = minOf(minY, lm.y())
            maxX = maxOf(minX, lm.x())
            maxY = maxOf(maxY, lm.y())
        }

        val handRect = RectF(
            minX*bitmap.width,
            minY*bitmap.height,
            maxX*bitmap.width,
            maxY*bitmap.height
        )
        return HandData(
            landmarks=input,
            rect= handRect
        )
    }
}
