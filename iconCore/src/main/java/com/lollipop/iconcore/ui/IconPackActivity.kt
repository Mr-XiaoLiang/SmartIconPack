package com.lollipop.iconcore.ui

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.iconcore.listener.*
import com.lollipop.iconcore.provider.MainPageRenderer

/**
 * @author lollipop
 * @date 10/22/20 01:26
 * 被展示的主页Activity
 */
class IconPackActivity: AppCompatActivity(), BackPressedProvider, OnWindowInsetsProvider {

    private val backPressedProviderHelper = BackPressedProviderHelper()

    private val windowInsetsProviderHelper = WindowInsetsProviderHelper()

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
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
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
        backPressedProviderHelper.destroy()
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
        if (backPressedProviderHelper.onBackPressed()) {
            return
        }
        super.onBackPressed()
    }

    fun initRootGroup(group: View) {
        val attributes = window.attributes
        attributes.systemUiVisibility = (
                attributes.systemUiVisibility
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        group.fitsSystemWindows = true
        group.setOnApplyWindowInsetsListener { _, insets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val systemInsets = insets.getInsets(WindowInsets.Type.systemBars())
                onWindowInsetsChange(group,
                        systemInsets.left, systemInsets.top,
                        systemInsets.right, systemInsets.bottom)
                windowInsetsProviderHelper.onInsetsChange(group,
                        systemInsets.left, systemInsets.top,
                        systemInsets.right, systemInsets.bottom)
                WindowInsets.CONSUMED
            } else {
                windowInsetsProviderHelper.onInsetsChange(group,
                        insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                        insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
                onWindowInsetsChange(group,
                        insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                        insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
                insets.consumeSystemWindowInsets()
            }
        }

    }

    private fun onWindowInsetsChange(root: View, left: Int, top: Int, right: Int, bottom: Int) {
        mainPageRenderer?.onInsetsChange(root, left, top, right, bottom)
    }

    override fun addBackPressedListener(listener: BackPressedListener) {
        backPressedProviderHelper.addBackPressedListener(listener)
    }

    override fun removeBackPressedListener(listener: BackPressedListener) {
        backPressedProviderHelper.removeBackPressedListener(listener)
    }

    override fun addOnWindowInsetsProvider(listener: OnWindowInsetsListener) {
        windowInsetsProviderHelper.addOnWindowInsetsProvider(listener)
    }

    override fun removeOnWindowInsetsProvider(listener: OnWindowInsetsListener) {
        windowInsetsProviderHelper.removeOnWindowInsetsProvider(listener)
    }

}