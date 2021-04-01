package com.lollipop.iconkit.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.iconcore.listener.WindowInsetsHelper
import com.lollipop.iconcore.ui.BaseFragment
import com.lollipop.iconcore.ui.IconHelper
import com.lollipop.iconcore.ui.IconImageView
import com.lollipop.iconcore.util.delay
import com.lollipop.iconcore.util.doAsync
import com.lollipop.iconcore.util.lazyBind
import com.lollipop.iconcore.util.zeroTo
import com.lollipop.iconkit.LIconKit
import com.lollipop.iconkit.R
import com.lollipop.iconkit.databinding.KitFragmentIconBinding
import com.lollipop.iconkit.dialog.PreviewIconDialog

/**
 * @author lollipop
 * @date 10/23/20 19:24
 */
class IconFragment: BaseFragment() {

    override val isLightStatusBar: Boolean
        get() = true

    private val viewBinding: KitFragmentIconBinding by lazyBind()

    private val iconHelper = IconHelper.iconPackOnly(true) {
        LIconKit.createAppsPageMap(it)
    }

    private val previewIconDialog = PreviewIconDialog()

    private var appListInsetsHelper: WindowInsetsHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addBackPressedListener(previewIconDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val column = resources.getInteger(R.integer.app_list_column)
        viewBinding.appList.layoutManager = GridLayoutManager(
            view.context, column, RecyclerView.VERTICAL, false)
        viewBinding.appList.adapter = IconAdapter(iconHelper) { v, icon ->
            previewIconDialog.show(v, icon)
        }
        doAsync {
            iconHelper.loadAppInfo(context!!)
            delay(viewBinding.appList.animate().duration) {
                viewBinding.appList.adapter?.notifyDataSetChanged()
            }
        }
        appListInsetsHelper = WindowInsetsHelper(viewBinding.appList, autoLayout = false)
        view.post {
            previewIconDialog.attach(activity!!)
        }
    }

    override fun onInsetsChange(root: View, left: Int, top: Int, right: Int, bottom: Int) {
        super.onInsetsChange(root, left, top, right, bottom)
        appListInsetsHelper?.setInsetsByPadding(
            left.zeroTo { root.resources.getDimensionPixelSize(R.dimen.left_space) },
            top,
            right.zeroTo { root.resources.getDimensionPixelSize(R.dimen.right_space) },
            0)
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
            iconView.load(iconInfo)
            nameView.text = iconInfo.name
        }

    }

}