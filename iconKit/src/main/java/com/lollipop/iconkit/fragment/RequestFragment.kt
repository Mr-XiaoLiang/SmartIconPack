package com.lollipop.iconkit.fragment

import com.lollipop.iconkit.R

class RequestFragment : BaseTabFragment() {
    override val tabIcon: Int
        get() = R.drawable.ic_baseline_architecture_24
    override val tabTitle: Int
        get() = R.string.request
    override val tabColorId: Int
        get() = R.color.tabRequestSelectedColor
    override val layoutId: Int
        get() = R.layout.kit_fragment_request


}