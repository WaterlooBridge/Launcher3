package com.android.launcher3.uioverrides.touchcontrollers

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherAppTransitionManagerImpl
import com.android.launcher3.LauncherState
import com.android.launcher3.LauncherStateManager
import com.android.launcher3.anim.AnimatorSetBuilder
import com.android.launcher3.anim.Interpolators
import com.android.launcher3.touch.AbstractStateChangeTouchController
import com.android.launcher3.touch.SingleAxisSwipeDetector
import com.android.launcher3.userevent.nano.LauncherLogProto
import com.android.launcher3.util.VibratorWrapper
import com.android.quickstep.OverviewInteractionState
import com.android.quickstep.util.MotionPauseDetector
import com.android.quickstep.views.RecentsView
import com.android.systemui.shared.system.QuickStepContract

/**
 * Created by lin on 2020/10/8.
 */
class NoAllAppsTouchController(l: Launcher) : AbstractStateChangeTouchController(l, SingleAxisSwipeDetector.VERTICAL) {

    companion object {
        private const val PEEK_IN_ANIM_DURATION: Long = 240
        private const val PEEK_OUT_ANIM_DURATION: Long = 100
        private const val MAX_DISPLACEMENT_PERCENT = 0.75f
    }

    private var mMotionPauseDetector: MotionPauseDetector = MotionPauseDetector(l)
    private var mMotionPauseMinDisplacement = ViewConfiguration.get(l).scaledTouchSlop.toFloat()
    private var mMotionPauseMaxDisplacement = shiftRange * MAX_DISPLACEMENT_PERCENT

    private var mPeekAnim: AnimatorSet? = null

    override fun canInterceptTouch(ev: MotionEvent?): Boolean {
        return mLauncher.isInState(LauncherState.NORMAL)
    }

    override fun getTargetState(fromState: LauncherState, isDragTowardPositive: Boolean): LauncherState {
        if (fromState == LauncherState.ALL_APPS && !isDragTowardPositive)
            return LauncherState.NORMAL
        else if (fromState == LauncherState.NORMAL && isDragTowardPositive)
            return LauncherState.ALL_APPS
        return fromState
    }

    override fun initCurrentAnimation(animComponents: Int): Float {
        val range = shiftRange
        val maxAccuracy = (2 * range).toLong()

        val startVerticalShift = mFromState.getVerticalProgress(mLauncher) * range
        val endVerticalShift = mToState.getVerticalProgress(mLauncher) * range

        val totalShift = endVerticalShift - startVerticalShift

        val builder = if (totalShift == 0f) AnimatorSetBuilder() else getAnimatorSetBuilderForStates(mFromState, mToState)
        if (handlingOverviewAnim()) {
            // We don't want the state transition to all apps to animate overview,
            // as that will cause a jump after our atomic animation.
            builder.addFlag(AnimatorSetBuilder.FLAG_DONT_ANIMATE_OVERVIEW)
        }

        mCurrentAnimation = mLauncher.stateManager
                .createAnimationToNewWorkspace(mToState, builder, maxAccuracy, { clearState() },
                        animComponents and LauncherStateManager.NON_ATOMIC_COMPONENT.inv())

        return 1 / totalShift
    }

    override fun getLogContainerTypeForNormalState(ev: MotionEvent?): Int {
        return LauncherLogProto.ContainerType.WORKSPACE
    }

    override fun getAtomicDuration(): Long {
        return LauncherAppTransitionManagerImpl.ATOMIC_DURATION_FROM_PAUSED_TO_OVERVIEW
    }

    private fun handlingOverviewAnim(): Boolean {
        val stateFlags = OverviewInteractionState.INSTANCE[mLauncher].systemUiStateFlags
        return mStartState === LauncherState.NORMAL && stateFlags and QuickStepContract.SYSUI_STATE_OVERVIEW_DISABLED == 0
    }

    override fun getAnimatorSetBuilderForStates(fromState: LauncherState?, toState: LauncherState?): AnimatorSetBuilder {
        if (fromState === LauncherState.NORMAL && toState === LauncherState.ALL_APPS) {
            val builder = AnimatorSetBuilder()
            // Get workspace out of the way quickly, to prepare for potential pause.
            builder.setInterpolator(AnimatorSetBuilder.ANIM_WORKSPACE_SCALE, Interpolators.DEACCEL_3)
            builder.setInterpolator(AnimatorSetBuilder.ANIM_WORKSPACE_TRANSLATE, Interpolators.DEACCEL_3)
            builder.setInterpolator(AnimatorSetBuilder.ANIM_WORKSPACE_FADE, Interpolators.DEACCEL_3)
            return builder
        }
        return super.getAnimatorSetBuilderForStates(fromState, toState)
    }

    override fun onDragStart(start: Boolean) {
        mMotionPauseDetector.clear()

        super.onDragStart(start)

        if (handlingOverviewAnim()) {
            mMotionPauseDetector.setOnMotionPauseListener { isPaused: Boolean ->
                val recentsView = mLauncher.getOverviewPanel<RecentsView<*>>()
                recentsView.setOverviewStateEnabled(isPaused)
                mPeekAnim?.cancel()
                val fromState = if (isPaused) LauncherState.NORMAL else LauncherState.OVERVIEW_PEEK
                val toState = if (isPaused) LauncherState.OVERVIEW_PEEK else LauncherState.NORMAL
                val peekDuration = if (isPaused) PEEK_IN_ANIM_DURATION else PEEK_OUT_ANIM_DURATION
                mPeekAnim = mLauncher.stateManager.createAtomicAnimation(fromState, toState,
                        AnimatorSetBuilder(), LauncherStateManager.ATOMIC_OVERVIEW_PEEK_COMPONENT, peekDuration)
                mPeekAnim?.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        mPeekAnim = null
                    }
                })
                mPeekAnim?.start()
                VibratorWrapper.INSTANCE[mLauncher].vibrate(VibratorWrapper.OVERVIEW_HAPTIC)
                mLauncher.dragLayer.scrim.animateToSysuiMultiplier(if (isPaused) 0f else 1f,
                        peekDuration, 0)
            }
        }
    }

    override fun onDrag(displacement: Float, ev: MotionEvent): Boolean {
        val upDisplacement = -displacement
        mMotionPauseDetector.setDisallowPause(upDisplacement < mMotionPauseMinDisplacement
                || upDisplacement > mMotionPauseMaxDisplacement)
        mMotionPauseDetector.addPosition(displacement, ev.eventTime)
        return super.onDrag(displacement, ev)
    }

    override fun onDragEnd(velocity: Float) {
        if (mMotionPauseDetector.isPaused && handlingOverviewAnim()) {
            if (mPeekAnim != null) {
                mPeekAnim!!.cancel()
            }
            val overviewAnim = mLauncher.appTransitionManager.createStateElementAnimation(
                    LauncherAppTransitionManagerImpl.INDEX_PAUSE_TO_OVERVIEW_ANIM)
            overviewAnim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onSwipeInteractionCompleted(LauncherState.OVERVIEW, LauncherLogProto.Action.Touch.SWIPE)
                }
            })
            overviewAnim.start()
        } else {
            super.onDragEnd(velocity)
        }
        mMotionPauseDetector.clear()
    }

    override fun goToTargetState(targetState: LauncherState?, logAction: Int) {
        if (mPeekAnim != null && mPeekAnim!!.isStarted) {
            // Don't jump to the target state until overview is no longer peeking.
            mPeekAnim!!.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super@NoAllAppsTouchController.goToTargetState(targetState, logAction)
                }
            })
        } else {
            super.goToTargetState(targetState, logAction)
        }
    }
}