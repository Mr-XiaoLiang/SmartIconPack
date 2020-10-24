package com.lollipop.iconcore.util

import android.graphics.drawable.Drawable
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.core.util.set
import com.lollipop.iconcore.throwable.IconPackException
import com.lollipop.iconcore.ui.IconView
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min

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

    init {
        notifyIconViewChange()
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

    fun autoFit(allCount: Int, fit: (icon: IconView, index: Int) -> Unit) {
        if (iconCount < allCount) {
            val map = SparseArray<Int>()
            val random = Random()
            for (index in 0 until (min(iconCount, allCount))) {
                val randomIndex = randomIndex(allCount, map, random)
                fit(getChildAt(index), randomIndex)
            }
        } else {
            for (index in 0 until iconCount) {
                if (index < allCount) {
                    fit(getChildAt(index), index)
                } else {
                    getChildAt(index).loadIcon(0)
                }
            }
        }
    }

    private fun randomIndex(max: Int, map: SparseArray<Int>, random: Random): Int {
        while (max > map.size()) {
            val nextInt = random.nextInt(max)
            if (map.get(nextInt, 0) == 0) {
                map.put(nextInt, 1)
                return nextInt
            }
        }
        return -1
    }

    fun setAllBackground(drawable: Drawable) {
        for (index in 0 until iconCount) {
            val child = getChildAt(index)
            if (child is View) {
                child.background = drawable.mutate()
            }
        }
    }

    fun setAllBackground(color: Int) {
        for (index in 0 until iconCount) {
            val child = getChildAt(index)
            if (child is View) {
                child.setBackgroundColor(color)
            }
        }
    }

}