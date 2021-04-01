package com.lollipop.iconcore.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment

/**
 * @author lollipop
 * @date 4/1/21 23:03
 * 简化后的Fragment管理工厂
 * 它主要用于实例化Fragment
 */
class SimpleFragmentCreator(
    /**
     * fragment的类数组
     * 它要求fragment提供无参数的构造器
     */
    private val fragmentClassArray: Array<Class<out Fragment>>,
    /**
     * 标题信息的提供者，如果不需要，那么可以返回空字符串
     */
    private val titleProvider: TitleProvider,
    /**
     * fragment初始化时的回调函数，此时可以设置一些必要的参数
     */
    private val fragmentInitCallback: (Bundle, Int) -> Unit
) : FragmentCreator {

    override val fragmentCount = fragmentClassArray.size

    override fun getNewFragment(position: Int): Fragment {
        val newInstance = fragmentClassArray[position].newInstance()
        val arguments = newInstance.arguments ?: Bundle()
        fragmentInitCallback(arguments, position)
        newInstance.arguments = arguments
        return newInstance
    }

    override fun getTitle(position: Int): CharSequence {
        return titleProvider.getTitle(position)
    }

}