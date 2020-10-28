package com.lollipop.iconkit.dialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.iconcore.listener.OnWindowInsetsListener
import com.lollipop.iconcore.listener.WindowInsetsHelper
import com.lollipop.iconcore.util.UpdateInfoManager
import com.lollipop.iconkit.R
import com.lollipop.iconkit.dialog.base.InnerDialogProvider
import com.lollipop.iconkit.view.LBannerLayoutManager

/**
 * @author lollipop
 * @date 10/25/20 22:41
 */
class UpdateInfoDialog(private val updateInfoManager: UpdateInfoManager):
    InnerDialogProvider(), OnWindowInsetsListener {

    override val layoutId: Int
        get() = R.layout.kit_dialog_update_info

    private var windowInsetsHelper: WindowInsetsHelper? = null

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        val pageGroup: RecyclerView = find(R.id.updateInfoPage) ?: return
        windowInsetsHelper = WindowInsetsHelper(pageGroup)
        windowInsetsHelper?.baseMarginFromNow()
        pageGroup.adapter = PageAdapter(updateInfoManager)
        pageGroup.layoutManager = LinearLayoutManager(
            pageGroup.context, RecyclerView.HORIZONTAL, false)
        pageGroup.layoutManager = LBannerLayoutManager()
        pageGroup.adapter?.notifyDataSetChanged()
        PagerSnapHelper().attachToRecyclerView(pageGroup)
    }

    override fun onInsetsChange(root: View, left: Int, top: Int, right: Int, bottom: Int) {
        windowInsetsHelper?.updateByMargin(root, left, top, right, bottom)
    }

    private class PageAdapter(private val infoManager: UpdateInfoManager):
        RecyclerView.Adapter<PageHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageHolder {
            return PageHolder.create(parent)
        }

        override fun onBindViewHolder(holder: PageHolder, position: Int) {
            holder.bind(infoManager.getVersionInfo(position))
        }

        override fun getItemCount(): Int {
            return infoManager.infoCount
        }

    }

    private class PageHolder private constructor(view: View): RecyclerView.ViewHolder(view) {

        companion object {
            fun create(group: ViewGroup): PageHolder {
                return PageHolder(
                    LayoutInflater.from(group.context)
                        .inflate(R.layout.kit_fragment_update_info, group, false))
            }
        }

        private val infoTitle: TextView = itemView.findViewById(R.id.versionName)
        private val infoList: RecyclerView = itemView.findViewById(R.id.updateInfoGroup)
        private val infoItemAdapter = ItemAdapter()

        init {
            infoList.layoutManager = LinearLayoutManager(
                itemView.context, RecyclerView.VERTICAL, false)
            infoList.adapter = infoItemAdapter
        }

        fun bind(info: UpdateInfoManager.VersionInfo) {
            infoTitle.text = info.name
            infoItemAdapter.update(info.info)
        }

    }

    private class ItemAdapter: RecyclerView.Adapter<ItemHolder>() {

        private val infoList = ArrayList<String>()

        fun update(info: Array<String>) {
            infoList.clear()
            for (str in info) {
                infoList.add(str)
            }
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
            return ItemHolder.create(parent)
        }

        override fun onBindViewHolder(holder: ItemHolder, position: Int) {
            holder.bind(infoList[position])
        }

        override fun getItemCount(): Int {
            return infoList.size
        }

    }

    private class ItemHolder private constructor(view: View): RecyclerView.ViewHolder(view) {
        companion object {
            fun create(group: ViewGroup): ItemHolder {
                return ItemHolder(
                    LayoutInflater.from(group.context)
                        .inflate(R.layout.kit_item_update_info, group, false))
            }
        }

        private val infoView: TextView = itemView.findViewById(R.id.updateInfoView)

        fun bind(value: String) {
            infoView.text = value
        }

    }

}