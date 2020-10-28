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

    /**
     * 设置Layout的ID，以此来简化开发过程
     * 在{@link #onCreateView}时，将会使用它来实例化View
     */
    open val layoutId = 0

    private var lifecycleHelper: FragmentLifecycleHelper = FragmentLifecycleHelper()

    private val windowInsetsProviderHelper: WindowInsetsProviderHelper by lazy {
        WindowInsetsProviderHelper()
    }

    private val backPressedProviderHelper: BackPressedProviderHelper by lazy {
        BackPressedProviderHelper()
    }

    protected fun supportLifecycle(fragment: Fragment) {
        lifecycleHelper.bindFragment(fragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportLifecycle(this)
        super.onCreate(savedInstanceState)
        lifecycleHelper.onCreate(this, savedInstanceState)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (layoutId != 0) {
            return inflater.inflate(layoutId, container, false)
        }
        return super.onCreateView(inflater, container, savedInstanceState)
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