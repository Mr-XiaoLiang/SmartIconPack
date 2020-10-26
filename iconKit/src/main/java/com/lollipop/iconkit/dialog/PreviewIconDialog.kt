package com.lollipop.iconkit.dialog

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewManager
import com.lollipop.iconcore.listener.BackPressedListener
import com.lollipop.iconcore.ui.IconImageView
import com.lollipop.iconcore.util.lifecycleBinding
import com.lollipop.iconcore.util.onEnd
import com.lollipop.iconkit.R
import com.lollipop.iconkit.dialog.base.CurtainDialog

/**
 * @author lollipop
 * @date 10/26/20 23:09
 * 预览Icon的Dialog
 */
class PreviewIconDialog: BackPressedListener {

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

    fun attach(context: Activity) {
        remove()
        val rootGroup = CurtainDialog.findGroup(context.window.decorView)
        val view = LayoutInflater.from(context).inflate(
            R.layout.kit_preview_dialog, rootGroup, false)
        dialogView = view

        view.visibility = View.INVISIBLE

        view.setOnClickListener {
            dismiss()
        }
        rootGroup?.addView(view)
    }

    fun onDestroy() {
        remove()
    }

    private fun remove() {
        backgroundView?.animate()?.cancel()
        previewView?.animate()?.cancel()
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
        val dialog = dialogView?:return
        val preview = previewView?:return
        val background = backgroundView?:return

        background.alpha = 0F
        background.animate().apply {
            cancel()
            alpha(1F)
            start()
        }

        sizeOffset[0] = view.width * 1F / preview.width
        sizeOffset[1] = view.height * 1F / preview.height

        getLocationInRoot(preview, view, locationOffset)

        preview.apply {
            scaleX = sizeOffset[0]
            scaleY = sizeOffset[1]
            translationX = locationOffset[0].toFloat()
            translationY = locationOffset[1].toFloat()
            loadIcon(icon)
        }
        preview.animate().apply {
            cancel()
            scaleX(1F)
            scaleY(1F)
            translationX(0F)
            translationY(0F)
            start()
        }

//        view.visibility = View.INVISIBLE

        dialog.visibility = View.VISIBLE
    }

    private fun getLocationInRoot(self: View, target: View, intArray: IntArray) {
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
        val preview = previewView?:return
        val background = backgroundView?:return

        background.animate().apply {
            cancel()
            alpha(0F)
            start()
        }

        preview.animate().apply {
            cancel()
            scaleX(sizeOffset[0])
            scaleY(sizeOffset[1])
            translationX(locationOffset[0].toFloat())
            translationY(locationOffset[1].toFloat())
            lifecycleBinding {
                onEnd {
                    removeThis(it)
                    targetView?.visibility = View.VISIBLE
                    targetView = null
                    dialogView?.visibility = View.INVISIBLE
                }
            }
            start()
        }
    }

    override fun onBackPressed(): Boolean {
        if (dialogView?.visibility == View.VISIBLE) {
            dismiss()
            return true
        }
        return false
    }

}