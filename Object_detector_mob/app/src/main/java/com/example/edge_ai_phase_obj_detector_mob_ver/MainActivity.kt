package com.example.edge_ai_phase_obj_detector_mob_ver

import android.os.Bundle
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
import androidx.compose.ui.tooling.preview.Preview
import com.example.edge_ai_phase_obj_detector_mob_ver.ui.theme.Edge_ai_phase_obj_detector_mob_verTheme

//adding camera imports
import androidx.camera.view.PreviewView
import androidx.compose.ui.viewinterop.AndroidView

import androidx.camera.core.Preview.Builder
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
   var isFrontCamera by remember{mutableStateOf(false)}
    key(isFrontCamera){
        CameraPreview(isFrontCamera=isFrontCamera)
    }

    Box(modifier= Modifier.fillMaxSize())
    {
        CameraPreview(isFrontCamera=isFrontCamera)

        Button(
            onClick={isFrontCamera= !isFrontCamera},
            modifier= Modifier.align(Alignment.BottomCenter)
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

    AndroidView(
        factory={ context->  PreviewView(context)},

        update={previewView ->
              val cameraProviderFuture= ProcessCameraProvider.getInstance(previewView.context)

            cameraProviderFuture.addListener(
                {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = androidx.camera.core.Preview.Builder().build()

                    preview.surfaceProvider = previewView.surfaceProvider

                   //adding switch
                    val cameraSelector=
                        if(isFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA
                    else CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()

                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview
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





