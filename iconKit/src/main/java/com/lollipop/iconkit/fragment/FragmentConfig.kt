package com.lollipop.iconkit.fragment

import com.lollipop.iconcore.ui.fragment.TabFragmentInfo
import com.lollipop.iconkit.R

/**
 * @author lollipop
 * @date 4/1/21 22:53
 */
object FragmentConfig {

    /**
     * 默认的Fragment配置信息
     */
    val defaultFragmentInfo = arrayOf(
        TabFragmentInfo(
            HomeFragment::class.java,
            R.drawable.ic_baseline_home_24,
            R.string.home,
            R.color.tabHomeSelectedColor,
        ),
        TabFragmentInfo(
            IconFragment::class.java,
            R.drawable.ic_baseline_apps_24,
            R.string.icon,
            R.color.tabIconSelectedColor,
        ),
        TabFragmentInfo(
            RequestFragment::class.java,
            R.drawable.ic_baseline_architecture_24,
            R.string.request,
            R.color.tabRequestSelectedColor
        ),
        TabFragmentInfo(
            AboutFragment::class.java,
            R.drawable.ic_baseline_person_24,
            R.string.about,
            R.color.tabAboutSelectedColor,
        ),
    )

}