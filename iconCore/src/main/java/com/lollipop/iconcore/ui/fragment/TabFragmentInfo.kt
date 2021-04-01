package com.lollipop.iconcore.ui.fragment

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

/**
 * @author lollipop
 * @date 4/1/21 22:57
 * 与Tab配合的Fragment描述信息
 */
data class TabFragmentInfo(
    /**
     * fragment的类描述信息
     */
    val fragment: Class<out Fragment>,
    /**
     * tab对应的icon信息
     */
    @DrawableRes
    val tabIcon: Int = 0,
    /**
     * tab对应的标题信息
     */
    @StringRes
    val tabTitle: Int = 0,
    /**
     * tab对应的icon信息
     */
    @ColorRes
    val tabColor: Int = 0,
)