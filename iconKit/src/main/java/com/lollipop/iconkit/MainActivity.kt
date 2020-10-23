package com.lollipop.iconkit

import android.os.Bundle
import com.lollipop.iconcore.ui.IconPackActivity
import com.lollipop.iconcore.ui.SimpleActivityRenderer
import com.lollipop.iconkit.fragment.BaseTabFragment

/**
 * @author lollipop
 * @date 10/22/20 20:34
 * 主页
 */
open class MainActivity: SimpleActivityRenderer() {

    private val fragmentList = ArrayList<BaseTabFragment>()

    override fun onCreate(target: IconPackActivity, savedInstanceState: Bundle?) {
        super.onCreate(target, savedInstanceState)
        setContentView(target, R.layout.activity_main)
        initView()
    }

    protected open fun customizeFragment(): Array<BaseTabFragment> {
        return arrayOf()
    }

    private fun initView() {
        val customizeFragment = customizeFragment()
        if (customizeFragment.isEmpty()) {
//            fragmentList.add()
        } else {
            fragmentList.addAll(customizeFragment)
        }
    }

}