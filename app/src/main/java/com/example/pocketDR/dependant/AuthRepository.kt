package com.example.pocketDR.dependant

import com.example.pocketDR.dependant.Response.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    internal val auth: FirebaseAuth,
    private val usersRef: CollectionReference
) {
    suspend fun firebaseSignInWithGoogle(idToken: String) = flow {
        try {
            emit(Loading)
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            authResult.additionalUserInfo?.apply {
                emit(Success(isNewUser))
            }
        } catch (e: Exception) {
            emit(Failure(e.message ?: "Unexpected error!"))
        }
    }

    suspend fun firebaseSignOutOfGoogle() = flow {
        try {
            emit(Loading)
            auth.signOut()
            if(auth.currentUser == null) //signout was successful
                emit(Success(auth.currentUser))

        } catch (e: Exception) {
            emit(Failure(e.message ?: "Unexpected error!"))
        }
    }

    suspend fun createDependantInFirestore() = flow {
        try {
            emit(Loading)
            auth.currentUser?.apply {
                usersRef.document(uid).set(
                    mapOf(
                        "name" to displayName,
                        "email" to email,
                        "care_flag" to false
                    )
                ).await().also {
                    emit(Success(it))
                }
            }
        } catch (e: Exception) {
            emit(Failure(e.message ?: "Unexpected error!"))
        }
    }

    suspend fun createCaretakerInFirestore() = flow {
        try {
            emit(Loading)
            auth.currentUser?.apply {
                usersRef.document(uid).set(
                    mapOf(
                        "name" to displayName,
                        "email" to email,
                        "care_flag" to true
                    )
                ).await().also {
                    emit(Success(it))
                }
            }
        } catch (e: Exception) {
            emit(Failure(e.message ?: "Unexpected error!"))
        }
    }
}

