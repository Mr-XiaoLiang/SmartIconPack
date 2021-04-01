package com.lollipop.iconcore.util

import android.content.res.Resources
import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.annotation.StringRes
import androidx.fragment.app.*
import androidx.viewpager.widget.ViewPager
import java.lang.ref.WeakReference

/**
 * @author lollipop
 * @date 3/31/21 23:37
 * Fragment的包装工具
 */
object FragmentPageHelper {

    /**
     * 从一个Activity中创建一个Fragment管理器的引导工具
     *
     * 提示：
     * 如果你是在一个Fragment中使用，那么建议穿入Fragment，
     * 而不是使用getActivity，因为你需要的可能是在Fragment中管理另一组Fragment。
     * 这时候如果使用了Activity的FragmentManager，那么会导致Fragment的生命周期错乱
     * 那么你需要使用的是{@link with(Fragment)}
     *
     */
    fun with(activity: FragmentActivity): Builder {
        return Builder(activity.supportFragmentManager)
    }

    /**
     * 从一个Fragment中创建一个Fragment管理器的引导工具
     *
     * 提示：
     * 如果你是需要一个在Fragment中管理另一组Fragment的方法，那么你应该使用当前的方法
     * 如果你需要的是Activity来管理Fragment，那么你需要使用的是{@link with(FragmentActivity)}
     */
    fun with(fragment: Fragment): Builder {
        return Builder(fragment.childFragmentManager)
    }

    /**
     * 一个简化Fragment管理器构造过程的辅助工具
     */
    class Builder(private val fragmentManager: FragmentManager) {

        private val fragmentList = ArrayList<Class<out Fragment>>()

        private val titleList = ArrayList<Int>()

        private var fragmentInitCallback: ((Bundle, Int) -> Unit)? = null

        private var titleProvider: TitleProvider? = null

        /**
         * 添加Fragment的类
         * 由于考虑到Fragment的销毁和恢复，因此使用类的形式传入
         * 它需要实现类提供无参数的构造器
         */
        fun addFragment(vararg fragment: Class<out Fragment>): Builder {
            fragmentList.addAll(fragment)
            return this
        }

        /**
         * 设置标题的资源
         * 它的顺序与Fragment顺序一致
         */
        fun addTitle(@StringRes vararg strId: Int): Builder {
            strId.forEach {
                titleList.add(it)
            }
            return this
        }

        /**
         * 设置一个自定义的title提供者
         * @param provider title的提供者
         */
        fun setTitles(provider: TitleProvider): Builder {
            titleProvider = provider
            return this
        }

        /**
         * 监听Fragment的创建，可以在必要的时候传入一些参数
         */
        fun onFragmentCreated(callback: (bundle: Bundle, position: Int) -> Unit): Builder {
            fragmentInitCallback = callback
            return this
        }

        /**
         * 绑定内容到一个ViewPager中
         * 它会生成一个Adapter，它会替你管理Fragment
         * @param viewPager 需要管理的ViewPager
         * @param enableState 如果为true，那么将会使用SimpleFragmentStateAdapter，
         * 否则将会使用SimpleFragmentAdapter。
         */
        fun bindTo(viewPager: ViewPager, enableState: Boolean = true) {
            val fragmentCreator = buildCreator(viewPager.resources)
            val adapter = if (enableState) {
                SimpleFragmentStateAdapter(fragmentManager, fragmentCreator)
            } else {
                SimpleFragmentAdapter(fragmentManager, fragmentCreator)
            }
            viewPager.adapter = adapter
        }

        /**
         * 为指定的View容器创建一个Fragment切换工具
         * @param viewGroup 用于放置Fragment的容器
         * @return 可以控制Fragment的切换器
         */
        fun bindTo(viewGroup: ViewGroup): FragmentSwitcher {
            return FragmentSwitcher(
                viewGroup.id,
                fragmentManager,
                buildCreator(viewGroup.resources)
            )
        }

