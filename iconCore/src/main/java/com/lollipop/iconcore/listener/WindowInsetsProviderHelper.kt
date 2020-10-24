package com.lollipop.iconcore.listener

import android.graphics.Rect
import android.view.View
import java.lang.ref.SoftReference

/**
 * @author lollipop
 * @date 2020/5/13 23:46
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

    fun destroy() {
        lastWindowInsets.set(0, 0, 0, 0)
        rootView = null
        windowInsetsListenerList.clear()
    }

    fun call(listener: OnWindowInsetsListener) {
        rootView?.get()?.let { root ->
            listener.onInsetsChange(root, lastWindowInsets.left, lastWindowInsets.top,
                    lastWindowInsets.right, lastWindowInsets.bottom)
        }
    }

}