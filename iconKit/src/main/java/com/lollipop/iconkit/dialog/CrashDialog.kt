package com.lollipop.iconkit.dialog

import android.view.View
import com.lollipop.iconcore.util.CrashHandler
import com.lollipop.iconkit.LIconKit
import com.lollipop.iconkit.R
import com.lollipop.iconkit.dialog.base.InnerDialogProvider
import java.io.File

/**
 * @author lollipop
 * @date 11/7/20 15:24
 * 崩溃信息的Dialog
 */
class CrashDialog(private val crashFile: File): InnerDialogProvider() {

    override val layoutId: Int
        get() = R.layout.kit_dialog_crash

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        find<View>(R.id.cleanBtn)?.setOnClickListener {
            CrashHandler.clean(it.context)
            dismiss()
        }
        find<View>(R.id.ignoreBtn)?.setOnClickListener {
            CrashHandler.resetCrashFlag(it.context)
            dismiss()
        }
        find<View>(R.id.sendBtn)?.setOnClickListener {
            CrashHandler.resetCrashFlag(it.context)
            LIconKit.mailTo(
                it.context,
                R.string.title_choose_email,
                R.string.email_request_subject,
                R.string.app_name,
                crashFile)
        }
    }

    override fun onBackPressed(): Boolean {
        return true
    }

}