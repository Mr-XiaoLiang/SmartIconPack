package com.lollipop.iconkit.dialog.base

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lollipop.iconcore.listener.BackPressedListener
import com.lollipop.iconcore.listener.OnWindowInsetsListener

/**
 * @author lollipop
 * @date 2020/5/14 22:58
 * 内部Dialog的呈现器
 */
open class InnerDialogProvider: BackPressedListener, OnWindowInsetsListener {

    private var callback: Callback? = null

    open val layoutId = 0

    private var view: View? = null
        private set

    fun bindCallback(callback: Callback?) {
        this.callback = callback
    }

    protected fun dismiss() {
        callback?.callDismiss()
    }

    open fun onCreate() {

    }

    open fun onStart() {

    }

    open fun onStop() {

    }

    open fun onDestroy() {

    }

    open fun createContentView(group: ViewGroup): View {
        return createViewById(group)
    }

    open fun onViewCreated(view: View) {

    }

    private fun createViewById(group: ViewGroup): View {
        if (layoutId == 0) {
            throw NullPointerException("layoutId == 0")
        }
        val thisView = LayoutInflater.from(group.context).inflate(layoutId, group, false)
        view = thisView
        return thisView
    }

    interface Callback {
        fun callDismiss()
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    fun <T: View> find(id: Int): T? {
        return view?.findViewById<T>(id)
    }

    fun show(activity: Activity) {
        val curtainDialog = CurtainDialog.with(activity)
        curtainDialog.bindProvider(this)
        curtainDialog.show()
    }

    override fun onInsetsChange(root: View, left: Int, top: Int, right: Int, bottom: Int) {

    }

}