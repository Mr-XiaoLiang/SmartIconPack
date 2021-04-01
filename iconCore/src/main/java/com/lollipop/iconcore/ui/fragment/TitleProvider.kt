package com.lollipop.iconcore.ui.fragment

/**
 * @author lollipop
 * @date 4/1/21 23:02
 * Fragment的title提供者
 */
fun interface TitleProvider {

    /**
     * 按照序号返回相应的内容
     * @param position 标题对应的页面序号
     */
    fun getTitle(position: Int): CharSequence
}