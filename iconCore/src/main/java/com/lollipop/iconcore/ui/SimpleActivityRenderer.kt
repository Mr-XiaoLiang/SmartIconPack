package com.lollipop.iconcore.ui

import android.app.TaskStackBuilder
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.lollipop.iconcore.provider.MainPageRenderer

/**
 * @author lollipop
 * @date 10/23/20 19:01
 * 简易的Activity呈现类实现，它提供了基础的方法实现
 * 可以更加简单的实现Activity的布局
 */
open class SimpleActivityRenderer: MainPageRenderer {

    private var activityView: View? = null

    /**
     * 为activty设置内容体的View
     */
    protected fun setContentView(activity: IconPackActivity, view: View) {
        activityView = view
        activity.setContentView(activityView)
        activity.initRootGroup(view)
    }

    /**
     * 以ID的形式为activity设置内容体的view
     */
    protected fun setContentView(activity: IconPackActivity, resId: Int) {
        setContentView(activity,
            LayoutInflater.from(activity).inflate(resId, null))
    }

    /**
     * 为了弥补不在activity中，不能便捷寻找View的缺憾
     * 这里提供了方法，简化来这个过程
     */
    protected fun <T: View> find(id: Int): T? {
        return activityView?.findViewById(id)
    }

    override fun onCreate(target: IconPackActivity, savedInstanceState: Bundle?) {

    }

    override fun onStart(target: IconPackActivity) {

    }

    override fun onStop(target: IconPackActivity) {

    }

    override fun onResume(target: IconPackActivity) {

    }

    override fun onPause(target: IconPackActivity) {

    }

    override fun onRestart(target: IconPackActivity) {

    }

    override fun onDestroy(target: IconPackActivity) {

    }

    override fun onSaveInstanceState(target: IconPackActivity, outState: Bundle) {

    }

    override fun onRestoreInstanceState(target: IconPackActivity, savedInstanceState: Bundle) {

    }

    override fun onSupportNavigateUp(target: IconPackActivity): Boolean? {
        return null
    }

    override fun onActionModeFinished(target: IconPackActivity, mode: ActionMode?) {
        return
    }

    override fun onActionModeStarted(target: IconPackActivity, mode: ActionMode?) {
        return
    }

    override fun onActivityReenter(target: IconPackActivity, resultCode: Int, data: Intent?) {
        return
    }

    override fun onApplyThemeResource(
        target: IconPackActivity, theme: Resources.Theme?,
        resid: Int, first: Boolean) {
        return
    }

    override fun onAttachedToWindow(target: IconPackActivity) {
        return
    }

    override fun onContextMenuClosed(target: IconPackActivity, menu: Menu) {
        return
    }

    override fun onCreateContextMenu(
        target: IconPackActivity, menu: ContextMenu?,
        v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        return
    }

    override fun onCreateDescription(target: IconPackActivity): CharSequence? {
        return null
    }

    override fun onCreateOptionsMenu(target: IconPackActivity, menu: Menu?): Boolean? {
        return null
    }

    override fun onOptionsMenuClosed(target: IconPackActivity, menu: Menu?) {
        return
    }

    override fun onPrepareOptionsMenu(target: IconPackActivity, menu: Menu?): Boolean? {
        return null
    }

    override fun onOptionsItemSelected(target: IconPackActivity, item: MenuItem): Boolean? {
        return null
    }

    override fun onPrepareNavigateUpTaskStack(
        target: IconPackActivity, builder: TaskStackBuilder?) {
        return
    }

    override fun onActivityResult(
        target: IconPackActivity, requestCode: Int, resultCode: Int, data: Intent?) {
        return
    }

    override fun onAttachFragment(target: IconPackActivity, fragment: Fragment) {
        return
    }

    override fun onLowMemory(target: IconPackActivity) {
        return
    }

    override fun onNightModeChanged(target: IconPackActivity, mode: Int) {
        return
    }

    override fun onNewIntent(target: IconPackActivity, intent: Intent?) {
        return
    }

    override fun onMenuOpened(target: IconPackActivity, featureId: Int, menu: Menu): Boolean? {
        return null
    }

    override fun onDetachedFromWindow(target: IconPackActivity) {
        return
    }

    override fun onEnterAnimationComplete(target: IconPackActivity) {
        return
    }

    override fun onKeyLongPress(
        target: IconPackActivity, keyCode: Int, event: KeyEvent?): Boolean? {
        return null
    }

    override fun onNavigateUp(target: IconPackActivity): Boolean? {
        return null
    }

    override fun onInsetsChange(root: View, left: Int, top: Int, right: Int, bottom: Int) {

    }

    override fun onBackPressed(): Boolean {
        return false
    }

}