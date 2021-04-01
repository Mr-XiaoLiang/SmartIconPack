package com.lollipop.iconkit.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.lollipop.iconcore.listener.WindowInsetsHelper
import com.lollipop.iconcore.ui.BaseFragment
import com.lollipop.iconcore.ui.IconHelper
import com.lollipop.iconcore.ui.IconImageView
import com.lollipop.iconcore.ui.IconView
import com.lollipop.iconcore.util.*
import com.lollipop.iconcore.util.SharedPreferencesUtils.get
import com.lollipop.iconcore.util.SharedPreferencesUtils.set
import com.lollipop.iconkit.LIconKit
import com.lollipop.iconkit.R
import com.lollipop.iconkit.databinding.KitFragmentHomeBinding
import com.lollipop.iconkit.dialog.NewIconDialog
import com.lollipop.iconkit.dialog.UpdateInfoDialog

/**
 * @author lollipop
 * @date 10/23/20 19:24
 */
class HomeFragment: BaseFragment() {

    private val viewBinding: KitFragmentHomeBinding by lazyBind()

    companion object {
        private const val REQUEST_READ_SDCARD = 996

        private const val KEY_LAST_VERSION = "LAST_VERSION"
        private const val KEY_LAST_SUPPORT = "LAST_SUPPORT"
        private const val KEY_LAST_ICON = "LAST_ICON"

        private const val KEY_THIS_SUPPORT = "THIS_SUPPORT"
        private const val KEY_THIS_ICON = "THIS_ICON"

        fun lastSupportInfo(context: Context): String {
            return context[KEY_LAST_SUPPORT, ""]
        }

        fun lastIconInfo(context: Context): String {
            return context[KEY_LAST_ICON, ""]
        }
    }

