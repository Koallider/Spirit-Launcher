package ru.ryakovlev.spiritlauncher.adapter

import android.annotation.SuppressLint
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.shortcut_layout.view.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import ru.ryakovlev.spiritlauncher.R
import ru.ryakovlev.spiritlauncher.domain.Shortcut

/**
 * Created by roma on 28.05.2018.
 */
class ShortcutAdapter(val shortcutList: List<Shortcut>, private val clickListener: (shortcut: Shortcut) -> Unit)
    : RecyclerView.Adapter<ShortcutAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.shortcut_layout, parent, false))

    @SuppressLint("NewApi")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = shortcutList[position]
        holder.label.text = item.shortcutInfo.shortLabel
        holder.icon.setImageDrawable(item.drawable)
        holder.container.onClick { clickListener(item) }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val label: TextView = view.label
        val icon: ImageView = view.image
        val container: CardView = view.shortcutContainer
    }

    override fun getItemCount() = shortcutList.size
}