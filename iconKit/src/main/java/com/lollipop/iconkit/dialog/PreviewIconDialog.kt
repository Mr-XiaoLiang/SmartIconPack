package com.lollipop.iconkit.dialog

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewManager
import com.lollipop.iconcore.listener.BackPressedListener
import com.lollipop.iconcore.ui.IconImageView
import com.lollipop.iconkit.R
import com.lollipop.iconkit.dialog.base.CurtainDialog

/**
 * @author lollipop
 * @date 10/26/20 23:09
 * 预览Icon的Dialog
 */
class PreviewIconDialog: BackPressedListener,
    ValueAnimator.AnimatorUpdateListener,
    Animator.AnimatorListener {

    private var dialogView: View? = null

    private var targetView: View? = null

    private val locationOffset = IntArray(2)

    private val sizeOffset = FloatArray(2)

    private val backgroundView: View?
        get() {
            return dialogView?.findViewById(R.id.backgroundView)
        }

    private val previewView: IconImageView?
        get() {
            return dialogView?.findViewById(R.id.iconView)
        }

    private val leftPanel: View?
        get() {
            return dialogView?.findViewById(R.id.leftPanel)
        }

    private val rightPanel: View?
        get() {
            return dialogView?.findViewById(R.id.rightPanel)
        }

    private var progress = 0F

    private val animator = ValueAnimator().apply {
        addUpdateListener(this@PreviewIconDialog)
        addListener(this@PreviewIconDialog)
    }

    fun attach(context: Activity) {
        remove()
        val rootGroup = CurtainDialog.findGroup(context.window.decorView)
        val view = LayoutInflater.from(context).inflate(
            R.layout.kit_preview_dialog, rootGroup, false)
        dialogView = view
        view.setOnClickListener {
            dismiss()
        }
        view.visibility = View.INVISIBLE
        rootGroup?.addView(view)
    }

    fun onDestroy() {
        remove()
    }

    private fun remove() {
        animator.cancel()
        targetView?.visibility = View.VISIBLE
        targetView = null
        dialogView?.let { dialog ->
            dialog.parent?.let {
                if (it is ViewManager) {
                    it.removeView(dialog)
                }
            }
        }
        dialogView = null
    }

    fun show(view: View, icon: Int) {
        val preview = previewView?:return
        targetView = view

        progress = 0F
        preview.apply {
            scaleX = 1F
            scaleY = 1F
            translationX = 0F
            translationY = 0F
        }
        leftPanel?.let {
            it.translationX = it.width.toFloat() * -1
        }
        rightPanel?.let {
            it.translationX = it.width.toFloat()
        }
        preview.loadIcon(icon)
        initInfo(view, preview)
        view.visibility = View.INVISIBLE
        doAnimation(true)
    }

    private fun initInfo(view: View, preview: View) {
        sizeOffset[0] = view.width * 1F / preview.width
        sizeOffset[1] = view.height * 1F / preview.height

        locationOffset[0] = 0
        locationOffset[1] = 0
        getLocationOffset(preview, view, locationOffset)
    }

    private fun getLocationOffset(self: View, target: View, intArray: IntArray) {
        val selfLoc = IntArray(2)
        self.getLocationInWindow(selfLoc)
        selfLoc[0] -= self.translationX.toInt()
        selfLoc[1] -= self.translationY.toInt()
        selfLoc[0] += self.width / 2
        selfLoc[1] += self.height / 2
        val targetLoc = IntArray(2)
        target.getLocationInWindow(targetLoc)
        targetLoc[0] -= target.translationX.toInt()
        targetLoc[1] -= target.translationY.toInt()
        targetLoc[0] += target.width / 2
        targetLoc[1] += target.height / 2
        intArray[0] = targetLoc[0] - selfLoc[0]
        intArray[1] = targetLoc[1] - selfLoc[1]
    }

    private fun dismiss() {
        doAnimation(false)
        closePanel()
    }

    private fun doAnimation(isShow: Boolean) {
        val endValue = if (isShow) { 1F } else { 0F }
        animator.cancel()
        animator.setFloatValues(progress, endValue)
        animator.start()
    }

    override fun onBackPressed(): Boolean {
        if (dialogView?.visibility == View.VISIBLE) {
            dismiss()
            return true
        }
        return false
    }

    private fun onUpdate() {
        val preview = previewView?:return
        val background = backgroundView?:return
        preview.apply {
            val x = (1 - sizeOffset[0]) * progress + sizeOffset[0]
            val y = (1 - sizeOffset[1]) * progress + sizeOffset[1]
            scaleX = x
            scaleY = y
            translationX = locationOffset[0] * (1 - progress)
            translationY = locationOffset[1] * (1 - progress)
        }
        background.alpha = progress
    }

    private fun closePanel() {
        val left = leftPanel?:return
        left.animate()?.apply {
            cancel()
            translationX(left.width.toFloat() * -1)
            start()
        }
        val right = rightPanel?:return
        right.animate()?.apply {
            cancel()
            translationX(right.width.toFloat())
            start()
        }
    }

    private fun openPanel() {
        leftPanel?.animate()?.apply {
            cancel()
            translationX(0F)
            start()
        }
        rightPanel?.animate()?.apply {
            cancel()
            translationX(0F)
            start()
        }
    }

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        if (animation == animator) {
            progress = animation.animatedValue as Float
            onUpdate()
        }
    }

    override fun onAnimationStart(animation: Animator?) {
        if (animation == animator && dialogView?.visibility != View.VISIBLE) {
            dialogView?.visibility = View.VISIBLE
        }
    }

    override fun onAnimationEnd(animation: Animator?) {
        if (animation == animator) {
            if (progress < 0.1F) {
                dialogView?.visibility = View.INVISIBLE
                targetView?.visibility = View.VISIBLE
            }
            if (progress > 0.9F) {
                openPanel()
            }
        }
    }

    override fun onAnimationCancel(animation: Animator?) {
    }

    override fun onAnimationRepeat(animation: Animator?) {
    }

}