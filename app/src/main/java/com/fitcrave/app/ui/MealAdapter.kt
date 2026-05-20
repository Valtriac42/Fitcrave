package com.fitcrave.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fitcrave.app.data.models.Meal
import com.fitcrave.app.databinding.ItemMealBinding

class MealAdapter(
    private val items: List<Meal>
) : RecyclerView.Adapter<MealAdapter.VH>() {

    inner class VH(val b: ItemMealBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemMealBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val m = items[position]
        holder.b.tvMealName.text = m.name
        holder.b.tvMealItems.text = m.items
        holder.b.tvMealKcal.text = "${m.kcal} KCAL"
    }

    override fun getItemCount(): Int = items.size
}
