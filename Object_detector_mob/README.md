# Edge AI Object Detector (Mobile Version)

## Overview

Real time object detection Android application built using CameraX, Jetpack Compose and TensorFlow lite.

The application performs on-device object detection using the device camera and displays detected object labels, confidence scores and bounding boxes.

The project includes Google's pre-trained EfficientDet Lite0 Tensorflow Lite Model.

## Features

- Live camera preview
- Front Camera Support
- Back camera Support
- Camera Switching
- Real - time frame analysis
- TensorFlow Lite object detection
- Confidence score display
- Bounding box visualization
- On-device inference (No internet required)

## Tech Stack

- Kotlin
- Jetpack Compose
- Camera X
- TensorFlow lite
- ML model Binding
- Android SDK 36

## Architecture

Camera
   ↓
CameraX
   ↓
ImageProxy
   ↓
Bitmap Conversion
   ↓
TensorImage
   ↓
EfficientDet Lite0
   ↓
Detection Results
   ↓
Compose Overlay

## Screen shots

## Challenges

- Camera permission handling
- CameraX and compose integration
- ImageProxy to bitmap conversion
- Tensorflow lite dependency conflicts
- Bounding box co ordinate mapping

## Future work

- Improve bounding box alignment
- Object tracking across frames
- Upgrade to EfficeintDet Lite2/Lite3
- Custom trained models
- Performance Optimization

