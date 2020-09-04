package com.mancel.yann.poseanalyser.analysers

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetectorOptions
import com.google.mlkit.vision.pose.PoseLandmark
import com.mancel.yann.poseanalyser.models.KeyPointOfPose
import com.mancel.yann.poseanalyser.states.ScanState

/**
 * Created by Yann MANCEL on 04/09/2020.
 * Name of the project: PoseAnalyser
 * Name of the package: com.mancel.yann.poseanalyser.analyzers
 *
 * A class which implements [ImageAnalysis.Analyzer].
 */
@androidx.camera.core.ExperimentalGetImage
class MLKitPoseAnalyzer(
    private val _config: ScanConfig,
    private val _actionOnScanResult: (ScanState) -> Unit
) : ImageAnalysis.Analyzer {

    /*
        See ML Kit:
            [1]: https://developers.google.com/ml-kit/vision/pose-detection/android
     */

    // ENUMS ---------------------------------------------------------------------------------------

    enum class ScanConfig { STATIC_IMAGE, STREAMING_FRAMES }

    // METHODS -------------------------------------------------------------------------------------

    // -- ImageAnalysis.Analyzer interface --

    override fun analyze(image: ImageProxy) {
        // Warning: Do not forget to close the ImageProxy
        this.analyseImage(image)
    }

    // -- ML Kit - Pose scanner --

    /**
     * Analyses an [ImageProxy] in parameter with the [ImageAnalysis] use case of CameraX
     * @param imageProxy an [ImageProxy]
     */
    private fun analyseImage(imageProxy: ImageProxy) {
        // Creates an InputImage object from a media.Image object
        imageProxy.image?.let {
            // InputImage
            val image = InputImage.fromMediaImage(it, imageProxy.imageInfo.rotationDegrees)

            // Pass image to an ML Kit Vision API
            this.scanPose(image) {
                imageProxy.close()
            }
        } ?: imageProxy.close()
    }

    /**
     * Scans the pose thanks to ML Kit
     * @param image an [InputImage]
     */
    private fun scanPose(image: InputImage, actionOnComplete: () -> Unit) {
        // Options
        val options = when (this._config) {
            ScanConfig.STATIC_IMAGE -> this.configureStaticImage()
            ScanConfig.STREAMING_FRAMES -> this.configureStreamingFrames()
        }

        // PoseDetection
        val poseDetector = PoseDetection.getClient(options)

        // Result
        poseDetector.process(image)
            .addOnSuccessListener { pose ->
                val data = this.convertPoseFromMLKitToApp(pose)
                this._actionOnScanResult(
                    ScanState.SuccessScan(data)
                )
            }
            .addOnFailureListener { exception ->
                this._actionOnScanResult(
                    ScanState.FailedScan(exception)
                )
            }
            .addOnCompleteListener {
                actionOnComplete()
            }
    }

    // -- PoseDetectorOptions --

    /**
     * Configures the [PoseDetectorOptions] for static images.
     * @return a [PoseDetectorOptions]
     */
    private fun configureStaticImage(): PoseDetectorOptions =
        PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.SINGLE_IMAGE_MODE)
            .setPerformanceMode(PoseDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .build()

    /**
     * Configures the [PoseDetectorOptions] for streaming frames.
     * @return a [PoseDetectorOptions]
     */
    private fun configureStreamingFrames(): PoseDetectorOptions =
        PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .setPerformanceMode(PoseDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()

    // -- Pose conversion --

    /**
     * Converts the pose from ML Kit library to this application (abstract layer)
     */
    private fun convertPoseFromMLKitToApp(pose: Pose): List<KeyPointOfPose> {
        val allPoseLandmarks = pose.allPoseLandmarks

        // No person was detected
        if (allPoseLandmarks.isEmpty()) return emptyList()

        // Person is detected in the image
        // The Pose Detection API returns a Pose object with 33 PoseLandmarks
        return mutableListOf<KeyPointOfPose>().also { newData ->
            allPoseLandmarks.forEach { poseLandmark ->
                newData.add(this.createNewKeyPointOfPose(poseLandmark))
            }
        }
    }

    // -- Type --

    /**
     * Creates a new [KeyPointOfPose] from ML Kit library to this application (abstract layer)
     */
    private fun createNewKeyPointOfPose(poseLandmark: PoseLandmark): KeyPointOfPose =
        KeyPointOfPose(
            poseLandmark.position,
            poseLandmark.inFrameLikelihood,
            this.getType(poseLandmark.landmarkType)
        )

    // -- Type --

    /**
     * Gets the [PoseLandmark]'s type from ML Kit library to this application (abstract layer)
     */
    private fun getType(landmarkType: PoseLandmark.Type): KeyPointOfPose.Type {
        return when (landmarkType) {
            PoseLandmark.Type.NOSE -> KeyPointOfPose.Type.NOSE
            PoseLandmark.Type.LEFT_EYE_INNER -> KeyPointOfPose.Type.LEFT_EYE_INNER
            PoseLandmark.Type.LEFT_EYE -> KeyPointOfPose.Type.LEFT_EYE
            PoseLandmark.Type.LEFT_EYE_OUTER -> KeyPointOfPose.Type.LEFT_EYE_OUTER
            PoseLandmark.Type.RIGHT_EYE_INNER -> KeyPointOfPose.Type.RIGHT_EYE_INNER
            PoseLandmark.Type.RIGHT_EYE -> KeyPointOfPose.Type.RIGHT_EYE
            PoseLandmark.Type.RIGHT_EYE_OUTER -> KeyPointOfPose.Type.RIGHT_EYE_OUTER
            PoseLandmark.Type.LEFT_EAR -> KeyPointOfPose.Type.LEFT_EAR
            PoseLandmark.Type.RIGHT_EAR -> KeyPointOfPose.Type.RIGHT_EAR
            PoseLandmark.Type.LEFT_MOUTH -> KeyPointOfPose.Type.LEFT_MOUTH
            PoseLandmark.Type.RIGHT_MOUTH -> KeyPointOfPose.Type.RIGHT_MOUTH
            PoseLandmark.Type.LEFT_SHOULDER -> KeyPointOfPose.Type.LEFT_SHOULDER
            PoseLandmark.Type.RIGHT_SHOULDER -> KeyPointOfPose.Type.RIGHT_SHOULDER
            PoseLandmark.Type.LEFT_ELBOW -> KeyPointOfPose.Type.LEFT_ELBOW
            PoseLandmark.Type.RIGHT_ELBOW -> KeyPointOfPose.Type.RIGHT_ELBOW
            PoseLandmark.Type.LEFT_WRIST -> KeyPointOfPose.Type.LEFT_WRIST
            PoseLandmark.Type.RIGHT_WRIST -> KeyPointOfPose.Type.RIGHT_WRIST
            PoseLandmark.Type.LEFT_PINKY -> KeyPointOfPose.Type.LEFT_PINKY
            PoseLandmark.Type.RIGHT_PINKY -> KeyPointOfPose.Type.RIGHT_PINKY
            PoseLandmark.Type.LEFT_INDEX -> KeyPointOfPose.Type.LEFT_INDEX
            PoseLandmark.Type.RIGHT_INDEX -> KeyPointOfPose.Type.RIGHT_INDEX
            PoseLandmark.Type.LEFT_THUMB -> KeyPointOfPose.Type.LEFT_THUMB
            PoseLandmark.Type.RIGHT_THUMB -> KeyPointOfPose.Type.RIGHT_THUMB
            PoseLandmark.Type.LEFT_HIP -> KeyPointOfPose.Type.LEFT_HIP
            PoseLandmark.Type.RIGHT_HIP -> KeyPointOfPose.Type.RIGHT_HIP
            PoseLandmark.Type.LEFT_KNEE -> KeyPointOfPose.Type.LEFT_KNEE
            PoseLandmark.Type.RIGHT_KNEE -> KeyPointOfPose.Type.RIGHT_KNEE
            PoseLandmark.Type.LEFT_ANKLE -> KeyPointOfPose.Type.LEFT_ANKLE
            PoseLandmark.Type.RIGHT_ANKLE -> KeyPointOfPose.Type.RIGHT_ANKLE
            PoseLandmark.Type.LEFT_HEEL -> KeyPointOfPose.Type.LEFT_HEEL
            PoseLandmark.Type.RIGHT_HEEL -> KeyPointOfPose.Type.RIGHT_HEEL
            PoseLandmark.Type.LEFT_FOOT_INDEX -> KeyPointOfPose.Type.LEFT_FOOT_INDEX
            PoseLandmark.Type.RIGHT_FOOT_INDEX -> KeyPointOfPose.Type.RIGHT_FOOT_INDEX
        }
    }
}