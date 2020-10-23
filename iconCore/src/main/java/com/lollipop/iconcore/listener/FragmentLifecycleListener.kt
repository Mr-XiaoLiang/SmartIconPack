package com.lollipop.iconcore.listener

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

/**
 * @author lollipop
 * @date 9/21/20 01:33
 */
interface FragmentLifecycleListener: LifecycleListener<Fragment> {

    fun onAttach(target: Fragment, context: Context)

    fun onDetach(target: Fragment)

    fun onViewCreated(target: Fragment, view: View, savedInstanceState: Bundle?)

}