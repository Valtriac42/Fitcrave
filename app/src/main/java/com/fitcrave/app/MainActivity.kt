package com.fitcrave.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.fitcrave.app.databinding.ActivityMainBinding
import com.fitcrave.app.fragments.ProfileFragment
import com.fitcrave.app.fragments.SettingsFragment
import com.fitcrave.app.fragments.TrainingFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_training -> replace(TrainingFragment())
                R.id.nav_settings -> replace(SettingsFragment())
                R.id.nav_profile  -> replace(ProfileFragment())
            }
            true
        }
        if (savedInstanceState == null) {
            binding.bottomNav.selectedItemId = R.id.nav_training
        }
    }

    private fun replace(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
