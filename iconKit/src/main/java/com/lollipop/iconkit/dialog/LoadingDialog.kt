package com.lollipop.iconkit.dialog

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.LinearInterpolator
import com.lollipop.iconcore.ui.IconImageView
import com.lollipop.iconkit.R
import com.lollipop.iconkit.dialog.base.InnerDialogProvider

/**
 * @author lollipop
 * @date 10/28/20 01:17
 * 加载对话框的实现类
 */
class LoadingDialog(private val nextIconProvider: NextIconProvider): InnerDialogProvider(),
    Animator.AnimatorListener,
    ValueAnimator.AnimatorUpdateListener {

    companion object {
        const val ANIMATION_DURATION = 1000L * 2
    }

    override val layoutId: Int
        get() = R.layout.kit_dialog_loading

    private var iconView: IconImageView? = null
    private var dialogWidth = 0
    private var leftOffset = 0

    private val animator = ValueAnimator().apply {
        addUpdateListener(this@LoadingDialog)
        addListener(this@LoadingDialog)
        interpolator = DecelerateAccelerateInterpolator()
        setFloatValues(0F, 1F)
        duration = ANIMATION_DURATION
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.RESTART
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        iconView = find(R.id.iconView)
        view.post {
            dialogWidth = view.width
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        iconView = null
    }

    override fun onStart() {
        super.onStart()
        iconView?.let {
            nextIconProvider.next(it)
        }
        iconView?.post {
            iconView?.let {
                leftOffset = it.width + it.left
            }
            animator.start()
        }
    }

    override fun onStop() {
        super.onStop()
        animator.cancel()
    }

    override fun onBackPressed(): Boolean {
        return true
    }

    override fun onAnimationStart(animation: Animator?) {}

    override fun onAnimationEnd(animation: Animator?) {}

    override fun onAnimationCancel(animation: Animator?) {}

    override fun onAnimationRepeat(animation: Animator?) {
        if (animation == animator) {
            iconView?.let {
                nextIconProvider.next(it)
            }
        }
    }

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        if (animation == animator) {
            val progress = animator.animatedValue as Float
            iconView?.let {
                val width = it.width
                val translationX = ((dialogWidth + width) * progress) - leftOffset
                it.translationX = translationX
            }
        }
    }

    private class DecelerateAccelerateInterpolator: LinearInterpolator() {
        override fun getInterpolation(input: Float): Float {
            // (((x - 0.5) * (x - 0.5) * (x - 0.5) * 8) + 1) / 2
            val a = input - 0.5F
            return (a * a * a * 8 + 1) / 2
        }
    }

    interface NextIconProvider {
        fun next(view: IconImageView)
    }

}