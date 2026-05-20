package com.fitcrave.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fitcrave.app.data.models.Exercise
import com.fitcrave.app.databinding.ItemExerciseBinding

class ExerciseAdapter(
    private val items: List<Exercise>
) : RecyclerView.Adapter<ExerciseAdapter.VH>() {

    inner class VH(val b: ItemExerciseBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemExerciseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val e = items[position]
        holder.b.tvExerciseName.text = e.name
        val unit = if (e.name.contains("Plank", ignoreCase = true)) "sec" else "reps"
        holder.b.tvExerciseSets.text = "${e.sets} sets × ${e.reps} $unit"
    }

    override fun getItemCount(): Int = items.size
}
