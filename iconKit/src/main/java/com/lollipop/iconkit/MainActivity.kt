package com.lollipop.iconkit

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.lollipop.iconcore.listener.WindowInsetsHelper
import com.lollipop.iconcore.ui.IconPackActivity
import com.lollipop.iconcore.ui.SimpleActivityRenderer
import com.lollipop.iconcore.util.CrashHandler
import com.lollipop.iconcore.util.log
import com.lollipop.iconkit.databinding.KitActivityMainBinding
import com.lollipop.iconkit.dialog.CrashDialog
import com.lollipop.iconkit.fragment.*
import liang.lollipop.ltabview.LTabHelper
import liang.lollipop.ltabview.LTabView

/**
 * @author lollipop
 * @date 10/22/20 20:34
 * 主页
 */
open class MainActivity: SimpleActivityRenderer() {

    private val fragmentList = ArrayList<BaseTabFragment>()
    private var tabGroupInsetsHelper: WindowInsetsHelper? = null

    private var crashDialogShown = false

    override fun onCreate(target: IconPackActivity, savedInstanceState: Bundle?) {
        super.onCreate(target, savedInstanceState)
        setContentView(target, KitActivityMainBinding.inflate(target.layoutInflater).root)
        target.initWindowFlag()
        initView(target)
    }

    protected open fun customizeFragment(): Array<BaseTabFragment> {
        return arrayOf()
    }

    private fun initView(target: IconPackActivity) {
        val pageGroup: ViewPager = find(R.id.pageGroup)?:return
        val tabView: LTabView = find(R.id.tabView)?:return

        val customizeFragment = customizeFragment()
        if (customizeFragment.isEmpty()) {
            fragmentList.add(HomeFragment())
            fragmentList.add(IconFragment())
            fragmentList.add(RequestFragment())
            fragmentList.add(AboutFragment())
        } else {
            fragmentList.addAll(customizeFragment)
        }
        pageGroup.adapter = FragmentAdapter(target.supportFragmentManager, fragmentList)
        // 全部保留
        pageGroup.offscreenPageLimit = fragmentList.size
        val build = LTabHelper.withExpandItem(tabView)
        build.layoutStyle = LTabView.Style.Fit
        val tabUnselectedColor = ContextCompat.getColor(target, R.color.tabUnselectedColor)
        for (fragment in fragmentList) {
            val selectedColor = ContextCompat.getColor(
                target, fragment.tabColorId)
            val title = target.getString(fragment.tabTitle)
            val icon = ContextCompat.getDrawable(target, fragment.tabIcon)
            if (icon != null) {
                build.addItem {
                    this.text = title
                    this.icon = icon
                    this.selectedIconColor = selectedColor
                    this.unselectedIconColor = tabUnselectedColor
                    this.textColor = selectedColor
                    this.expandColor = selectedColor.and(0x40FFFFFF)
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

    private class FragmentAdapter(fragmentManager: FragmentManager,
        private val fragments: ArrayList<BaseTabFragment>):
        FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int {
            return fragments.size
        }

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }


    }

}