package com.lollipop.iconkit

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.lollipop.iconcore.ui.IconPackActivity
import com.lollipop.iconcore.ui.SimpleActivityRenderer
import com.lollipop.iconkit.fragment.AboutFragment
import com.lollipop.iconkit.fragment.BaseTabFragment
import com.lollipop.iconkit.fragment.HomeFragment
import com.lollipop.iconkit.fragment.IconFragment
import liang.lollipop.ltabview.LTabHelper
import liang.lollipop.ltabview.LTabView

/**
 * @author lollipop
 * @date 10/22/20 20:34
 * 主页
 */
open class MainActivity: SimpleActivityRenderer() {

    private val fragmentList = ArrayList<BaseTabFragment>()

    override fun onCreate(target: IconPackActivity, savedInstanceState: Bundle?) {
        super.onCreate(target, savedInstanceState)
        setContentView(target, R.layout.kit_activity_main)
        initView(target)
    }

    protected open fun customizeFragment(): Array<BaseTabFragment> {
        return arrayOf()
    }

    private fun initView(target: IconPackActivity) {
        val pageGroup: ViewPager2 = find(R.id.pageGroup)?:return
        val tabView: LTabView = find(R.id.tabView)?:return
        val customizeFragment = customizeFragment()
        if (customizeFragment.isEmpty()) {
            fragmentList.add(HomeFragment())
            fragmentList.add(IconFragment())
            fragmentList.add(AboutFragment())
        } else {
            fragmentList.addAll(customizeFragment)
        }
        pageGroup.adapter = FragmentAdapter(target, fragmentList)
        pageGroup.orientation = ViewPager2.ORIENTATION_HORIZONTAL
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
        pageGroup.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                build.selected(position)
            }
        })
        build.onSelected {
            pageGroup.currentItem = it
        }
    }

    private class FragmentAdapter(activity: IconPackActivity,
        private val fragments: ArrayList<BaseTabFragment>): FragmentStateAdapter(activity) {

        override fun getItemCount(): Int {
            return fragments.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }

    }

}