package com.lollipop.iconkit.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.content.PermissionChecker
import com.lollipop.iconcore.listener.WindowInsetsHelper
import com.lollipop.iconcore.ui.IconHelper
import com.lollipop.iconcore.ui.IconView
import com.lollipop.iconcore.util.*
import com.lollipop.iconkit.LIconKit
import com.lollipop.iconkit.R
import com.lollipop.iconkit.dialog.UpdateInfoDialog
import kotlinx.android.synthetic.main.kit_fragment_home.*

/**
 * @author lollipop
 * @date 10/23/20 19:24
 */
class HomeFragment: BaseTabFragment() {
    override val tabIcon: Int
        get() = R.drawable.ic_baseline_home_24
    override val tabTitle: Int
        get() = R.string.home
    override val tabColorId: Int
        get() = R.color.tabHomeSelectedColor
    override val layoutId: Int
        get() = R.layout.kit_fragment_home

    companion object {
        private const val REQUEST_READ_SDCARD = 996
    }

    private var iconHelper = IconHelper.newHelper {
        LIconKit.createHomePageMap(it)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        iconHelper.loadAppInfo(context)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val supportedCount = iconHelper.supportedCount
        val allAppCount = iconHelper.allAppCount

        val iconGroup = IconGroup(pageRoot)
        val fit: (icon: IconView, index: Int) -> Unit = { icon, index ->
            val iconPack = iconHelper.getAppInfo(index).iconPack
            if (iconPack.isEmpty()) {
                icon.loadIcon(0)
            } else {
                icon.loadIcon(iconPack[0])
            }
        }
        iconGroup.autoFit(supportedCount, fit)
        // 只有支持数量超过了展示数量，那么才能点击切换
        if (supportedCount > iconGroup.iconCount) {
            iconGroup.forEach { iconView ->
                if (iconView is View) {
                    iconView.setOnClickListener(IconChangeCallback(iconGroup, fit))
                }
            }
        }


        supportQuantityValue.text = "$supportedCount/$allAppCount"
        supportQuantityProgress.progress = (100F * supportedCount / allAppCount).toInt()

        val checkSelfPermission = PermissionChecker.checkSelfPermission(
            context!!, Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (checkSelfPermission != PermissionChecker.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_READ_SDCARD
            )
        } else {
            setHeadWallpaper()
        }

        versionTitle.text = versionName()
        versionBtn.setOnClickListener {
            activity?.let { activity ->
                val updateInfoProvider = LIconKit.createUpdateInfoProvider(activity)
                if (updateInfoProvider != null) {
                    UpdateInfoDialog(UpdateInfoManager(updateInfoProvider)).show(activity)
                }
            }
        }
    }

    private fun versionName(): String {
        return context?.let {
            it.packageManager.getPackageInfo(it.packageName, 0).versionName
        } ?: "Unknown"
    }

    private class IconChangeCallback(
        private val iconGroup: IconGroup,
        private val iconFit: (icon: IconView, index: Int) -> Unit
    ): View.OnClickListener {

        private var isIntAnimation = false

        override fun onClick(v: View?) {
            if (isIntAnimation) {
                return
            }
            v?:return
            if (v is IconView) {
                viewHide(v) {
                    iconGroup.changeIcon(v, iconFit)
                    viewShow(v)
                }
            }
        }

        private fun viewHide(v: View, endCallback: () -> Unit) {
            v.animate().apply {
                cancel()
                scaleX(0F)
                scaleY(0F)
                alpha(0F)
                lifecycleBinding {
                    onStart {
                        isIntAnimation = true
                    }
                    onEnd {
                        isIntAnimation = false
                        removeThis(it)
                        endCallback()
                    }
                }
                start()
            }
        }

        private fun viewShow(v: View) {
            v.animate().apply {
                cancel()
                scaleX(1F)
                scaleY(1F)
                alpha(1F)
                lifecycleBinding {
                    onStart {
                        isIntAnimation = true
                    }
                    onEnd {
                        isIntAnimation = false
                        removeThis(it)
                    }
                }
                start()
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ_SDCARD) {
            val index = permissions.indexOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            if (index >= 0 && grantResults[index] == PermissionChecker.PERMISSION_GRANTED) {
                setHeadWallpaper()
            }
        }
    }

    private fun setHeadWallpaper() {
        val wallpaperManager = WallpaperManager.getInstance(context)
        headerImageView.setImageDrawable(wallpaperManager.drawable)
    }

    override fun onInsetsChange(root: View, left: Int, top: Int, right: Int, bottom: Int) {
        super.onInsetsChange(root, left, top, right, bottom)
        WindowInsetsHelper.setMargin(previewIcon1, left, top, 0, bottom)
        WindowInsetsHelper.setMargin(previewIcon4, 0, 0, right, 0)
    }

}