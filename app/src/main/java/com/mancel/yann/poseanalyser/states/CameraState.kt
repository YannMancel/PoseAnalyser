package com.mancel.yann.poseanalyser.states

/**
 * Created by Yann MANCEL on 04/09/2020.
 * Name of the project: PoseAnalyser
 * Name of the package: com.mancel.yann.poseanalyser.states
 */
sealed class CameraState(
    val _lensFacing: Int
) {

    // CLASSES -------------------------------------------------------------------------------------

    /**
     * State:  SetupCamera
     * Where:  CameraXFragment#onViewCreated
     * Why:    After post method of Preview widget
     */
    class SetupCamera(
        lensFacing: Int
    ) : CameraState(lensFacing)

    /**
     * State:  PreviewReady
     * Where:  CameraXFragment#configureCameraProvider
     * Why:    After to bind Preview use case
     */
    class PreviewReady(
        lensFacing: Int
    ) : CameraState(lensFacing)

    /**
     * State:  Error
     * Where:  CameraXFragment#configureCameraX
     * Why:    No permission
     */
    class Error(
        val _errorMessage: String,
        lensFacing: Int
    ) : CameraState(lensFacing)
}