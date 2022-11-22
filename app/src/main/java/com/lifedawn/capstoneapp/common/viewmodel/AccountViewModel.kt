package com.lifedawn.capstoneapp.common.viewmodel;

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthResult
import com.lifedawn.capstoneapp.common.repository.AccountRepositoryImpl
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountViewModel : ViewModel() {
    val signInResult = MutableLiveData<AuthResult?>()
    val signUpResult = MutableLiveData<AuthResult?>()
    val signOutResult = MutableLiveData<Boolean>()

    fun signIn(email: String, pw: String) {
        viewModelScope.launch {
            val result = async { AccountRepositoryImpl.signIn(email, pw) }
            result.await()

            withContext(Main) {
                signOutResult.value = false
                signInResult.value = result.await()
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            AccountRepositoryImpl.signOut()
            withContext(Main) {
                signOutResult.value = true
                signInResult.value = null
            }
        }
    }

    fun signUp(map: Map<String, String>) {
        viewModelScope.launch {
            val result = async { AccountRepositoryImpl.signUp(map) }
            result.await()

            withContext(Main) {
                signUpResult.value = result.await()
            }
        }
    }
}
