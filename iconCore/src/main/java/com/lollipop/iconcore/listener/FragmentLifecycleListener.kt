package com.lollipop.iconcore.listener

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

/**
 * @author lollipop
 * @date 9/21/20 01:33
 *
 * 这是Fragment的生命周期监听类
 * 它与AndroidX中的Lifecycle没有本质的区别，但是它脱离Android的版本限制，
 * 并且针对当前业务场景适配
 */
interface FragmentLifecycleListener: LifecycleListener<Fragment> {

    fun onAttach(target: Fragment, context: Context)

    fun onDetach(target: Fragment)

    fun onViewCreated(target: Fragment, view: View, savedInstanceState: Bundle?)

}