package com.lollipop.iconkit.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lollipop.iconcore.ui.IconHelper
import com.lollipop.iconcore.ui.IconImageView
import com.lollipop.iconkit.R
import kotlinx.android.synthetic.main.kit_fragment_home.*

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

    private var iconHelper: IconHelper? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        iconHelper = IconHelper(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onInsetsChange(root: View, left: Int, top: Int, right: Int, bottom: Int) {
        super.onInsetsChange(root, left, top, right, bottom)
        previewIcon1?.changeViewMargin(left, top, 0, bottom)
        previewIcon4?.changeViewMargin(0, 0, right, 0)
    }

    private fun View.changeViewMargin(left: Int, top: Int, right: Int, bottom: Int) {
        val lp = layoutParams?:return
        if (lp is ViewGroup.MarginLayoutParams) {
            lp.setMargins(left, top, right, bottom)
            layoutParams = lp
        }
    }


}