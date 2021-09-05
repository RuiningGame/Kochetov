package com.kochet.devlife

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kochet.devlife.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val lisOfContext: List<JokeSectionContext> = mutableListOf(
        JokeSectionContext("Top", "top"),
        JokeSectionContext("Latest", "latest"),
        JokeSectionContext("Hot", "hot"),
        JokeSectionContext("Random", "random", false)
    )

    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.viewpager.isUserInputEnabled = false

        binding.tabs.tabGravity = TabLayout.GRAVITY_FILL
        binding.viewpager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): JokeReviewFragment {
                return JokeReviewFragment(lisOfContext[position])
            }

            override fun getItemCount(): Int {
                return lisOfContext.count()
            }
        }

        TabLayoutMediator(binding.tabs, binding.viewpager) { tab, position ->
            tab.text = lisOfContext[position].title
        }.attach()
    }
}