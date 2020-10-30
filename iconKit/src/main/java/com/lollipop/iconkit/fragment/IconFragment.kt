package com.lollipop.iconkit.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.iconcore.listener.WindowInsetsHelper
import com.lollipop.iconcore.ui.IconHelper
import com.lollipop.iconcore.ui.IconImageView
import com.lollipop.iconcore.util.delay
import com.lollipop.iconcore.util.doAsync
import com.lollipop.iconkit.LIconKit
import com.lollipop.iconkit.R
import com.lollipop.iconkit.dialog.PreviewIconDialog
import kotlinx.android.synthetic.main.kit_fragment_icon.*

/**
 * @author lollipop
 * @date 10/23/20 19:24
 */
class IconFragment: BaseTabFragment() {
    override val tabIcon: Int
        get() = R.drawable.ic_baseline_apps_24
    override val tabTitle: Int
        get() = R.string.icon
    override val tabColorId: Int
        get() = R.color.tabIconSelectedColor
    override val layoutId: Int
        get() = R.layout.kit_fragment_icon

    private val iconHelper = IconHelper.iconPackOnly(true) {
        LIconKit.createAppsPageMap(it)
    }

    private val previewIconDialog = PreviewIconDialog()

    private var appListInsetsHelper: WindowInsetsHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addBackPressedListener(previewIconDialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val column = resources.getInteger(R.integer.app_list_column)
        appList.layoutManager = GridLayoutManager(
            view.context, column, RecyclerView.VERTICAL, false)
        appList.adapter = IconAdapter(iconHelper) { view, icon ->
            previewIconDialog.show(view, icon)
        }
        doAsync {
            iconHelper.loadAppInfo(context!!)
            delay(appList.animate().duration) {
                appList.adapter?.notifyDataSetChanged()
            }
        }
        appListInsetsHelper = WindowInsetsHelper(appList, autoLayout = false)
        view.post {
            previewIconDialog.attach(activity!!)
        }
    }

    override fun onInsetsChange(root: View, left: Int, top: Int, right: Int, bottom: Int) {
        super.onInsetsChange(root, left, top, right, bottom)
        appListInsetsHelper?.setInsetsByPadding(left, top, right, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        appListInsetsHelper = null
        previewIconDialog.onDestroy()
        iconHelper.onDestroy()
    }

    private class IconAdapter(
        private val iconHelper: IconHelper,
        private val onClick: (View, Int) -> Unit): RecyclerView.Adapter<IconHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconHolder {
            return IconHolder.create(parent) { view, index ->
                onClick(view, iconHelper.getIconInfo(index).resId)
            }
        }

        override fun onBindViewHolder(holder: IconHolder, position: Int) {
            holder.bind(iconHelper.getIconInfo(position))
        }

        override fun getItemCount(): Int {
            return iconHelper.iconCount
        }
    }

    private class IconHolder
        private constructor(
            view: View,
            private val onClick: (View, Int) -> Unit): RecyclerView.ViewHolder(view) {

        companion object {
            fun create(group: ViewGroup, onClick: (View, Int) -> Unit): IconHolder {
                return IconHolder(
                    LayoutInflater.from(group.context)
                        .inflate(R.layout.kit_item_icon, group, false), onClick)
            }
        }

        private val iconView: IconImageView = itemView.findViewById(R.id.iconView)
        private val nameView: TextView = itemView.findViewById(R.id.nameView)

        init {
            itemView.setOnClickListener {
                onClick(iconView, adapterPosition)
            }
        }

        fun bind(iconInfo: IconHelper.IconInfo) {
            iconView.loadIcon(iconInfo.resId)
            nameView.text = iconInfo.name
        }

    }

}