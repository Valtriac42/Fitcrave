package com.fitcrave.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.fitcrave.app.MainActivity
import com.fitcrave.app.R
import com.fitcrave.app.databinding.ActivityOnboardingBinding
import com.fitcrave.app.databinding.ItemOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private data class Page(val title: Int, val body: Int, val image: Int)

    private val pages = listOf(
        Page(R.string.onb_title_1, R.string.onb_body_1, R.drawable.ic_bodybuilder),
        Page(R.string.onb_title_2, R.string.onb_body_2, R.drawable.ic_dumbbell),
        Page(R.string.onb_title_3, R.string.onb_body_3, R.drawable.ic_report_hero)
    )

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.pager.adapter = OnbAdapter(pages)
        renderDots(0)

        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                renderDots(position)
                binding.btnNext.text =
                    if (position == pages.lastIndex) getString(R.string.onb_start)
                    else getString(R.string.onb_next)
                binding.btnSkip.visibility =
                    if (position == pages.lastIndex) View.INVISIBLE else View.VISIBLE
            }
        })

        binding.btnSkip.setOnClickListener { finishOnboarding() }
        binding.btnNext.setOnClickListener {
            val cur = binding.pager.currentItem
            if (cur < pages.lastIndex) binding.pager.currentItem = cur + 1
            else finishOnboarding()
        }
    }

    private fun renderDots(active: Int) {
        binding.dots.removeAllViews()
        val padding = (resources.displayMetrics.density * 4).toInt()
        repeat(pages.size) { i ->
            val v = ImageView(this)
            v.setImageResource(if (i == active) R.drawable.dot_active else R.drawable.dot_inactive)
            val lp = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(padding, 0, padding, 0)
            binding.dots.addView(v, lp)
        }
    }

    private fun finishOnboarding() {
        markCompleted(this)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private class OnbAdapter(private val items: List<Page>) :
        RecyclerView.Adapter<OnbAdapter.VH>() {
        class VH(val b: ItemOnboardingBinding) : RecyclerView.ViewHolder(b.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val b = ItemOnboardingBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return VH(b)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val p = items[position]
            holder.b.imgOnb.setImageResource(p.image)
            holder.b.tvOnbTitle.setText(p.title)
            holder.b.tvOnbBody.setText(p.body)
        }

        override fun getItemCount(): Int = items.size
    }

    companion object {
        private const val PREFS = "fitcrave_prefs"
        private const val KEY_COMPLETED = "onboarding_completed"

        fun isCompleted(ctx: Context): Boolean =
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_COMPLETED, false)

        fun markCompleted(ctx: Context) {
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_COMPLETED, true).apply()
        }
    }
}
