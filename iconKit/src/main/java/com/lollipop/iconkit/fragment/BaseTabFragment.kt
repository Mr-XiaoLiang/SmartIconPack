package com.lollipop.iconkit.fragment

import com.lollipop.iconcore.ui.BaseFragment

/**
 * @author lollipop
 * @date 10/23/20 19:18
 */
abstract class BaseTabFragment: BaseFragment() {

    abstract val tabIcon: Int

    abstract val tabTitle: Int

    abstract val tabColorId: Int


}