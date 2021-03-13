package com.lollipop.iconcore.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.lollipop.iconcore.listener.*

/**
 * @author lollipop
 * @date 10/24/20 20:24
 *
 * 基础的Fragment，它提供了：
 * 基础的返回事件监听与分发
 * 基础的窗口缩进事件接受与分发
 * 自动创建View（需要设置layoutId）
 *
 */
open class BaseFragment: Fragment(),
        BackPressedListener,
        BackPressedProvider,
        OnWindowInsetsProvider,
        OnWindowInsetsListener {

    private var lifecycleHelper: FragmentLifecycleHelper = FragmentLifecycleHelper()

    private val windowInsetsProviderHelper: WindowInsetsProviderHelper by lazy {
        WindowInsetsProviderHelper()
    }

    private val backPressedProviderHelper: BackPressedProviderHelper by lazy {
        BackPressedProviderHelper()
    }

    open val isLightStatusBar = false

    open val isLightNavigationBar = false

    private fun setViewFlag(open: Boolean, flag: Int) {
        val decorView = activity?.window?.decorView?:return
        val systemUiFlag = decorView.systemUiVisibility
        val has = systemUiFlag and flag != 0
        if (open == has) {
            return
        }
        if (open) {
            decorView.systemUiVisibility = (systemUiFlag or flag)
        } else {
            decorView.systemUiVisibility = (systemUiFlag xor flag)
        }
    }

    protected fun supportLifecycle(fragment: Fragment) {
        lifecycleHelper.bindFragment(fragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportLifecycle(this)
        super.onCreate(savedInstanceState)
        lifecycleHelper.onCreate(this, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleHelper.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        windowInsetsProviderHelper.call(this)
        lifecycleHelper.onStart()
    }

    override fun onResume() {
        super.onResume()
        setViewFlag(isLightStatusBar, View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        setViewFlag(isLightNavigationBar, View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
        lifecycleHelper.onResume()
    }

    override fun onPause() {
        super.onPause()
        lifecycleHelper.onPause()
    }

    override fun onStop() {
        super.onStop()
        lifecycleHelper.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleHelper.onDestroy()
        windowInsetsProviderHelper.destroy()
        backPressedProviderHelper.destroy()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        lifecycleHelper.onAttach(context)
        if (context is OnWindowInsetsProvider) {
            context.addOnWindowInsetsProvider(this)
        }
        if (context is BackPressedProvider) {
            context.addBackPressedListener(this)
        }
    }

    override fun onDetach() {
        super.onDetach()
        lifecycleHelper.onDetach()
    }

    fun addLifecycleListener(listener: FragmentLifecycleListener) {
        lifecycleHelper.addLifecycleListener(listener)
    }

    fun removeLifecycleListener(listener: FragmentLifecycleListener) {
        lifecycleHelper.removeLifecycleListener(listener)
    }

    override fun onBackPressed(): Boolean {
        return backPressedProviderHelper.onBackPressed()
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

    override fun onInsetsChange(root: View, left: Int, top: Int, right: Int, bottom: Int) {
        windowInsetsProviderHelper.onInsetsChange(root, left, top, right, bottom)
    }

}