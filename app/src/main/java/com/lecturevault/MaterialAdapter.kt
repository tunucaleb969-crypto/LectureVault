package com.lecturevault

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lecturevault.data.Material

class MaterialAdapter(private var items: List<Material>) :
    RecyclerView.Adapter<MaterialAdapter.ViewHolder>() {

    class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.materialText)
        val type: TextView = itemView.findViewById(R.id.materialType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_material, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.type.text = item.type.uppercase()
        holder.text.text = if (item.rawText.length > 120)
            item.rawText.take(120) + "…" else item.rawText
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<Material>) {
        items = newItems
        notifyDataSetChanged()
    }
    }