    private var iconHelper = IconHelper.newHelper(IconHelper.FLAG_ALL_INFO) {
        LIconKit.createMultipleIconMap(it)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        // 先隐藏
        checkNewIconItem(viewBinding.newSupportIcon, viewBinding.newSupportTitle,
            viewBinding.newSupportCount, viewBinding.newSupportList,
            viewBinding.newSupportArrow, 0)
        checkNewIconItem(viewBinding.newIconIcon, viewBinding.newIconTitle,
            viewBinding.newIconCount, viewBinding.newIconList,
            viewBinding.newIconArrow, 0)
        doAsync {
            val ctx = view.context
            iconHelper.loadAppInfo(ctx)
            onUI {
                initIconView()
            }
            checkVersionUpdate(ctx)
            val newSupport = iconHelper.supportedDiffUp(
                IconHelper.parseAppInfo(
                    ctx, lastSupportInfo(ctx)))
            onUI {
                initNewSupportList(
                    viewBinding.newSupportIcon, viewBinding.newSupportTitle,
                    viewBinding.newSupportCount, viewBinding.newSupportList,
                    viewBinding.newSupportArrow, newSupport)
            }
            val newIcon = iconHelper.iconInfoDiffUp(
                IconHelper.parseIconInfo(
                    ctx, lastIconInfo(ctx)))
            onUI {
                initNewIconList(viewBinding.newIconIcon, viewBinding.newIconTitle,
                    viewBinding.newIconCount, viewBinding.newIconList,
                    viewBinding.newIconArrow, newIcon)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initIconView() {
        val supportedCount = iconHelper.supportedCount
        val allAppCount = iconHelper.allAppCount

        val iconGroup = IconGroup(viewBinding.pageRoot)
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


        viewBinding.supportQuantityValue.text = "$supportedCount/$allAppCount"
        viewBinding.supportQuantityProgress.progress = (100F * supportedCount / allAppCount).toInt()
    }

    private fun initView() {

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

        viewBinding.versionTitle.text = context?.versionName()?: "Unknown"
        viewBinding.versionBtn.setOnClickListener {
            activity?.let { activity ->
                val updateInfoProvider = LIconKit.createUpdateInfoProvider(activity)
                if (updateInfoProvider != null) {
                    UpdateInfoDialog(UpdateInfoManager(updateInfoProvider)).show(activity)
                }
            }
        }

        bindLinkInfo(viewBinding.linkGroup, ExternalLinkManager(LIconKit.createLinkInfoProvider(context!!)))
    }

    private fun initNewIconList(icon: View, title: View, count: TextView,
                                listView: RecyclerView, arrow: View,
                                data: List<IconHelper.IconInfo>) {
        if (!checkNewIconItem(icon, title, count, listView, arrow, data.size)) {
            return
        }

        val adapter = NewIconAdapter(data)
        listView.layoutManager = LinearLayoutManager(
            listView.context, RecyclerView.HORIZONTAL, false)
        listView.adapter = adapter
        adapter.notifyDataSetChanged()

        val dialog = NewIconDialog(data)
        arrow.setOnClickListener {
            activity?.let {
                dialog.show(it)
            }
        }
    }


    private fun initNewSupportList(icon: View, title: View, count: TextView,
                                   listView: RecyclerView, arrow: View,
                                   data: List<Any>) {
        checkNewIconItem(icon, title, count, listView, arrow, data.size)

        if (!checkNewIconItem(icon, title, count, listView, arrow, data.size)) {
            return
        }

        val adapter = NewIconAdapter(data)
        listView.layoutManager = LinearLayoutManager(
            listView.context, RecyclerView.HORIZONTAL, false)
        listView.adapter = adapter
        adapter.notifyDataSetChanged()

        val dialog = NewIconDialog(data)
        arrow.setOnClickListener {
            activity?.let {
                dialog.show(it)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun checkNewIconItem(icon: View, title: View, count: TextView,
                                 listView: RecyclerView, arrow: View, dataCount: Int): Boolean {
        if (dataCount == 0) {
            icon.visibility = View.GONE
            title.visibility = View.GONE
            count.visibility = View.GONE
            listView.visibility = View.GONE
            arrow.visibility = View.GONE
            return false
        }
        icon.visibility = View.VISIBLE
        title.visibility = View.VISIBLE
        count.visibility = View.VISIBLE
        listView.visibility = View.VISIBLE
        arrow.visibility = View.VISIBLE

        count.text = "+${dataCount}"
        return true
    }

    private fun checkVersionUpdate(context: Context) {
        val iconHelper = IconHelper.newHelper(IconHelper.FLAG_ALL_INFO) {
            LIconKit.createMultipleIconMap(it)
        }
        iconHelper.loadAppInfo(context)

        val lastVersion = context[KEY_LAST_VERSION, 0L]
        val thisVersion = context.versionCode()
        val supportedValue = iconHelper.supportedListToString(context)
        if (lastVersion != thisVersion) {
            val iconValue = iconHelper.iconListToString(context)
            context[KEY_LAST_SUPPORT] = context[KEY_THIS_SUPPORT, ""]
            context[KEY_LAST_ICON] = context[KEY_THIS_ICON, ""]
            context[KEY_THIS_ICON] = iconValue
            context[KEY_LAST_VERSION] = thisVersion
        }
        context[KEY_THIS_SUPPORT] = supportedValue
        iconHelper.onAppListChange {
            updateSupportInfo(context, it)
        }
    }

    private fun updateSupportInfo(context: Context, iconHelper: IconHelper) {
        doAsync {
            iconHelper.loadAppInfo(context, false)
            val supportedValue = iconHelper.supportedListToString(context)
            context[KEY_THIS_SUPPORT] = supportedValue
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        iconHelper.onDestroy()
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
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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
        viewBinding.headerImageView.setImageDrawable(wallpaperManager.drawable)
    }

    override fun onInsetsChange(root: View, left: Int, top: Int, right: Int, bottom: Int) {
        super.onInsetsChange(root, left, top, right, bottom)
        WindowInsetsHelper.setMargin(viewBinding.previewIcon1, left, top, 0, bottom)
        WindowInsetsHelper.setMargin(viewBinding.previewIcon4, 0, 0, right, 0)
    }

    private fun bindLinkInfo(group: ViewGroup, linkManager: ExternalLinkManager) {
        if (linkManager.linkCount < 1) {
            return
        }
        for (index in 0 until linkManager.linkCount) {
            val holder = LinkItemHolder.create(group)
            holder.bind(linkManager.getLink(index), index % 2 == 1)
            group.addView(holder.itemView)
        }
    }

    private class LinkItemHolder private constructor(val itemView: View) {
        companion object {
            fun create(group: ViewGroup): LinkItemHolder {
                return LinkItemHolder(
                    LayoutInflater.from(group.context)
                        .inflate(R.layout.kit_item_link, group, false)
                )
            }
        }

        fun bind(info: ExternalLinkManager.LinkInfo, showBackground: Boolean) {
            val titleView: TextView = itemView.findViewById(R.id.titleView)
            val summaryView: TextView = itemView.findViewById(R.id.summaryView)
            val iconView: ImageView = itemView.findViewById(R.id.iconView)
            val backgroundView: View = itemView.findViewById(R.id.backgroundView)

            OvalOutlineProvider.bind(iconView)

            backgroundView.visibility = if (showBackground) { View.VISIBLE } else { View.GONE }
            titleView.text = info.title
            summaryView.text = info.summary
            iconView.setImageResource(info.icon)
            itemView.setOnClickListener {
                try {
                    when(ExternalLinkManager.getLinkType(info.url)) {
                        ExternalLinkManager.LINK_TYPE_APP -> {
                            it.context.startActivity(info.url)
                        }
                        ExternalLinkManager.LINK_TYPE_STORE -> {
                            val uri = Uri.parse("market://details?id=${it.context.packageName}")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            it.context.startActivity(intent)
                        }
                        ExternalLinkManager.LINK_TYPE_WEB -> {
                            val webUrl = ExternalLinkManager.getWebUrl(info.url)
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
                            it.context.startActivity(intent)
                        }
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                    Snackbar.make(it, R.string.open_link_error, Snackbar.LENGTH_SHORT).show()
                }
            }
        }

    }

    private class NewIconAdapter(private val data: List<Any>): RecyclerView.Adapter<NewIconHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewIconHolder {
            return NewIconHolder.create(parent)
        }

        override fun onBindViewHolder(holder: NewIconHolder, position: Int) {
            val any = data[position]
            if (any is IconHelper.AppInfo) {
                holder.bind(any)
            } else if (any is IconHelper.IconInfo) {
                holder.bind(any)
            }
        }

        override fun getItemCount(): Int {
            return data.size
        }

    }

    private class NewIconHolder private constructor(view: View): RecyclerView.ViewHolder(view) {
        companion object {
            fun create(group: ViewGroup): NewIconHolder {
                return NewIconHolder(
                    LayoutInflater.from(group.context)
                        .inflate(R.layout.kit_item_new, group, false))
            }
        }

        private val iconView: IconImageView = itemView.findViewById(R.id.iconView)

        fun bind(info: IconHelper.AppInfo) {
            iconView.load(info)
        }

        fun bind(info: IconHelper.IconInfo) {
            iconView.load(info)
        }

    }

}