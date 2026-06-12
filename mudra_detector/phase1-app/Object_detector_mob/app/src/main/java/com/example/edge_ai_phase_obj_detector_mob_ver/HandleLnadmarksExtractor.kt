package com.example.edge_ai_phase_obj_detector_mob_ver
import android.content.Context
import android.graphics.Bitmap
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

    fun extract(bitmap: Bitmap): FloatArray?{
        val mpImage = BitmapImageBuilder(bitmap).build()

        val result = handLandmarker.detect(mpImage)

        if(result.landmarks().isEmpty()) {
            return null
        }

        val landmarks = result.landmarks()[0]

        val input = FloatArray(63)

        var index =0

        for(lm in landmarks)
        {
            input[index++] = lm.x()
            input[index++]= lm.y()
            input[index++]= lm.z()
        }
        return input
    }
}
