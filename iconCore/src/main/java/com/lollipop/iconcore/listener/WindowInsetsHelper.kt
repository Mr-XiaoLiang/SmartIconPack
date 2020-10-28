package com.lollipop.iconcore.listener

import android.graphics.Rect
import android.util.Log
import android.view.View
import android.view.ViewGroup

/**
 * @author lollipop
 * @date 2020/5/13 23:05
 * 窗口的Insets变化时的辅助处理器
 */
class WindowInsetsHelper (
    private val self: View,
    private val ignoreRootGroup: Boolean = false,
    private val autoLayout: Boolean = true) {

    companion object {
        fun setMargin(self: View?, left: Int, top: Int, right: Int, bottom: Int) {
            self?:return
            val layoutParams = self.layoutParams
            if (layoutParams is ViewGroup.MarginLayoutParams) {
                layoutParams.setMargins(
                        left, top, right, bottom)
                self.layoutParams = layoutParams
            }
        }
    }

    private var rootParent: View? = null

    private val tempLocalArray = IntArray(2)
    private val tempBounds = Rect()
    private val windowBounds = Rect()
    private val tempOutSize = Rect()

    private val srcMargin = Rect()

    private val pendingInsetsList = ArrayList<PendingInsets>()

    private var insetsCallback: (WindowInsetsHelper.(Rect) -> Unit)? = null

    init {
        // 准备就绪后发起一次布局排版
        if (autoLayout) {
            self.post {
                self.requestLayout()
            }
        }
        if (self.isAttachedToWindow) {
            rootParent = findRootParent(self)
        }
        self.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewDetachedFromWindow(v: View?) {
                rootParent = null
            }

            override fun onViewAttachedToWindow(v: View?) {
                rootParent = findRootParent(self)
            }
        })
        self.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            if (pendingInsetsList.isNotEmpty()) {
                val pending = pendingInsetsList.removeAt(0)
                pendingTask.updateInfo(pending)
                pendingTask.run()
            }

        }
    }

    private val pendingTask = PendingTask(this)

    private class PendingTask(private val windowInsetsHelper: WindowInsetsHelper): Runnable {

        private var pendingInfo: PendingInsets? = null

        fun updateInfo(info: PendingInsets) {
            pendingInfo = info
        }

        override fun run() {
            pendingInfo?.let { pending ->
                if (pending.isPadding) {
                    windowInsetsHelper.updateByPadding(pending.target,
                        pending.left, pending.top,
                        pending.right, pending.bottom)
                } else {
                    windowInsetsHelper.updateByMargin(pending.target,
                        pending.left, pending.top,
                        pending.right, pending.bottom)
                }
            }
            pendingInfo = null
        }

    }

    fun updateByPadding(view: View, left: Int, top: Int, right: Int, bottom: Int) {
        if (checkParent(view)) {
            if (self.width < 1 && self.height < 1) {
                pendingInsetsList.add(PendingInsets(view, left, top, right, bottom, true))
                return
            }
            val insets = getViewInsets(left, top, right, bottom)
            val callback = insetsCallback
            if (callback != null) {
                callback(insets)
            } else {
                setInsetsByPadding(insets.left, insets.top, insets.right, insets.bottom)
            }
        }
    }

    fun updateByMargin(view: View, left: Int, top: Int, right: Int, bottom: Int) {
        if (checkParent(view)) {
            if (self.width < 1 && self.height < 1) {
                pendingInsetsList.add(PendingInsets(view, left, top, right, bottom, false))
                return
            }
            val insets = getViewInsets(left, top, right, bottom)
            val callback = insetsCallback
            if (callback != null) {
                callback(insets)
            } else {
                setInsetsByMargin(insets.left, insets.top, insets.right, insets.bottom)
            }
        }
    }

    fun setInsetsByPadding(left: Int, top: Int, right: Int, bottom: Int) {
        self.setPadding(left, top, right, bottom)
    }

    fun setInsetsByMargin(left: Int, top: Int, right: Int, bottom: Int) {
        val layoutParams = self.layoutParams
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            layoutParams.setMargins(
                    srcMargin.left + left,
                    srcMargin.top + top,
                    srcMargin.right + right,
                    srcMargin.bottom + bottom)
            self.layoutParams = layoutParams
        }
    }

    fun custom(callback: (WindowInsetsHelper.(Rect) -> Unit)?): WindowInsetsHelper {
        this.insetsCallback = callback
        return this
    }

    private fun getViewInsets(left: Int, top: Int, right: Int, bottom: Int): Rect {
//        self.getLocationInWindow(tempLocalArray)
        getLocationInRoot(tempLocalArray)
        tempBounds.set(0, 0, self.width, self.height)
        tempBounds.offset(tempLocalArray[0], tempLocalArray[1])
        windowBounds.set(rootParent?.left?:0, rootParent?.top?:0,
                rootParent?.right?:0, rootParent?.bottom?:0)
        if (windowBounds.isEmpty) {
            tempOutSize.set(0, 0, 0, 0)
        } else {
            tempOutSize.left = (left - tempBounds.left).limit()
            tempOutSize.top = (top - tempBounds.top).limit()
            tempOutSize.right = (right - (windowBounds.right - tempBounds.right)).limit()
            tempOutSize.bottom = (bottom - (windowBounds.bottom - tempBounds.bottom)).limit()
        }
        Log.d("getViewInsets", "${self.javaClass.simpleName}: [$left, $top - $right, $bottom], $tempOutSize")
        Log.d("getViewInsets", "${self.javaClass.simpleName}: windowBounds: $windowBounds, tempBounds: $tempBounds")
        return tempOutSize
    }

    private fun getLocationInRoot(intArray: IntArray) {
        val selfLoc = IntArray(2)
        self.getLocationInWindow(selfLoc)
        selfLoc[0] -= self.translationX.toInt()
        selfLoc[1] -= self.translationY.toInt()
        val rootLoc = IntArray(2) { 0 }
        rootParent?.getLocationInWindow(rootLoc)
        rootLoc[0] -= rootParent?.translationX?.toInt()?:0
        rootLoc[1] -= rootParent?.translationY?.toInt()?:0
        intArray[0] = selfLoc[0] - rootLoc[0]
        intArray[1] = selfLoc[1] - rootLoc[1]
    }

    private fun Int.limit(): Int {
        if (this < 0) {
            return 0
        }
        return this
    }

    /**
     * 检查是否时同一个宿主
     */
    private fun checkParent(view: View): Boolean {
        rootParent?:return ignoreRootGroup
        val parent = findRootParent(view)
        return parent == rootParent
    }

    fun baseMargin(left: Int, top: Int, right: Int, bottom: Int) {
        srcMargin.set(left, top, right, bottom)
    }

    fun baseMarginFromNow() {
        val layoutParams = self.layoutParams
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            baseMargin(layoutParams.leftMargin, layoutParams.topMargin,
                    layoutParams.rightMargin, layoutParams.bottomMargin)
        }
    }

    private fun findRootParent(view: View): View {
        var target = view
        while (target.parent != null) {
            val parent = target.parent
            if (parent is View) {
                target = parent
            } else {
                break
            }
        }
        return target
    }

    private data class PendingInsets(val target: View,
                                     val left: Int, val top: Int,
                                     val right: Int, val bottom: Int,
                                     val isPadding: Boolean)

}