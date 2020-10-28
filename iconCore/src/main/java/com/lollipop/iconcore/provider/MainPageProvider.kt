package com.lollipop.iconcore.provider

/**
 * @author lollipop
 * @date 10/22/20 01:38
 * 主页的提供类
 */
interface MainPageProvider {

    /**
     * 创建一个主页展示实现类
     */
    fun createRenderer(): MainPageRenderer

}