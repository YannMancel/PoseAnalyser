package com.mancel.yann.poseanalyser.models

import android.graphics.PointF

/**
 * Created by Yann MANCEL on 04/09/2020.
 * Name of the project: PoseAnalyser
 * Name of the package: com.mancel.yann.poseanalyser.models
 */
data class KeyPointOfPose(
    val _position: PointF,
    val _inFrameLikelihood: Float,
    val _type: Type
) {

    // ENUMS ---------------------------------------------------------------------------------------

    enum class Type {
        NOSE,
        LEFT_EYE_INNER,
        LEFT_EYE,
        LEFT_EYE_OUTER,
        RIGHT_EYE_INNER,
        RIGHT_EYE,
        RIGHT_EYE_OUTER,
        LEFT_EAR,
        RIGHT_EAR,
        LEFT_MOUTH,
        RIGHT_MOUTH,
        LEFT_SHOULDER,
        RIGHT_SHOULDER,
        LEFT_ELBOW,
        RIGHT_ELBOW,
        LEFT_WRIST,
        RIGHT_WRIST,
        LEFT_PINKY,
        RIGHT_PINKY,
        LEFT_INDEX,
        RIGHT_INDEX,
        LEFT_THUMB,
        RIGHT_THUMB,
        LEFT_HIP,
        RIGHT_HIP,
        LEFT_KNEE,
        RIGHT_KNEE,
        LEFT_ANKLE,
        RIGHT_ANKLE,
        LEFT_HEEL,
        RIGHT_HEEL,
        LEFT_FOOT_INDEX,
        RIGHT_FOOT_INDEX
    }
}