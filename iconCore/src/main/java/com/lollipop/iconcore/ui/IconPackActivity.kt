package com.lollipop.iconcore.ui

import android.os.Bundle
import android.view.View
import android.view.Window
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

    protected override fun onWindowInsetsChange(root: View, left: Int, top: Int, right: Int, bottom: Int) {
        mainPageRenderer?.onInsetsChange(root, left, top, right, bottom)
    }

}