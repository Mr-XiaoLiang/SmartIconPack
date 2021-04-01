package com.lollipop.iconcore.ui.fragment

import androidx.fragment.app.Fragment

/**
 * @author lollipop
 * @date 4/1/21 23:01
 * Fragment的构造接口
 */
interface FragmentCreator {

    /**
     * fragment的数量
     */
    val fragmentCount: Int

    /**
     * 返回序号指定的Fragment
     * @param position Fragment的序号
     */
    fun getNewFragment(position: Int): Fragment

    /**
     * 获取Fragment对应的标题
     * @param position Fragment的序号
     */
    fun getTitle(position: Int): CharSequence
}