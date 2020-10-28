package com.lollipop.iconcore.listener

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

/**
 * @author lollipop
 * @date 9/22/20 00:44
 * 这是Fragment的生命周期监听分发类
 * 它与AndroidX中的Lifecycle没有本质的区别，但是它脱离Android的版本限制，
 * 并且针对当前业务场景适配
 */
class FragmentLifecycleHelper {

    private val listenerList = ArrayList<FragmentLifecycleListener>()

    private var target: Fragment? = null

    fun addLifecycleListener(listener: FragmentLifecycleListener) {
        listenerList.add(listener)
    }

    fun removeLifecycleListener(listener: FragmentLifecycleListener) {
        listenerList.remove(listener)
    }

    fun bindFragment(fragment: Fragment) {
        target = fragment
    }

    fun onAttach(context: Context) {
        target?.let { fragment ->
            listenerList.forEach { it.onAttach(fragment, context) }
        }
    }

    fun onDetach() {
        target?.let { fragment ->
            listenerList.forEach { it.onDetach(fragment) }
        }
    }

    fun onCreate(fragment: Fragment, savedInstanceState: Bundle?) {
        bindFragment(fragment)
        listenerList.forEach { it.onCreate(fragment, savedInstanceState) }
    }

    fun onStart() {
        target?.let { fragment ->
            listenerList.forEach { it.onStart(fragment) }
        }
    }

    fun onStop() {
        target?.let { fragment ->
            listenerList.forEach { it.onStop(fragment) }
        }
    }

    fun onResume() {
        target?.let { fragment ->
            listenerList.forEach { it.onResume(fragment) }
        }
    }

    fun onPause() {
        target?.let { fragment ->
            listenerList.forEach { it.onPause(fragment) }
        }
    }

    fun onDestroy() {
        target?.let { fragment ->
            listenerList.forEach { it.onDestroy(fragment) }
        }
    }

    fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        target?.let { fragment ->
            listenerList.forEach { it.onViewCreated(fragment, view, savedInstanceState) }
        }
    }

}