package com.lifedawn.capstoneapp.common.repositoryinterface

import com.google.firebase.auth.AuthResult

interface AccountRepository {
    suspend fun signIn(email: String, pw: String): AuthResult?
    suspend fun signOut()
    suspend fun signUp(map: Map<String, String>): AuthResult?
}