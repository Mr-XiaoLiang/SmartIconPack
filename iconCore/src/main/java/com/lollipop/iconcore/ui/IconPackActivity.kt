package com.lollipop.iconcore.ui

import android.graphics.Color
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.iconcore.provider.MainPageRenderer

/**
 * @author lollipop
 * @date 10/22/20 01:26
 * 被展示的主页Activity
 */
class IconPackActivity: AppCompatActivity() {

    private var mainpagerenderer: MainPageRenderer? = null

    fun bindRenderer(renderer: MainPageRenderer) {
        this.mainpagerenderer = renderer
    }

    private fun unbindRenderer() {
        this.mainpagerenderer = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        mainpagerenderer?.onStart(this)
    }

    override fun onStop() {
        super.onStop()
        mainpagerenderer?.onStop(this)
    }

    override fun onResume() {
        super.onResume()
        mainpagerenderer?.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        mainpagerenderer?.onPause(this)
    }

    override fun onRestart() {
        super.onRestart()
        mainpagerenderer?.onRestart(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainpagerenderer?.onDestroy(this)
        unbindRenderer()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mainpagerenderer?.onSaveInstanceState(this, outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mainpagerenderer?.onRestoreInstanceState(this, savedInstanceState)
    }

}