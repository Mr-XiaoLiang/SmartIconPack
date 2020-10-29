package com.lollipop.iconcore.provider

import android.app.TaskStackBuilder
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.lollipop.iconcore.listener.BackPressedListener
import com.lollipop.iconcore.listener.LifecycleListener
import com.lollipop.iconcore.listener.OnWindowInsetsListener
import com.lollipop.iconcore.ui.IconPackActivity

/**
 * @author lollipop
 * @date 10/22/20 01:42
 * 主页的真正实现者
 *
 * 它具备了基本的生命周期接口，
 * 以及窗口缩进事件分发接口
 * （需要调用initRootGroup注册事件或者手动实现），
 * 返回事件处理分发接口
 *
 */
interface MainPageRenderer: LifecycleListener<IconPackActivity>,
        OnWindowInsetsListener, BackPressedListener {

    fun onSaveInstanceState(target: IconPackActivity, outState: Bundle)

    fun onRestoreInstanceState(target: IconPackActivity, savedInstanceState: Bundle)

    fun onSupportNavigateUp(target: IconPackActivity): Boolean?

    fun onActionModeFinished(target: IconPackActivity, mode: ActionMode?)

    fun onActionModeStarted(target: IconPackActivity, mode: ActionMode?)

    fun onActivityReenter(target: IconPackActivity, resultCode: Int, data: Intent?)

    fun onApplyThemeResource(target: IconPackActivity, theme: Resources.Theme?, resid: Int, first: Boolean)

    fun onAttachedToWindow(target: IconPackActivity, )

    fun onContextMenuClosed(target: IconPackActivity, menu: Menu)

    fun onCreateContextMenu(target: IconPackActivity, menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?)

    fun onCreateDescription(target: IconPackActivity, ): CharSequence?

    fun onCreateOptionsMenu(target: IconPackActivity, menu: Menu?): Boolean?

    fun onOptionsMenuClosed(target: IconPackActivity, menu: Menu?)

    fun onPrepareOptionsMenu(target: IconPackActivity, menu: Menu?): Boolean?

    fun onOptionsItemSelected(target: IconPackActivity, item: MenuItem): Boolean?

    fun onPrepareNavigateUpTaskStack(target: IconPackActivity, builder: TaskStackBuilder?)

    fun onActivityResult(target: IconPackActivity, requestCode: Int, resultCode: Int, data: Intent?)

    fun onAttachFragment(target: IconPackActivity, fragment: Fragment)

    fun onLowMemory(target: IconPackActivity, )

    fun onNightModeChanged(target: IconPackActivity, mode: Int)

    fun onNewIntent(target: IconPackActivity, intent: Intent?)

    fun onMenuOpened(target: IconPackActivity, featureId: Int, menu: Menu): Boolean?

    fun onDetachedFromWindow(target: IconPackActivity, )

    fun onEnterAnimationComplete(target: IconPackActivity, )

    fun onKeyLongPress(target: IconPackActivity, keyCode: Int, event: KeyEvent?): Boolean?

    fun onNavigateUp(target: IconPackActivity, ): Boolean?

}