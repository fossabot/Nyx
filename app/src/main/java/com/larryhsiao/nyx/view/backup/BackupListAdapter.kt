package com.larryhsiao.nyx.view.backup

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.larryhsiao.nyx.backup.Backup
import com.silverhetch.aura.view.ViewHolder

/**
 * Adapter for list of backup list.
 */
class BackupListAdapter : RecyclerView.Adapter<ViewHolder>() {
    private val items = ArrayList<Backup>()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                android.R.layout.simple_list_item_1,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        (holder.rootView as TextView).text = items[position].title()
    }

    fun loadUp(new: List<Backup>) {
        items.clear()
        items.addAll(new)
        notifyDataSetChanged()
    }
}