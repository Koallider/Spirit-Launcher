package ru.ryakovlev.spiritlauncher.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.app_list_item.view.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import ru.ryakovlev.spiritlauncher.R
import ru.ryakovlev.spiritlauncher.domain.ApplicationInfo

/**
 * Created by roma on 28.05.2018.
 */
class ApplicationInfoAdapter(private val applications: List<ApplicationInfo>, private val clickListener: (ApplicationInfo) -> Unit)
    : RecyclerView.Adapter<ApplicationInfoAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.app_list_item, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = applications[position]
        holder.label.text = item.label
        holder.icon.setImageDrawable(item.icon)
        holder.container.onClick { clickListener(item) }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val label: TextView = view.label
        val icon: ImageView = view.icon
        val container: LinearLayout = view.container
    }

    override fun getItemCount() = applications.size
}