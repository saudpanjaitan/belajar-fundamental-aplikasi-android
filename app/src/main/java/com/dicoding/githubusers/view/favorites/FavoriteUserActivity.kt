package com.dicoding.githubusers.view.favorites

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.githubusers.R
import com.dicoding.githubusers.databinding.ActivityFavoriteUserBinding
import com.dicoding.githubusers.view.ViewModelFactory

class FavoriteUserActivity : AppCompatActivity() {

    private var _binding: ActivityFavoriteUserBinding? = null
    private val binding get() = _binding
    private lateinit var adapter: FavoriteUserAdapter

    private lateinit var favoriteUserViewModel: FavoriteUserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityFavoriteUserBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        supportActionBar?.title = getString(R.string.favoriteduser)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        favoriteUserViewModel = obtainViewModel(this@FavoriteUserActivity)
        favoriteUserViewModel.getAllFavorites().observe(this) { favoriteList ->
            if (favoriteList != null) {
                adapter.setFavorites(favoriteList)
            }
        }
        adapter = FavoriteUserAdapter()
        binding?.rvFavorites?.layoutManager = LinearLayoutManager(this)
        binding?.rvFavorites?.setHasFixedSize(false)
        binding?.rvFavorites?.adapter = adapter
    }

    private fun obtainViewModel(activity: AppCompatActivity): FavoriteUserViewModel {
        val factory = ViewModelFactory.getInstance(activity.application)
        return ViewModelProvider(activity, factory)[FavoriteUserViewModel::class.java]
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}