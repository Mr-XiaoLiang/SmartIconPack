package com.lollipop.iconcore.listener

import android.view.View

/**
 * @author lollipop
 * @date 2020/5/13 23:03
 * 窗口缩进变化的监听器
 */
interface OnWindowInsetsListener {

    /**
     * 监听窗口锁进事件发生时
     * 它的来源及数据取决于监听的对象以及监听的方式
     * @param root 一般情况下，root指的当前屏幕可以获取的根结点View，
     * 或者等价于根结点显示范围的View，需要注意的是，
     * 需要确定它是否与自己当前的页面展示的View位于同一棵View树的同一分支，
     * 换言之，需要确定它是否是当前页面展示的View的上级
     * @param left
     * @param top
     * @param right
     * @param bottom
     * 需要注意的是，以上四个参数，不表示坐标位置，仅仅表示四边的屏幕UI缩进，
     * 缩进的源于可能来自于：系统UI（状态栏，虚拟按键等），异形屏（水滴，刘海等），
     * 这些缩进表示在原本的屏幕尺寸上，可能会占用一定的区域，可能会影响界面的显示。
     * 可以根据具体的场景来决定是否需要响应这些缩进
     */
    fun onInsetsChange(root: View, left: Int, top: Int, right: Int, bottom: Int)

}