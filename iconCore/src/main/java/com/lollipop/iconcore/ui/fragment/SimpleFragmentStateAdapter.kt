package com.lollipop.iconcore.ui.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

/**
 * @author lollipop
 * @date 4/1/21 23:03
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
