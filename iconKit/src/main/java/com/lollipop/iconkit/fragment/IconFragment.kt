package com.lollipop.iconkit.fragment

import com.lollipop.iconkit.R

/**
 * @author lollipop
 * @date 10/23/20 19:24
 */
class IconFragment: BaseTabFragment() {
    override val tabIcon: Int
        get() = R.drawable.ic_baseline_apps_24
    override val tabTitle: Int
        get() = R.string.icon
    override val tabColorId: Int
        get() = R.color.tabIconSelectedColor
    override val layoutId: Int
        get() = R.layout.kit_bottom_dialog


}