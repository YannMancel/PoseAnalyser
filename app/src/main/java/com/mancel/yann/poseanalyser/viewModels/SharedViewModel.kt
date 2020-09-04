package com.mancel.yann.poseanalyser.viewModels

import androidx.camera.core.CameraSelector
import androidx.lifecycle.*
import com.mancel.yann.poseanalyser.models.KeyPointOfPose
import com.mancel.yann.poseanalyser.states.CameraState

/**
 * Created by Yann MANCEL on 04/09/2020.
 * Name of the project: PoseAnalyser
 * Name of the package: com.mancel.yann.poseanalyser.viewModels
 *
 * A [ViewModel] subclass.
 */
class SharedViewModel :  ViewModel() {

    // FIELDS --------------------------------------------------------------------------------------

    private val _pose = MutableLiveData<List<KeyPointOfPose>>()

    private val _cameraState = MutableLiveData<CameraState>()
    private var _lensFacing: Int = CameraSelector.LENS_FACING_BACK

    // METHODS -------------------------------------------------------------------------------------

    // -- Pose --

    /**
     * Gets the [LiveData] of [List] of [KeyPointOfPose]
     */
    fun getPose(): LiveData<List<KeyPointOfPose>> = this._pose

    /**
     * Sets pose to the [MutableLiveData] of [List] of [KeyPointOfPose]
     * @param pose a [List] of [KeyPointOfPose]
     */
    fun setPose(pose: List<KeyPointOfPose>) {
        this._pose.value = pose
    }

    // -- CameraState --

    fun getCameraState(): LiveData<CameraState> = this._cameraState

    /**
     * Changes [CameraState] with [CameraState.SetupCamera] state
     */
    fun changeCameraStateToSetupCamera() {
        this._cameraState.value = CameraState.SetupCamera(
            this._lensFacing
        )
    }

    /**
     * Changes [CameraState] with [CameraState.PreviewReady] state
     */
    fun changeCameraStateToPreviewReady() {
        this._cameraState.value = CameraState.PreviewReady(
            this._lensFacing
        )
    }

    /**
     * Changes [CameraState] with [CameraState.Error] state
     */
    fun changeCameraStateToError(errorMessage: String) {
        this._cameraState.value = CameraState.Error(
            errorMessage,
            this._lensFacing
        )
    }
}