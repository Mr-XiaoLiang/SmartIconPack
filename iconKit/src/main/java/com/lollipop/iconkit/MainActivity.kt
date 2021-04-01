package com.lollipop.iconkit

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.lollipop.iconcore.listener.WindowInsetsHelper
import com.lollipop.iconcore.ui.IconPackActivity
import com.lollipop.iconcore.ui.SimpleActivityRenderer
import com.lollipop.iconcore.ui.fragment.FragmentPageHelper
import com.lollipop.iconcore.ui.fragment.TabFragmentInfo
import com.lollipop.iconcore.util.CrashHandler
import com.lollipop.iconcore.util.alpha
import com.lollipop.iconcore.util.log
import com.lollipop.iconkit.databinding.KitActivityMainBinding
import com.lollipop.iconkit.dialog.CrashDialog
import com.lollipop.iconkit.fragment.FragmentConfig
import liang.lollipop.ltabview.LTabHelper
import liang.lollipop.ltabview.LTabView

/**
 * @author lollipop
 * @date 10/22/20 20:34
 * 主页
 */
open class MainActivity: SimpleActivityRenderer() {

    private var tabGroupInsetsHelper: WindowInsetsHelper? = null

    private var crashDialogShown = false

    override fun onCreate(target: IconPackActivity, savedInstanceState: Bundle?) {
        super.onCreate(target, savedInstanceState)
        setContentView(target, KitActivityMainBinding.inflate(target.layoutInflater).root)
        target.initWindowFlag()
        initView(target)
    }

    protected open fun customizeFragment(): Array<TabFragmentInfo> {
        return arrayOf()
    }

    private fun initView(target: IconPackActivity) {
        val pageGroup: ViewPager = find(R.id.pageGroup)?:return
        val tabView: LTabView = find(R.id.tabView)?:return

        val pageHelper = FragmentPageHelper.with(target)

        val customizeFragment = customizeFragment()
        if (customizeFragment.isEmpty()) {
            pageHelper.addFragment(*FragmentConfig.defaultFragmentInfo)
        } else {
            pageHelper.addFragment(*customizeFragment)
        }
        if (BuildConfig.DEBUG) {
            pageHelper.onFragmentCreated { _, position ->
                log("onFragmentCreated: $position")
            }
        }
        val pageInfo = pageHelper.bindTo(pageGroup, false)

        // 开始构造TabLayout
        val build = LTabHelper.withExpandItem(tabView)
        build.layoutStyle = LTabView.Style.Fit
        val tabUnselectedColor = ContextCompat.getColor(target, R.color.tabUnselectedColor)
        for (info in pageInfo) {
            val icon = ContextCompat.getDrawable(target, info.tabIcon)
            if (icon != null) {
                val selectedColor = ContextCompat.getColor(target, info.tabColor)
                val title = target.getString(info.tabTitle)
                build.addItem {
                    this.text = title
                    this.icon = icon
                    this.selectedIconColor = selectedColor
                    this.unselectedIconColor = tabUnselectedColor
                    this.textColor = selectedColor
                    this.expandColor = selectedColor.alpha(0x40)
                }
            }
        }
        build.setupWithViewPager(pageGroup)

        val tabGroup: View = find(R.id.tabGroup)?:return
        tabGroupInsetsHelper = WindowInsetsHelper(tabGroup)
    }

    override fun onInsetsChange(root: View, left: Int, top: Int, right: Int, bottom: Int) {
        tabGroupInsetsHelper?.updateByPadding(root, left, top, right, bottom)
        log("onInsetsChange: ", root, left, top, right, bottom)
    }

    override fun onResume(target: IconPackActivity) {
        super.onResume(target)
        if (!crashDialogShown) {
            if (CrashHandler.hasCrashFlag(target)) {
                CrashHandler.getCrashLog(target) {
                    CrashDialog(it).show(target)
                    crashDialogShown = true
                }
            }
        }
    }

}