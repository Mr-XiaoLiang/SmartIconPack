package com.lollipop.iconkit.fragment

import com.lollipop.iconkit.R

/**
 * @author lollipop
 * @date 10/23/20 19:24
 */
class AboutFragment: BaseTabFragment() {
    override val tabIcon: Int
        get() = R.drawable.ic_baseline_person_24
    override val tabTitle: Int
        get() = R.string.about
    override val tabColorId: Int
        get() = R.color.tabAboutSelectedColor
    override val layoutId: Int
        get() = R.layout.kit_fragment_about

}