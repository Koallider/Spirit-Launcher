package ru.ryakovlev.spiritlauncher.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.app_list_item.view.*
import ru.ryakovlev.spiritlauncher.R
import ru.ryakovlev.spiritlauncher.domain.ApplicationInfo
import ru.ryakovlev.spiritlauncher.util.TouchListener


/**
 * Created by roma on 28.05.2018.
 */
class ApplicationInfoAdapter(var applications: MutableList<ApplicationInfo>)
    : RecyclerView.Adapter<ApplicationInfoAdapter.ViewHolder>() {

    var clickListener: ((position: Int) -> Unit)? = null
    var longClickListener: ((position: Int) -> Unit)? = null
    var moveListener: ((position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.app_list_item, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = applications[position]
        holder.itemView.setTag(R.id.package_tag, applications[position].packageName)
        holder.label.text = item.label
        holder.icon.setImageDrawable(item.icon)
        val touchListener = TouchListener(position)
        touchListener.clickListener = clickListener
        touchListener.longClickListener = longClickListener
        touchListener.moveListener = moveListener
        holder.container.setOnTouchListener(touchListener)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val label: TextView = view.label
        val icon: ImageView = view.icon
        val container: LinearLayout = view.container
    }

    override fun getItemCount() = applications.size


}