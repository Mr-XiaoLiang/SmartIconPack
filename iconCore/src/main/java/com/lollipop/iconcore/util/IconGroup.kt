package com.lollipop.iconcore.util

import android.view.ViewGroup
import com.lollipop.iconcore.throwable.IconPackException
import com.lollipop.iconcore.ui.IconView

/**
 * @author lollipop
 * @date 10/24/20 20:38
 * icon的Group辅助类
 */
class IconGroup(private val viewGroup: ViewGroup) {

    private val iconIndexList = ArrayList<Int>()

    val iconCount: Int
        get() {
            return iconIndexList.size
        }

    fun getChildAt(index: Int): IconView {
        if (index < 0 || index >= iconCount) {
            throw IconPackException("IndexOutOfBounds")
        }
        return viewGroup.getChildAt(iconIndexList[index]) as IconView
    }

    fun notifyIconViewChange() {
        iconIndexList.clear()
        val childCount = viewGroup.childCount
        for (index in 0 until childCount) {
            val child = viewGroup.getChildAt(index)
            if (child is IconView) {
                iconIndexList.add(index)
            }
        }
    }

}