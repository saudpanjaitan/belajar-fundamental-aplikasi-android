@file:Suppress("SameParameterValue")

package com.dicoding.githubusers.view.main

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.githubusers.R
import com.dicoding.githubusers.databinding.ActivityMainBinding
import com.dicoding.githubusers.model.response.GithubUser
import com.dicoding.githubusers.utils.Helper
import com.dicoding.githubusers.view.settings.ThemeSettingsActivity
import com.dicoding.githubusers.view.details.UserDetailActivity
import com.dicoding.githubusers.view.favorites.FavoriteUserActivity
import com.dicoding.githubusers.view.settings.SettingsPreferences
import com.dicoding.githubusers.view.settings.SettingsViewModelFactory
import kotlin.collections.ArrayList

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
private lateinit var mainViewModel: MainViewModel

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding

    private var listGithubUser = ArrayList<GithubUser>()
    private val helper = Helper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_GithubUsers)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val pref = SettingsPreferences.getInstance(dataStore)
        mainViewModel = ViewModelProvider(this, SettingsViewModelFactory(pref))[MainViewModel::class.java]

        mainViewModel.listGithubUser.observe(this) { listGithubUser ->
            setUserData(listGithubUser)
        }
        mainViewModel.isLoading.observe(this) {
            helper.showLoading(it, binding!!.progressBar)
        }

        val layoutManager = LinearLayoutManager(this@MainActivity)
        binding?.rvUser?.layoutManager = layoutManager

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        binding?.searchView?.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchUsername()

        mainViewModel.searchGithubUser(randomStartingList(2))

        mainViewModel.getThemeSettings().observe(this) { isLightModeActive: Boolean ->
            if (isLightModeActive) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun randomStartingList(length: Int): String {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz"
        return (1..length)
            .map { alphabet.random() }
            .joinToString("")
    }

    private fun searchUsername() {
        binding?.searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    binding?.rvUser?.visibility = View.VISIBLE
                    mainViewModel.searchGithubUser(it)
                    setUserData(listGithubUser)
                }
                hideKeyboard()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.theme_setting -> {
                val intent = Intent(this@MainActivity, ThemeSettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.favorites -> {
                val intent = Intent(this@MainActivity, FavoriteUserActivity::class.java)
                startActivity(intent)
                true
            }
            else -> true
        }
    }

    private fun setUserData(listGithubUser: List<GithubUser>) {
        val listUser = ArrayList<GithubUser>()
        for (user in listGithubUser) {
            listUser.clear()
            listUser.addAll(listGithubUser)
        }
        val adapter = SearchAdapter(listUser)
        binding?.rvUser?.adapter = adapter

        adapter.setOnItemClickCallback(object : SearchAdapter.OnItemClickCallback {
            override fun onItemClicked(data: GithubUser) {
                showSelectedUser(data)
            }
        })
    }

    private fun showSelectedUser(data: GithubUser) {
        val intent = Intent(this@MainActivity, UserDetailActivity::class.java)
        intent.putExtra(UserDetailActivity.EXTRA_USER, data.login)
        startActivity(intent)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding?.rvUser?.windowToken, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}