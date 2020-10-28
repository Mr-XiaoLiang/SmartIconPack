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
 * 它会自动检索当前容器中的所有IconView
 * 然后重新排序，可以方便的记录、使用、修改
 */
class IconGroup(private val viewGroup: ViewGroup) {

    private val iconIndexList = ArrayList<Int>()

    private val iconShownMap = SparseArray<Int>()

    private val random: Random by lazy {
        Random()
    }

    private var allIconCount = 0

    /**
     * 当前已有的iconView数量
     */
    val iconCount: Int
        get() {
            return iconIndexList.size
        }

    init {
        notifyIconViewChange()
    }

    /**
     * 通过重新排序后的下标获取iconView
     */
    fun getChildAt(index: Int): IconView {
        if (index < 0 || index >= iconCount) {
            throw IconPackException("IndexOutOfBounds")
        }
        return viewGroup.getChildAt(iconIndexList[index]) as IconView
    }

    /**
     * 当容器中的View或者布局发生变化时，使用此方法检索并整理新的iconView
     */
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

    /**
     * 自动填充
     * 它会根据传入的总icon数量自动调用填充的回调函数来
     * 填充每个iconView，如果icon不足，那么将会把iconView设置为空
     * 并且会对icon进行去重复，保证设置的icon没有重复的
     * 如果icon数量大于来View数量，它会在其中随机摘取
     */
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

    /**
     * 修改某个iconView的icon
     * 它仍然符合autoFit的全部条件，但是仅仅针对当前icon做修改
     */
    fun changeIcon(view: IconView, fit: (icon: IconView, index: Int) -> Unit) {
        if (allIconCount == 0) {
            return
        }
        val randomIndex = randomIndex(allIconCount, iconShownMap, random)
        fit(view, randomIndex)
        iconShownMap[view.iconIndex] = 0
        view.iconIndex = randomIndex
    }

    /**
     * 遍历当前的所有iconView
     */
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

    /**
     * 为所有的iconView统一设置背景
     * 但是需要注意的是，每个iconView的背景是独立的实例
     * @param mutate 当设置为true时，
     * 表示每个view的背景是独立的，默认为true
     */
    fun setAllBackground(drawable: Drawable, mutate: Boolean = true) {
        for (index in 0 until iconCount) {
            val child = getChildAt(index)
            if (child is View) {
                child.background = if (mutate) {drawable.mutate()} else {drawable}
            }
        }
    }

    /**
     * 为每个View设置背景颜色
     */
    fun setAllBackground(color: Int) {
        for (index in 0 until iconCount) {
            val child = getChildAt(index)
            if (child is View) {
                child.setBackgroundColor(color)
            }
        }
    }

}