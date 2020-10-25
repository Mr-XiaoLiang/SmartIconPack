package com.lollipop.iconkit.dialog.base

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.view.*
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.lollipop.iconcore.listener.*
import com.lollipop.iconkit.R
import java.util.*

/**
 * @author lollipop
 * @date 2020/5/12 23:03
 * 帘子一样垂下的Dialog
 * 它会在保持页面不变的情况下尽可能的提供更多的内容空间
 */
class CurtainDialog private constructor(
    private val rootGroup: ViewGroup,
    private val onWindowInsetsProvider: OnWindowInsetsProvider?,
    private val backPressedProvider: BackPressedProvider?
) :
    View.OnClickListener,
    ValueAnimator.AnimatorUpdateListener,
    Animator.AnimatorListener,
    OnWindowInsetsListener,
    BackPressedProvider,
    BackPressedListener,
    InnerDialogProvider.Callback {

    companion object {
        private const val DURATION = 300L

        fun with(activity: Activity): CurtainDialog {
            val provider = if (activity is OnWindowInsetsProvider) {
                activity
            } else {
                null
            }
            val backProvider = if (activity is BackPressedProvider) {
                activity
            } else {
                null
            }
            return CurtainDialog(
                findGroup(activity.window.decorView)
                    ?: throw InflateException("Not fount root group"), provider, backProvider
            )
        }

        fun with(fragment: Fragment): CurtainDialog {
            val provider = if (fragment is OnWindowInsetsProvider) {
                fragment
            } else {
                null
            }
            val backProvider = if (fragment is BackPressedProvider) {
                fragment
            } else {
                null
            }
            return CurtainDialog(
                findGroup(fragment.view)
                    ?: throw InflateException("Not fount root group"), provider, backProvider
            )
        }

        private fun findGroup(rootGroup: View?): ViewGroup? {
            rootGroup ?: return null
            if (rootGroup is CoordinatorLayout) {
                return rootGroup
            }
            if (rootGroup is FrameLayout) {
                return rootGroup
            }
            if (rootGroup is ViewGroup) {
                val views = LinkedList<View>()
                views.add(rootGroup)
                // 按层次遍历
                while (views.isNotEmpty()) {
                    val view = views.removeFirst()
                    if (view is CoordinatorLayout) {
                        return view
                    }
                    if (view is FrameLayout) {
                        return view
                    }
                    if (view is ViewGroup) {
                        for (i in 0 until view.childCount) {
                            views.addLast(view.getChildAt(i))
                        }
                    }
                }
            }
            return null
        }

    }

    private val dialogView = LayoutInflater.from(rootGroup.context)
        .inflate(R.layout.kit_bottom_dialog, rootGroup, false)

    private val backgroundView: View = dialogView.findViewById(R.id.dialogBackground)
    private val contentGroup: ViewGroup = dialogView.findViewById(R.id.dialogGroup)

    private var progress = 0F
    private var pullCurtain = false
    private val valueAnimator = ValueAnimator()
    private var once = false
    private var innerDialogProvider: InnerDialogProvider? = null

    private val windowInsetsHelper: WindowInsetsHelper by lazy {
        WindowInsetsHelper(contentGroup)
    }

    private val backPressedProviderHelper: BackPressedProviderHelper by lazy {
        BackPressedProviderHelper()
    }

    init {
        valueAnimator.addUpdateListener(this)
        valueAnimator.addListener(this)
        dialogView.setOnClickListener(this)
        contentGroup.setOnClickListener {}
        dialogView.visibility = View.INVISIBLE
    }

    fun dismiss() {
        backPressedProvider?.removeBackPressedListener(this)
        if (!rootGroup.isAttachedToWindow || rootGroup.parent == null || !rootGroup.isShown) {
            return
        }
        innerDialogProvider?.onStop()
        doAnimation(false)
    }

    fun showOnce() {
        once = true
        show()
    }

    fun show() {
        if (dialogView.parent == null || dialogView.parent != rootGroup) {
            dialogView.parent?.let { parent ->
                if (parent is ViewManager) {
                    parent.removeView(dialogView)
                }
            }
            innerDialogProvider?.let { provider ->
                provider.onCreate()
                val contentView = provider.createContentView(contentGroup)
                contentGroup.addView(contentView)
                provider.onViewCreated(contentView)
            }
            rootGroup.addView(dialogView)
            windowInsetsHelper.baseMarginFromNow()
            onWindowInsetsProvider?.addOnWindowInsetsProvider(this)
        }
        dialogView.post {
            innerDialogProvider?.onStart()
            backPressedProvider?.addBackPressedListener(this)
            doAnimation(true)
        }
    }

    fun bindProvider(provider: InnerDialogProvider): CurtainDialog {
        if (innerDialogProvider == provider) {
            return this
        }
        innerDialogProvider?.let {
            removeBackPressedListener(it)
            it.bindCallback(null)
        }
        this.innerDialogProvider = provider
        provider.bindCallback(this)
        addBackPressedListener(provider)
        return this
    }

    private fun removeView() {
        innerDialogProvider?.onDestroy()
        backPressedProviderHelper.destroy()
        if (dialogView.isAttachedToWindow) {
            dialogView.parent?.let { parent ->
                if (parent is ViewManager) {
                    parent.removeView(dialogView)
                }
            }
        }
        onWindowInsetsProvider?.removeOnWindowInsetsProvider(this)
    }

    private fun doAnimation(open: Boolean) {
        pullCurtain = open
        valueAnimator.cancel()
        val endValue = if (open) {
            1F
        } else {
            0F
        }
        valueAnimator.duration = if (open) {
            (endValue - progress) * DURATION
        } else {
            (progress - endValue) * DURATION
        }.toLong()
        valueAnimator.setFloatValues(progress, endValue)
        valueAnimator.start()
    }

    private fun onProgressChange() {
        if (progress > 1F) {
            progress = 1F
        } else if (progress < 0F) {
            progress = 0F
        }
        backgroundView.alpha = progress
        contentGroup.translationY = (contentGroup.bottom + contentGroup.height) * (1 - progress)
    }

    override fun onClick(v: View?) {
        when (v) {
            dialogView -> {
                onBackPressed()
            }
        }
    }

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        if (animation == valueAnimator) {
            progress = animation.animatedValue as Float
            onProgressChange()
        }
    }

    override fun onAnimationRepeat(animation: Animator?) {}

    override fun onAnimationEnd(animation: Animator?) {
        if (!pullCurtain) {
            dialogView.visibility = View.INVISIBLE
            if (once) {
                removeView()
            }
        }
    }

    override fun onBackPressed(): Boolean {
        if (backPressedProviderHelper.onBackPressed()) {
            return true
        }
        dismiss()
        return true
    }

    override fun onAnimationCancel(animation: Animator?) {}

    override fun onAnimationStart(animation: Animator?) {
        if (pullCurtain) {
            dialogView.visibility = View.VISIBLE
        }
    }

    override fun onInsetsChange(root: View, left: Int, top: Int, right: Int, bottom: Int) {
        windowInsetsHelper.updateByMargin(root, left, top, right, bottom)
    }

    override fun addBackPressedListener(listener: BackPressedListener) {
        backPressedProviderHelper.addBackPressedListener(listener)
    }

    override fun removeBackPressedListener(listener: BackPressedListener) {
        backPressedProviderHelper.removeBackPressedListener(listener)
    }

    override fun callDismiss() {
        dismiss()
    }

}