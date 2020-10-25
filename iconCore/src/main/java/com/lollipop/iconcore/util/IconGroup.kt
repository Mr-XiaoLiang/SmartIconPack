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

    private val iconShownMap = SparseArray<Int>()

    private val random: Random by lazy {
        Random()
    }

    private var allIconCount = 0

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
        allIconCount = allCount
        iconShownMap.clear()
        if (iconCount < allCount) {
            for (index in 0 until (min(iconCount, allCount))) {
                val randomIndex = randomIndex(allCount, iconShownMap, random)
                val child = getChildAt(index)
                child.iconIndex = randomIndex
                fit(child, randomIndex)
            }
        } else {
            for (index in 0 until iconCount) {
                val child = getChildAt(index)
                if (index < allCount) {
                    fit(child, index)
                    child.iconIndex = index
                    iconShownMap[index] = 1
                } else {
                    child.loadIcon(0)
                    child.iconIndex = -1
                }
            }
        }
    }

    fun changeIcon(view: IconView, fit: (icon: IconView, index: Int) -> Unit) {
        if (allIconCount == 0) {
            return
        }
        val randomIndex = randomIndex(allIconCount, iconShownMap, random)
        fit(view, randomIndex)
        iconShownMap[view.iconIndex] = 0
        view.iconIndex = randomIndex
    }

    fun forEach(run: (icon: IconView) -> Unit) {
        for (index in iconIndexList.indices) {
            run(getChildAt(index))
        }
    }

    private fun randomIndex(max: Int, map: SparseArray<Int>, random: Random): Int {
        while (max > iconCount) {
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