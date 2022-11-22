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

    fun signIn(email: String, pw: String) {
        viewModelScope.launch {
            val result = async { AccountRepositoryImpl.signIn(email, pw) }
            result.await()

            withContext(Main) {
                signInResult.value = result.await()
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            AccountRepositoryImpl.signOut()
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
