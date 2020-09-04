package com.mancel.yann.poseanalyser.views.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.View
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.google.common.util.concurrent.ListenableFuture
import com.mancel.yann.poseanalyser.analysers.MLKitPoseAnalyzer
import com.mancel.yann.poseanalyser.lifecycles.ExecutorLifecycleObserver
import com.mancel.yann.poseanalyser.lifecycles.FullScreenLifecycleObserver
import com.mancel.yann.poseanalyser.R
import com.mancel.yann.poseanalyser.models.KeyPointOfPose
import com.mancel.yann.poseanalyser.states.CameraState
import com.mancel.yann.poseanalyser.states.ScanState
import com.mancel.yann.poseanalyser.utils.MessageTools
import com.mancel.yann.poseanalyser.viewModels.SharedViewModel
import kotlinx.android.synthetic.main.fragment_camera_x.view.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Created by Yann MANCEL on 04/09/2020.
 * Name of the project: PoseAnalyser
 * Name of the package: com.mancel.yann.poseanalyser.views.fragments
 *
 * A [BaseFragment] subclass.
 */
@androidx.camera.core.ExperimentalGetImage
class CameraXFragment : BaseFragment() {

    /*
        See GitHub example:
            [1]: https://github.com/android/camera-samples/tree/master/CameraXBasic

        See CameraX's uses cases
            [2]: https://developer.android.com/training/camerax/preview
            [3]: https://developer.android.com/training/camerax/analyze
     */

    // FIELDS --------------------------------------------------------------------------------------

    private val _viewModel: SharedViewModel by activityViewModels()

    private lateinit var _currentCameraState: CameraState

    private lateinit var _cameraProviderFuture : ListenableFuture<ProcessCameraProvider>
    private var _camera: Camera? = null
    private var _preview: Preview? = null
    private var _imageAnalysis: ImageAnalysis? = null

