package com.lollipop.iconkit.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import com.lollipop.iconcore.listener.WindowInsetsHelper
import com.lollipop.iconcore.ui.IconHelper
import com.lollipop.iconcore.util.IconGroup
import com.lollipop.iconkit.R
import com.lollipop.iconkit.util.log
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

    private var iconHelper = IconHelper.newHelper {
        IconHelper.DefaultXmlMap.readFromResource(it, R.xml.appfilter)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        iconHelper.loadAppInfo(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        log("iconInfo { allAppCount:${iconHelper.allAppCount} , supportedCount:${iconHelper.supportedCount} }")

        IconGroup(pageRoot).autoFit(iconHelper.supportedCount) { icon, index ->
            val iconPack = iconHelper.getAppInfo(index).iconPack
            if (iconPack.isEmpty()) {
                icon.loadIcon(0)
            } else {
                icon.loadIcon(iconPack[0])
            }
        }
    }

    override fun onInsetsChange(root: View, left: Int, top: Int, right: Int, bottom: Int) {
        super.onInsetsChange(root, left, top, right, bottom)
        log("onInsetsChange($root, $left, $top, $right, $bottom)")
        WindowInsetsHelper.setMargin(previewIcon1, left, top, 0, bottom)
        WindowInsetsHelper.setMargin(previewIcon4, 0, 0, right, 0)
    }

}