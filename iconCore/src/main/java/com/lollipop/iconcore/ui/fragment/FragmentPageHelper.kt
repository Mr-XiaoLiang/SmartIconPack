package com.lollipop.iconcore.ui.fragment

import android.content.res.Resources
import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.*
import androidx.fragment.app.*
import androidx.viewpager.widget.ViewPager

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

        private val fragmentList = ArrayList<TabFragmentInfo>()

        private var fragmentInitCallback: ((Bundle, Int) -> Unit)? = null

        private var titleProvider: TitleProvider? = null

        /**
         * 添加Fragment的类
         * 由于考虑到Fragment的销毁和恢复，因此使用类的形式传入
         * 它需要实现类提供无参数的构造器
         */
        fun addFragment(vararg fragment: TabFragmentInfo): Builder {
            fragmentList.addAll(fragment)
            return this
        }

        /**
         * 添加一个Fragment的描述信息
         * @param fragment Fragment的类信息
         * @param tabIcon tab的对应icon信息
         * @param tabTitle tab的标题信息
         * @param tabColor tab的颜色信息
         */
        fun addFragment(
            fragment: Class<out Fragment>,
            @DrawableRes
            tabIcon: Int = 0,
            @StringRes
            tabTitle: Int = 0,
            @ColorRes
            tabColor: Int = 0,
        ): Builder {
            return addFragment(TabFragmentInfo(fragment, tabIcon, tabTitle, tabColor))
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
         * @return fragment信息的集合
         */
        fun bindTo(viewPager: ViewPager, enableState: Boolean = true): Array<TabFragmentInfo> {
            val fragmentCreator = buildCreator(viewPager.resources)
            val adapter = if (enableState) {
                SimpleFragmentStateAdapter(fragmentManager, fragmentCreator)
            } else {
                SimpleFragmentAdapter(fragmentManager, fragmentCreator)
            }
            viewPager.adapter = adapter
            return fragmentList.toTypedArray()
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
                buildCreator(viewGroup.resources),
                fragmentList.toTypedArray()
            )
        }

        private fun buildCreator(resources: Resources): FragmentCreator {
            val title = titleProvider ?: SimpleTitleProvider(
                resources,
                IntArray(fragmentList.size) { fragmentList[it].tabTitle })
            return SimpleFragmentCreator(
                Array(fragmentList.size) { fragmentList[it].fragment },
                title,
                fragmentInitCallback ?: { _, _ -> }
            )
        }

    }

}