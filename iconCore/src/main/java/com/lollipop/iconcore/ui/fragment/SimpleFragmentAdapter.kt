package com.lollipop.iconcore.ui.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

/**
 * @author lollipop
 * @date 4/1/21 23:04
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
