package com.lifedawn.capstoneapp.common.repository

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.lifedawn.capstoneapp.common.repositoryinterface.AccountRepository
import kotlin.coroutines.suspendCoroutine

object AccountRepositoryImpl : AccountRepository {
    override suspend fun signIn(email: String, pw: String) = suspendCoroutine<AuthResult?> { continuation ->
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pw).addOnCompleteListener {
            if (it.isSuccessful)
                continuation.resumeWith(Result.success(it.result))
            else
                continuation.resumeWith(Result.failure(it.exception!!))
        }
    }

    override suspend fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    override suspend fun signUp(map: Map<String, String>) = suspendCoroutine<AuthResult?>
    { continuation ->
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(map["email"]!!, map["pw"]!!)
                .addOnCompleteListener {
                    if (it.isSuccessful)
                        continuation.resumeWith(Result.success(it.result))
                    else
                        continuation.resumeWith(Result.failure(it.exception!!))
                }
    }

}