package com.lollipop.iconkit.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lollipop.iconkit.R

/**
 * @author lollipop
 * @date 10/23/20 19:24
 */
class HomeFragment: BaseTabFragment() {
    override val tabIcon: Int
        get() = R.drawable.ic_baseline_home_24
    override val tabTitle: Int
        get() = R.string.home
    override val tabColorId: Int
        get() = R.color.tabHomeSelectedColor
    override val layoutId: Int
        get() = R.layout.kit_fragment_home

}