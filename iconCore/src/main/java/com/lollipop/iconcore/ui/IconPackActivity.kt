package com.lollipop.iconcore.ui

import android.app.TaskStackBuilder
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.lollipop.iconcore.provider.MainPageRenderer

/**
 * @author lollipop
 * @date 10/22/20 01:26
 * 被展示的主页Activity
 */
class IconPackActivity: BaseActivity() {

    private var mainPageRenderer: MainPageRenderer? = null

    fun bindRenderer(renderer: MainPageRenderer) {
        this.mainPageRenderer = renderer
    }

    private fun unbindRenderer() {
        this.mainPageRenderer = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        mainPageRenderer?.onCreate(this, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        mainPageRenderer?.onStart(this)
    }

    override fun onStop() {
        super.onStop()
        mainPageRenderer?.onStop(this)
    }

    override fun onResume() {
        super.onResume()
        mainPageRenderer?.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        mainPageRenderer?.onPause(this)
    }

    override fun onRestart() {
        super.onRestart()
        mainPageRenderer?.onRestart(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainPageRenderer?.onDestroy(this)
        unbindRenderer()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mainPageRenderer?.onSaveInstanceState(this, outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mainPageRenderer?.onRestoreInstanceState(this, savedInstanceState)
    }

    override fun onBackPressed() {
        if (mainPageRenderer?.onBackPressed() == true) {
            return
        }
        super.onBackPressed()
    }

    override fun onWindowInsetsChange(root: View, left: Int, top: Int, right: Int, bottom: Int) {
        mainPageRenderer?.onInsetsChange(root, left, top, right, bottom)
    }

    override fun onSupportNavigateUp(): Boolean {
        return mainPageRenderer?.onSupportNavigateUp(this)?: super.onSupportNavigateUp()
    }

    override fun onActionModeFinished(mode: ActionMode?) {
        super.onActionModeFinished(mode)
        mainPageRenderer?.onActionModeFinished(this, mode)
    }

    override fun onActionModeStarted(mode: ActionMode?) {
        super.onActionModeStarted(mode)
        mainPageRenderer?.onActionModeStarted(this, mode)
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)
        mainPageRenderer?.onActivityReenter(this, resultCode, data)
    }

    override fun onApplyThemeResource(theme: Resources.Theme?, resid: Int, first: Boolean) {
        super.onApplyThemeResource(theme, resid, first)
        mainPageRenderer?.onApplyThemeResource(this, theme, resid, first)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mainPageRenderer?.onAttachedToWindow(this)
    }

    override fun onContextMenuClosed(menu: Menu) {
        super.onContextMenuClosed(menu)
        mainPageRenderer?.onContextMenuClosed(this, menu)
    }

    override fun onCreateContextMenu(menu: ContextMenu?,
                                     v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        mainPageRenderer?.onCreateContextMenu(this, menu, v, menuInfo)
    }

    override fun onCreateDescription(): CharSequence? {
        return mainPageRenderer?.onCreateDescription(this)?:super.onCreateDescription()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return mainPageRenderer?.onCreateOptionsMenu(
            this, menu)?:super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsMenuClosed(menu: Menu?) {
        super.onOptionsMenuClosed(menu)
        mainPageRenderer?.onOptionsMenuClosed(this, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return mainPageRenderer?.onPrepareOptionsMenu(
            this, menu)?:super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return mainPageRenderer?.onOptionsItemSelected(
            this, item)?:super.onOptionsItemSelected(item)
    }

    override fun onPrepareNavigateUpTaskStack(builder: TaskStackBuilder?) {
        super.onPrepareNavigateUpTaskStack(builder)
        mainPageRenderer?.onPrepareNavigateUpTaskStack(this, builder)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mainPageRenderer?.onActivityResult(this, requestCode, resultCode, data)
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
        mainPageRenderer?.onAttachFragment(this, fragment)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mainPageRenderer?.onLowMemory(this)
    }

    override fun onNightModeChanged(mode: Int) {
        super.onNightModeChanged(mode)
        mainPageRenderer?.onNightModeChanged(this, mode)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        mainPageRenderer?.onNewIntent(this, intent)
    }

    override fun onMenuOpened(featureId: Int, menu: Menu): Boolean {
        return mainPageRenderer?.onMenuOpened(
            this, featureId, menu)?:super.onMenuOpened(featureId, menu)

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mainPageRenderer?.onDetachedFromWindow(this)
    }

    override fun onEnterAnimationComplete() {
        super.onEnterAnimationComplete()
        mainPageRenderer?.onEnterAnimationComplete(this)
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        return mainPageRenderer?.onKeyLongPress(
            this, keyCode, event)?:super.onKeyLongPress(keyCode, event)
    }

    override fun onNavigateUp(): Boolean {
        return mainPageRenderer?.onNavigateUp(this)?:super.onNavigateUp()
    }

}