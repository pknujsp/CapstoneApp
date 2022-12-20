package com.lifedawn.capstoneapp.ui.screens.main;

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.lifedawn.capstoneapp.databinding.ActivityMainBinding
import com.lifedawn.capstoneapp.ui.screens.account.SignInFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (!supportFragmentManager.popBackStackImmediate()) {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(onBackPressedCallback)
        init()
    }

    private fun init() {
        //intro
        val signInFragment = SignInFragment()
        supportFragmentManager.beginTransaction().add(binding.fragmentContainerView.id, signInFragment, SignInFragment.TAG)
            .setPrimaryNavigationFragment(signInFragment).commit()
    }
}