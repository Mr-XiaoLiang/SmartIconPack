package com.lollipop.iconcore.listener

import android.graphics.Rect
import android.util.Log
import android.view.View
import android.view.ViewGroup

/**
 * @author lollipop
 * @date 2020/5/13 23:05
 * 窗口的Insets变化时的辅助处理器
 *
 * 它可以帮助你快速处理窗口的缩进事件
 * 它主要提供了padding和margin两种处理方式
 * 同时，它会自动发起布局事件（你可以选择关闭它），触发布局排版以此来计算需要缩进的尺寸
 *
 * 它会对比事件来源的rootView是否与当前绑定的View存在包含关系，
 * 如果存在，那么将会进一步计算当前View与系统缩进部分的重叠区域，
 * 并且自动规避这部分区域，如果当前View尚未完成排版，它会把事件保留
 * 并在View准备就绪后再次尝试排版
 *
 */
class WindowInsetsHelper (
    private val self: View,
    private val ignoreRootGroup: Boolean = false,
    autoLayout: Boolean = true) {

    companion object {
        /**
         * 一个开放的设置外补白的方法
         * 它主要方便为某些不适合自动化设置外补白的场景使用
         * 它包含了必要来判定
         */
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
    private val srcPadding = Rect()

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

    /**
     * 以内补白的形式更新布局
     * @param view 这是onInsets()事件中的root，
     * 表示这是事件的来源，用于区分View树，并且决定是否响应事件
     *
     * 参数位置与定义同{@link OnWindowInsetsListener#onInsetsChange}
     */
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

    /**
     * 以外补白的形式更新布局
     * @param view 这是onInsets()事件中的root，
     * 表示这是事件的来源，用于区分View树，并且决定是否响应事件
     *
     * 参数位置与定义同{@link OnWindowInsetsListener#onInsetsChange}
     */
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

    /**
     * 直接对当前绑定的View设置内补白
     * 但是它会叠加基础的内补白信息
     * 主要用于已存在内补白的情况下，
     * 希望保留间距的情况下叠加内补白
     * 请见{@link #basePaddingFromNow() }
     */
    fun setInsetsByPadding(left: Int, top: Int, right: Int, bottom: Int) {
        self.setPadding(
            srcPadding.left + left,
            srcPadding.top + top,
            srcPadding.right + right,
            srcPadding.bottom + bottom)
    }

    /**
     * 直接对当前绑定的View设置外补白
     * 但是它会叠加基础的外补白信息
     * 主要用于已存在外补白的情况下，
     * 希望保留间距的情况下叠加外补白
     * 请见{@link #baseMarginFromNow() }
     */
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

    /**
     * 设置自定义的窗口缩进处理回调
     * 如果设置了它，那么将不会再自动设置内补白或者外补白
     * 它会传出缩进的计算结果，你可以按照需要自行处理
     */
    fun custom(callback: (WindowInsetsHelper.(Rect) -> Unit)?): WindowInsetsHelper {
        this.insetsCallback = callback
        return this
    }

    private fun getViewInsets(left: Int, top: Int, right: Int, bottom: Int): Rect {
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

    /**
     * 手动设置基础的margin值
     * 后续的外补白设置都会在此值上叠加，
     * 默认情况下，他们都会是0
     */
    fun baseMargin(left: Int, top: Int, right: Int, bottom: Int) {
        srcMargin.set(left, top, right, bottom)
    }

    /**
     * 手动设置基础的padding值
     * 后续的内补白设置都会在此值上叠加，
     * 默认情况下，他们都会是0
     */
    fun basePadding(left: Int, top: Int, right: Int, bottom: Int) {
        srcPadding.set(left, top, right, bottom)
    }

    /**
     * 记录当前的外补白值作为基础值
     * 后续的外补白设置都会在此基础上叠加
     */
    fun baseMarginFromNow() {
        val layoutParams = self.layoutParams
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            baseMargin(layoutParams.leftMargin, layoutParams.topMargin,
                    layoutParams.rightMargin, layoutParams.bottomMargin)
        }
    }

    /**
     * 记录当前的内补白值作为基础值
     * 后续的内补白设置都会在此基础上叠加
     */
    fun basePaddingFromNow() {
        basePadding(self.paddingLeft, self.paddingTop,
            self.paddingRight, self.paddingBottom)
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