    // Blocking camera operations are performed using this executor
    private lateinit var _cameraExecutor: ExecutorLifecycleObserver

    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }

    // METHODS -------------------------------------------------------------------------------------

    // -- BaseFragment --

    override fun getFragmentLayout(): Int = R.layout.fragment_camera_x

    override fun doOnCreateView() {
        this.configureFullScreenLifecycleObserver()
        this.configureExecutorLifecycleObserver()
        this.configureCameraState()
        this.configurePoseEvents()
    }

    override fun actionAfterPermission() = this.configureCameraX()

    // -- Fragment --

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Wait for the views to be properly laid out
        this._rootView.fragment_camera_preview.post {
            this._viewModel.changeCameraStateToSetupCamera()
        }
    }

    // -- LifecycleObserver --

    /**
     * Configures a [FullScreenLifecycleObserver]
     */
    private fun configureFullScreenLifecycleObserver() {
        this.lifecycle.addObserver(
            FullScreenLifecycleObserver(
                this.lifecycle,
                this.requireActivity()
            )
        )
    }

    /**
     * Configures a [FullScreenLifecycleObserver]
     */
    private fun configureExecutorLifecycleObserver() {
        this._cameraExecutor = ExecutorLifecycleObserver()
        this.lifecycle.addObserver(this._cameraExecutor)
    }

    // -- LiveData --

    /**
     * Configures the LiveData of [CameraState]
     */
    private fun configureCameraState() {
        this._viewModel
            .getCameraState()
            .observe(this.viewLifecycleOwner) { cameraState ->
                cameraState?.let {
                    this.updateUI(it)
                }
            }
    }

    /**
     * Configures the LiveData of pose events
     */
    private fun configurePoseEvents() {
        this._viewModel
            .getPose()
            .observe(this.viewLifecycleOwner) { pose ->
                pose?.let {
                    this.placeKeyPointsOfPose(it)
                }
            }
    }

    // -- CameraState --

    /**
     * Updates UI thanks to a [CameraState]
     * @param state a [CameraState]
     */
    private fun updateUI(state: CameraState) {
        // To update CameraSelector
        this._currentCameraState = state

        when (state) {
            is CameraState.SetupCamera -> this.handleStateSetupCamera()
            is CameraState.PreviewReady -> this.handleStatePreviewReady()
            is CameraState.Error -> this.handleStateError(state._errorMessage)
        }
    }

    /**
     * Handles the [CameraState.SetupCamera] state
     */
    private fun handleStateSetupCamera() = this.configureCameraX()

    /**
     * Handles the [CameraState.PreviewReady] state
     */
    private fun handleStatePreviewReady() { /* Do nothing here */ }

    /**
     * Handles the [CameraState.Error] state
     * @param errorMessage a [String] that contains the error message
     */
    private fun handleStateError(errorMessage: String) {
        MessageTools.showMessageWithSnackbar(
            this._rootView.fragment_camera_coordinator_layout,
            this.getString(R.string.error_camera, errorMessage)
        )
    }

    // -- CameraX --

    /**
     * Configures CameraX with Camera permission
     */
    private fun configureCameraX() {
        if (this.hasCameraPermission())
            this.configureCameraProvider()
        else
            this._viewModel.changeCameraStateToError(this.getString(R.string.no_permission))
    }

    /**
     * Configures the [ProcessCameraProvider] of CameraX
     */
    private fun configureCameraProvider() {
        this._cameraProviderFuture = ProcessCameraProvider.getInstance(this.requireContext())

        this._cameraProviderFuture.addListener(
            Runnable {
                val cameraProvider = this._cameraProviderFuture.get()
                this.bindAllUseCases(cameraProvider)
                this._viewModel.changeCameraStateToPreviewReady()
            },
            ContextCompat.getMainExecutor(this.requireContext())
        )
    }

    /**
     * Binds all use cases, [Preview] and [ImageAnalysis], to the Fragment's lifecycle
     * @param cameraProvider a [ProcessCameraProvider]
     */
    private fun bindAllUseCases(cameraProvider: ProcessCameraProvider) {
        // Metrics
        val metrics = DisplayMetrics().also {
            this._rootView.fragment_camera_preview.display.getRealMetrics(it)
        }

        // Rotation
        val rotation = this._rootView.fragment_camera_preview.display.rotation

        // Use case: Preview
        this._preview = this.buildPreview(
            this.getAspectRatio(metrics.widthPixels, metrics.heightPixels),
            rotation
        )

        // Use case: ImageAnalysis
        this._imageAnalysis = this.buildImageAnalysis(
            this.getResolution(),
            rotation
        )

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // Binds Preview to the Fragment's lifecycle
            this._camera = cameraProvider.bindToLifecycle(
                this.viewLifecycleOwner,
                this.buildCameraSelector(),
                this._preview, this._imageAnalysis
            )

            // Connects Preview to the view into xml file
            this._preview?.setSurfaceProvider(
                this._rootView.fragment_camera_preview.createSurfaceProvider()
            )
        } catch(e: Exception) {
            Log.e(
                this.javaClass.simpleName,
                "Use case binding failed: $e"
            )
        }
    }

    /**
     * Builds a [CameraSelector] of CameraX
     * @return a [CameraSelector]
     */
    private fun buildCameraSelector(): CameraSelector {
        return CameraSelector.Builder()
            .requireLensFacing(this._currentCameraState._lensFacing)
            .build()
    }

    /**
     * Builds [Preview] use case of CameraX
     * @param ratio     an [Int] that contains the ratio
     * @param rotation  an [Int] that contains the rotation
     * @return a [Preview]
     */
    private fun buildPreview(ratio: Int, rotation: Int): Preview {
        return Preview.Builder()
            .setTargetAspectRatio(ratio)
            .setTargetRotation(rotation)
            .build()
    }

    /**
     * Builds [ImageAnalysis] use case of CameraX
     * @param resolution    a [Size] that contains the resolution
     * @param rotation      an [Int] that contains the rotation
     * @return a [ImageAnalysis]
     */
    private fun buildImageAnalysis(resolution: Size, rotation: Int): ImageAnalysis {
        return ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetResolution(resolution)
            .setTargetRotation(rotation)
            .build()
            .also {
                it.setAnalyzer(
                    this._cameraExecutor._executor,
                    this.getAnalyzer()
                )
            }
    }

    // -- Ratio --

    /**
     *  [ImageAnalysis] requires enum value of [androidx.camera.core.AspectRatio].
     *  Currently it has values of 4:3 & 16:9.
     *
     *  Detecting the most suitable ratio for dimensions provided in @params by counting absolute
     *  of preview ratio to one of the provided values.
     *  @param width    an [Int] that contains the preview width
     *  @param height   an [Int] that contains the preview height
     *  @return suitable aspect ratio
     */
    private fun getAspectRatio(width: Int, height: Int): Int {
        /*
            Ex:
                width ..................................... 1080
                height .................................... 2400

                previewRatio .............................. 2400/1080 = 2.2

                Ratio 4/3 ................................. 1.3
                Ratio 16/9 ................................ 1.7

                Absolute of [previewRatio - Ratio 4/3] .... 0.8
                Absolute of [previewRatio - Ratio 16/9] ... 0.4
         */

        val previewRatio = max(width, height).toDouble() / min(width, height).toDouble()
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    // -- Resolution --

    /**
     * Gets the resolution to be in accordance with ML Kit
     * @return a [Size]
     */
    private fun getResolution() =
        // For ML Kit: 1280x720 or 1920x1080 -> Ratio: 1.7
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            Size(1080,1920)
        else
            Size(1920, 1080)

    // -- Analyzer --

    /**
     * Gets a [MLKitPoseAnalyzer]
     * @return a [ImageAnalysis.Analyzer]
     */
    private fun getAnalyzer(): ImageAnalysis.Analyzer {
        return MLKitPoseAnalyzer(MLKitPoseAnalyzer.ScanConfig.STREAMING_FRAMES) { scanState ->
            when (scanState) {
                is ScanState.SuccessScan -> {
                    val pose = scanState._pose
                    this._viewModel.setPose(pose)
                }

                is ScanState.FailedScan -> {
                    MessageTools.showMessageWithSnackbar(
                        this._rootView.fragment_camera_coordinator_layout,
                        this.getString(R.string.analyzer_failed_scan, scanState._exception.message)
                    )
                }
            }
        }
    }

    // -- Key points of pose --

    /**
     * Places the pose
     * @param pose a [List] of [KeyPointOfPose]
     */
    private fun placeKeyPointsOfPose(pose: List<KeyPointOfPose>) {
        if (pose.isEmpty())
            Log.d("TEST", "No person")
        else
            Log.d("TEST", "Person - ${pose[0]}")
    }
}