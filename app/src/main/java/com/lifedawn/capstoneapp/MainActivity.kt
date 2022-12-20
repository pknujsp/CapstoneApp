package com.lifedawn.capstoneapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil.setContentView
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.lifedawn.capstoneapp.common.constants.SharedPreferenceConstant;
import com.lifedawn.capstoneapp.databinding.ActivityMainBinding;
import com.lifedawn.capstoneapp.main.MainTransactionFragment;
import com.lifedawn.capstoneapp.view.account.SignInFragment;

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (!supportFragmentManager.popBackStackImmediate()) {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(onBackPressedCallback)
        init()
    }

    private fun init() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val appInit = sharedPreferences.getBoolean(SharedPreferenceConstant.APP_INIT.name, false)

        val fragmentTransaction = supportFragmentManager.beginTransaction()

        if (appInit) {
            val mainTransactionFragment = MainTransactionFragment()
            fragmentTransaction.add(
                binding.fragmentContainerView.id, mainTransactionFragment,
                MainTransactionFragment.TAG
            ).setPrimaryNavigationFragment(mainTransactionFragment)
                .commit()
        } else {
            //intro
            
            val signInFragment = SignInFragment()
            fragmentTransaction.add(binding.fragmentContainerView.id, signInFragment, SignInFragment.TAG)
                .setPrimaryNavigationFragment(signInFragment).commit()
        }
    }

}