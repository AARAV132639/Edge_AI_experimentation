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

import com.example.edge_ai_phase_obj_detector_mob_ver.ml.EfficientdetLite0

//checking imports
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.ColorSpaceType

//importing imageutils
import com.example.edge_ai_phase_obj_detector_mob_ver.toBitmap


//data class to display box, object and confidence percentage
import android.graphics.RectF
import java.nio.file.WatchEvent

data class Detection(
    val Label: String,
    val Confidence: Float,
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
    key(isFrontCamera){
        CameraPreview(isFrontCamera=isFrontCamera)
    }

    Box(modifier= Modifier.fillMaxSize())
    {
        CameraPreview(isFrontCamera=isFrontCamera)


        //overlaying Canvas


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
fun CameraPreview( isFrontCamera: Boolean)
{
    val lifecycleOwner = LocalLifecycleOwner.current

    //adding the object detector block
    val context= LocalContext.current
    val model= remember{
        EfficientdetLite0.newInstance(context)
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

                            val tensorImage= TensorImage.fromBitmap(bitmap)

                            val outputs= model.process(tensorImage)

                            //updating detections from tensorflow
                            if(outputs.detectionResultList.isNotEmpty()) {

                                val detection= outputs.detectionResultList[0]

                                Log.d(
                                    "Detection",
                                    detection.categoryAsString
                                )
                            }

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





