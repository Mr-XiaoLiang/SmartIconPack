package com.lollipop.iconcore.listener

import android.graphics.Rect
import android.view.View
import java.lang.ref.SoftReference

/**
 * @author lollipop
 * @date 2020/5/13 23:46
 * 窗口缩进信息提供者的辅助分发类
 * 它提供了一个一转多的事件分发功能
 * 可以接受窗口缩进信息并且分发出去
 *
 * 同时具有数据暂存的功能，会保存最后一次的缩进信息
 * 当有新的监听者加入时，会自动发送最后一次的数据
 */
class WindowInsetsProviderHelper: OnWindowInsetsProvider, OnWindowInsetsListener {

    private val lastWindowInsets = Rect()
    private var rootView: SoftReference<View>? = null

    private val windowInsetsListenerList = ArrayList<OnWindowInsetsListener>()

    override fun addOnWindowInsetsProvider(listener: OnWindowInsetsListener) {
        windowInsetsListenerList.add(listener)
        rootView?.get()?.let { root ->
            listener.onInsetsChange(root, lastWindowInsets.left, lastWindowInsets.top,
                    lastWindowInsets.right, lastWindowInsets.bottom)
        }
    }

    override fun removeOnWindowInsetsProvider(listener: OnWindowInsetsListener) {
        windowInsetsListenerList.remove(listener)
    }

    override fun onInsetsChange(root: View, left: Int, top: Int, right: Int, bottom: Int) {
        rootView = SoftReference(root)
        lastWindowInsets.set(left, top, right, bottom)
        windowInsetsListenerList.forEach {
            it.onInsetsChange(root, left, top, right, bottom)
        }
    }

    /**
     * 销毁引用的方法
     * 当生命周期结束时，请务必触发此方法来移除引用
     * 释放资源，避免不必要的内存泄漏
     */
    fun destroy() {
        lastWindowInsets.set(0, 0, 0, 0)
        rootView = null
        windowInsetsListenerList.clear()
    }

    /**
     * 主动触发一次窗口缩进事件（如果有）
     * 此方法主要用于过早注册监听的场景，可能导致遗漏缩进信息
     * 此时可以通过它回溯最后一次的缩进信息
     */
    fun call(listener: OnWindowInsetsListener) {
        rootView?.get()?.let { root ->
            listener.onInsetsChange(root, lastWindowInsets.left, lastWindowInsets.top,
                    lastWindowInsets.right, lastWindowInsets.bottom)
        }
    }

}