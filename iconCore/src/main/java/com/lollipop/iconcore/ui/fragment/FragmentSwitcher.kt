package com.lollipop.iconcore.ui.fragment

import androidx.annotation.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

/**
 * @author lollipop
 * @date 4/1/21 23:00
 * Fragment切换的辅助工具
 * 包含了一定的通用逻辑
 */
class FragmentSwitcher(
    private val containerId: Int,
    private val fragmentManager: FragmentManager,
    private val fragmentCreator: FragmentCreator,
    private val fragmentInfoArray: Array<TabFragmentInfo>
) {

    companion object {
        const val NO_FRAGMENT = -1
    }

    /**
     * 页面的数量
     */
    val pageCount = fragmentCreator.fragmentCount

    /**
     * position的合理范围
     */
    private val positionRange = 0 until pageCount

    /**
     * 事务返回栈的开关
     * 如果激活了返回栈，那么可以通过back方法返回到指定的事务状态
     */
    var backStackEnable = false

    /**
     * 上一个Fragment的序号
     */
    private var lastFragment = NO_FRAGMENT

    private var defaultEnterAnimation = 0
    private var defaultExitAnimation = 0
    private var defaultPopEnterAnimation = 0
    private var defaultPopExitAnimation = 0

    /**
     * 设置默认的Fragment切换动画
     * @param enter Fragment进入的动画
     * @param exit Fragment离开的动画
     * @param popEnter Fragment因为弹出栈而进入的动画
     * @param popExit Fragment因为弹出栈而离开的动画
     */
    fun setDefaultAnimations(
        @AnimatorRes @AnimRes enter: Int = defaultEnterAnimation,
        @AnimatorRes @AnimRes exit: Int = defaultExitAnimation,
        @AnimatorRes @AnimRes popEnter: Int = defaultPopEnterAnimation,
        @AnimatorRes @AnimRes popExit: Int = defaultPopExitAnimation
    ) {
        defaultEnterAnimation = enter
        defaultExitAnimation = exit
        defaultPopEnterAnimation = popEnter
        defaultPopExitAnimation = popExit
    }

    /**
     * 获取序号对应的tab的icon的资源id
     */
    @DrawableRes
    fun getTabIcon(position: Int): Int {
        if (position in positionRange) {
            return fragmentInfoArray[position].tabIcon
        }
        return 0
    }

    /**
     * 获取序号对应的tab的color的资源id
     */
    @ColorRes
    fun getTabColor(position: Int): Int {
        if (position in positionRange) {
            return fragmentInfoArray[position].tabColor
        }
        return 0
    }

    /**
     * 获取序号对应的tab的title的资源id
     */
    @StringRes
    fun getTabTitle(position: Int): Int {
        if (position in positionRange) {
            return fragmentInfoArray[position].tabTitle
        }
        return 0
    }

    /**
     * 切换为指定序号所代表的Fragment
     * @param position 需要的Fragment所对应的序号
     * @param hideLast 显示新的Fragment时，是否需要隐藏上一个Fragment
     * 如果需要显示的Fragment在上一个Fragment的下方，
     * 在不隐藏上一个Fragment的情况下，那么将会导致新的Fragment不可见
     * @param enter Fragment进入的动画
     * @param exit Fragment离开的动画
     * @param popEnter Fragment因为弹出栈而进入的动画
     * @param popExit Fragment因为弹出栈而离开的动画
     */
    fun switchTo(
        position: Int,
        hideLast: Boolean = !backStackEnable,
        @AnimatorRes @AnimRes enter: Int = defaultEnterAnimation,
        @AnimatorRes @AnimRes exit: Int = defaultExitAnimation,
        @AnimatorRes @AnimRes popEnter: Int = defaultPopEnterAnimation,
        @AnimatorRes @AnimRes popExit: Int = defaultPopExitAnimation
    ): Int {
        // 启动事务
        val transaction = fragmentManager.beginTransaction()
        // 如果需要隐藏上一个fragment 并且 上一个fragment不为空
        if (hideLast && lastFragment != NO_FRAGMENT) {
            findFragment(lastFragment)?.let {
                transaction.hide(it)
            }
        }

        // position需要存在合理的范围中
        if (position in positionRange) {
            // 查看管理器中是否已经存在了fragment
            val target = findFragment(position)
            // 如果存在了，那么就直接显示
            if (target != null) {
                transaction.show(target)
            } else {
                // 如果不存在就新添加
                transaction.add(createFragment(position), getTag(position))
            }
        }

        // 设置Fragment的动画
        transaction.setCustomAnimations(enter, exit, popEnter, popExit)

        // 如果需要返回栈，那么添加返回栈
        if (backStackEnable) {
            transaction.addToBackStack("from:$lastFragment,to:$position")
        }
        // 记录序号
        lastFragment = position
        return transaction.commit()
    }

    /**
     * 回退到指定位置的事务状态
     * 如果backStackEnable为false时，方法不会生效
     * @param commitId 指定提交id，如果在默认的情况下，或者不设置的情况下，
     * 表示不指定提交的位置，直接回退至上一个状态
     * @param inclusive 回退时是否包含commitId对应的那一笔提交
     * 如果为true，那么表示回退到commitId之前的状态
     * 如果为false，表示回退到commitId对应的状态
     * 如果commitId不做设置，那么此参数不生效
     */
    fun back(commitId: Int = -1, inclusive: Boolean = false) {
        if (!backStackEnable) {
            return
        }
        if (commitId != -1) {
            val flag = if (inclusive) {
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            } else {
                0
            }
            fragmentManager.popBackStack(commitId, flag)
        } else {
            fragmentManager.popBackStack()
        }
    }

    private fun findFragment(position: Int): Fragment? {
        return fragmentManager.findFragmentByTag(getTag(position))
    }

    private fun createFragment(position: Int): Fragment {
        return fragmentCreator.getNewFragment(position)
    }

    private fun getTag(position: Int): String {
        return "Lollipop:switcher:${containerId}:$position"
    }

}