        private fun buildCreator(resources: Resources): FragmentCreator {
            val title = titleProvider ?: SimpleTitleProvider(
                resources,
                IntArray(titleList.size) { titleList[it] })
            return SimpleFragmentCreator(
                fragmentList.toTypedArray(),
                title,
                fragmentInitCallback ?: { _, _ -> }
            )
        }

    }

    /**
     * 一个Fragment Adapter的简化包装
     * 它没有状态管理，但是也不会主动销毁fragment
     * 如果只需要管理少量的fragment，那么可以使用它来避免fragment反复创建
     */
    class SimpleFragmentAdapter(
        fragmentManager: FragmentManager,
        private val fragmentCreator: FragmentCreator
    ) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getCount(): Int {
            return fragmentCreator.fragmentCount
        }

        override fun getItem(position: Int): Fragment {
            return fragmentCreator.getNewFragment(position)
        }

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentCreator.getTitle(position)
        }

    }

    /**
     * 一个Fragment Adapter的简化包装
     * 它有状态管理，他会主动的销毁fragment，
     * 如果需要管理非常多的fragment，那么可能需要使用它来节约资源
     */
    class SimpleFragmentStateAdapter(
        fragmentManager: FragmentManager,
        private val fragmentCreator: FragmentCreator
    ) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getCount(): Int {
            return fragmentCreator.fragmentCount
        }

        override fun getItem(position: Int): Fragment {
            return fragmentCreator.getNewFragment(position)
        }

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentCreator.getTitle(position)
        }

    }

    /**
     * 简化后的Fragment管理工厂
     * 它主要用于实例化Fragment
     */
    class SimpleFragmentCreator(
        /**
         * fragment的类数组
         * 它要求fragment提供无参数的构造器
         */
        private val fragmentClassArray: Array<Class<out Fragment>>,
        /**
         * 标题信息的提供者，如果不需要，那么可以返回空字符串
         */
        private val titleProvider: TitleProvider,
        /**
         * fragment初始化时的回调函数，此时可以设置一些必要的参数
         */
        private val fragmentInitCallback: (Bundle, Int) -> Unit
    ) : FragmentCreator {

        override val fragmentCount = fragmentClassArray.size

        override fun getNewFragment(position: Int): Fragment {
            val newInstance = fragmentClassArray[position].newInstance()
            val arguments = newInstance.arguments ?: Bundle()
            fragmentInitCallback(arguments, position)
            newInstance.arguments = arguments
            return newInstance
        }

        override fun getTitle(position: Int): CharSequence {
            return titleProvider.getTitle(position)
        }

    }

    /**
     * 一个简单的标题提供器
     */
    class SimpleTitleProvider(
        /**
         * 提供字符串的资源提供者
         */
        resources: Resources,
        /**
         * 标题的id数组
         */
        private val titleResArray: IntArray
    ) : TitleProvider {

        private val resourcesReference = WeakReference(resources)

        override fun getTitle(position: Int): CharSequence {
            if (position < 0 || position >= titleResArray.size) {
                return ""
            }
            tryDo {
                return resourcesReference.get()?.getString(titleResArray[position]) ?: ""
            }
            return ""
        }

    }

    /**
     * Fragment的title提供者
     */
    fun interface TitleProvider {

        /**
         * 按照序号返回相应的内容
         * @param position 标题对应的页面序号
         */
        fun getTitle(position: Int): CharSequence
    }

    /**
     * Fragment的构造接口
     */
    interface FragmentCreator {

        /**
         * fragment的数量
         */
        val fragmentCount: Int

        /**
         * 返回序号指定的Fragment
         * @param position Fragment的序号
         */
        fun getNewFragment(position: Int): Fragment

        /**
         * 获取Fragment对应的标题
         * @param position Fragment的序号
         */
        fun getTitle(position: Int): CharSequence
    }

    class FragmentSwitcher(
        private val containerId: Int,
        private val fragmentManager: FragmentManager,
        private val fragmentCreator: FragmentCreator
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

}