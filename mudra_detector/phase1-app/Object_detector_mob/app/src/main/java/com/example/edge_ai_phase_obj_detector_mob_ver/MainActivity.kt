package com.example.edge_ai_phase_obj_detector_mob_ver

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment

//adding camera imports
import androidx.camera.view.PreviewView
import androidx.compose.ui.viewinterop.AndroidView

import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.core.CameraSelector
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner

//phase 2: imports---> enabling front camera
import androidx.camera.core.ImageAnalysis

//phase 3: adding tensorflow lite
import org.tensorflow.lite.support.image.TensorImage
import androidx.camera.core.ImageProxy
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.LaunchedEffect


import androidx.compose.ui.platform.LocalContext



//checking imports
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.ColorSpaceType

//importing imageutils
import com.example.edge_ai_phase_obj_detector_mob_ver.toBitmap


//data class to display box, object and confidence percentage
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.edge_ai_phase_obj_detector_mob_ver.ml.Mudra
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer


import java.nio.file.WatchEvent

data class Detection(
    val Label: String,
    val Confidence: Float,
    val rect: RectF
)

data class HandData(
    val landmarks: FloatArray,
    val rect: RectF
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CameraScreen()
        }
    }
}


@Composable
fun CameraScreen()
{

    //-storing detections in compose state
    var detections by remember{
        mutableStateOf<List<Detection>>(emptyList())
    }

    var isFrontCamera by remember{mutableStateOf(false)}


    Box(modifier= Modifier.fillMaxSize())
    {
        CameraPreview(isFrontCamera=isFrontCamera, onDetections = {detections=it})


        val bestDetection = detections.maxByOrNull { it.Confidence }

        if(bestDetection!=null)
        {
            Text(
                text = "${bestDetection.Label}${(bestDetection.Confidence*100).toInt()}%",
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(all = 8.dp)
            )
        }

        Canvas(modifier= Modifier.fillMaxSize())
        {
            val scaleX= size.width/640f
            val scaleY= size.height/480f

            detections.forEach{
                    detection-> drawRect (

                color = androidx.compose.ui.graphics.Color.Red,

                topLeft= Offset(
                    detection.rect.left,
                    detection.rect.top
                ),

                size = androidx.compose.ui.geometry.Size(
                    detection.rect.width()*scaleX,
                    detection.rect.height()*scaleY
                ),

                style = Stroke(width = 5f)
            )
            }
        }




        Button(
            onClick={isFrontCamera= !isFrontCamera},
            modifier= Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
        {
            Text(
                if(isFrontCamera) "Front camera"
                else "Back camera"
            )
        }
    }

}

// New function for camera working
@Composable
fun CameraPreview( isFrontCamera: Boolean, onDetections:(List<Detection>)->Unit)
{
    val lifecycleOwner = LocalLifecycleOwner.current

    //adding the object detector block
    val context= LocalContext.current
    val extractor = remember { HandleLnadmarksExtractor(context) }

    val model = Mudra.newInstance(context)

    val labels = remember {
        context.assets
            .open("labels.txt")
            .bufferedReader()
            .readLines()
    }

    AndroidView(
        factory={ context->  PreviewView(context)},

        update={previewView ->
            val cameraProviderFuture= ProcessCameraProvider.getInstance(previewView.context)

            //camera binding code
            cameraProviderFuture.addListener(
                {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = androidx.camera.core.Preview.Builder().build()
                    val imageAnalysis= ImageAnalysis.Builder().build()

                    var framecount=0 //for regulating logs and displays

                    //image analyzer
                    imageAnalysis.setAnalyzer(
                        ContextCompat.getMainExecutor(previewView.context)
                    ){
                            imageProxy->

                        //skip most frames because tensorflowlite is too fast
                        framecount++

                        if(framecount%15!=0){
                            imageProxy.close()
                            return@setAnalyzer
                        }
                        try{
                            //final step of detection

                            val bitmap = imageProxy.toBitmap()

                           val handData = extractor.extract(bitmap)

                            if(handData!=null)
                            {
                                val inputFeature0 = TensorBuffer.createFixedSize(
                                    intArrayOf(1,63),
                                    DataType.FLOAT32
                                )

                                val landmarks= handData.landmarks
                                val handRect= handData.rect


                                inputFeature0.loadArray(landmarks)

                                val outputs = model.process(inputFeature0)

                                val probability = outputs.outputFeature0AsTensorBuffer.floatArray


                                //probability to labels
                                val maxIndex = probability.indices.maxByOrNull { probability[it] }?:-1

                                if(maxIndex>=0)
                                {
                                    val label= labels[maxIndex]

                                    val confidence = probability[maxIndex]

                                    //updating ui
                                    onDetections(
                                        listOf(
                                            Detection(
                                                Label = label,
                                                Confidence = confidence,
                                                rect = handRect
                                            )
                                        )
                                    )

                                    Log.d(
                                        "Mudra", "$label${(confidence*100).toInt()}%"
                                    )
                                }
                            }

                            //updating UI sate
                           // onDetections(listOf(Detection(Label = label,Confidence = confidence, rect= RectF(0f,0f,0f,0f))))

                        }

                        finally{
                            imageProxy.close()
                        }

                    }



                    preview.surfaceProvider = previewView.surfaceProvider

                    //adding switch for camera
                    val cameraSelector=
                        if(isFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA
                        else CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()

                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                ContextCompat.getMainExecutor(previewView.context)

            )
            previewView
        }
    )
}

fun ImageProxy.debugFormat(): String{
    return "format=$format width=$width height=$height"
}





