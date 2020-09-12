package com.mancel.yann.poseanalyser.states

import com.mancel.yann.poseanalyser.models.KeyPointOfPose

/**
 * Created by Yann MANCEL on 04/09/2020.
 * Name of the project: PoseAnalyser
 * Name of the package: com.mancel.yann.poseanalyser.states
 */
sealed class ScanState {

    // CLASSES -------------------------------------------------------------------------------------

    /**
     * State:  SuccessScan
     * Where:  MLKitPoseAnalyzer#scanPose
     * Why:    Scan is a success
     */
    class SuccessScan(
        val _pose: List<KeyPointOfPose>,
        val _imageWidth: Int,
        val _imageHeight: Int
    ) : ScanState()

    /**
     * State:  FailedScan
     * Where:  MLKitPoseAnalyzer#scanPose
     * Why:    Scan is a fail
     */
    class FailedScan(val _exception: Exception) : ScanState()
}