package com.example.pocketDR

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.pocketDR.DTO.CaretakerDTO
import com.example.pocketDR.DTO.DependantDTO
import com.example.pocketDR.databinding.ActivityDependantLoginBinding
import com.example.pocketDR.dependant.AuthViewModel
import com.example.pocketDR.dependant.Response.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class _LoginActivity : AppCompatActivity()  {

    private companion object {
        private const val TAG = "_LoginActivity"
    }

    lateinit var owner_caretaker : CaretakerDTO
    lateinit var owner_dependant : DependantDTO
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient : GoogleSignInClient
    private lateinit var dataBinding: ActivityDependantLoginBinding
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private val viewModel by viewModels<AuthViewModel>()

    private val db = Firebase.firestore
    lateinit private var real_time_db : FirebaseDatabase
    lateinit private var real_time_db_Reference : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = ActivityDependantLoginBinding.inflate(layoutInflater)
        setContentView(dataBinding.root)

        //Firebase.database.setPersistenceEnabled(true)
        real_time_db = FirebaseDatabase.getInstance()
        real_time_db_Reference = real_time_db.getReference()

        auth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("421019481551-8crg3ur6gomsc7sl508uja9427vrinen.apps.googleusercontent.com") //
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        resultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = getSignedInAccountFromIntent(result.data)
                try {
                    val googleSignInAccount = task.getResult(ApiException::class.java)
                    googleSignInAccount?.apply {
                        idToken?.let { idToken ->
                            signInWithGoogle(idToken)
                        }
                    }
                } catch (e: ApiException) {
                    print(e.message)
                }
            }
        }
    }

    fun openLauncher(view: View) {
        if (view.id == R.id.dep_sign_in_button) {
            val signInIntent = googleSignInClient.signInIntent
            resultLauncher.launch(signInIntent)
        }
    }

    private fun signInWithGoogle(idToken: String) {
        viewModel.signInWithGoogle(idToken).observe(this, { response ->
            when(response) {
                is Loading -> dataBinding.progressBar.show()
                is Success -> {
                    val isNewUser = response.data
                    if (isNewUser) {
                        //owner_caretaker.ownerAcctIdToken=idToken
                        createCaretaker()
                    } else {
                        //if it is not a new user, it is a dependant
                        val query_result= auth.currentUser?.let { db.collection("users").document(it.uid) }
                        //according to the current's user data, it is possible to check if it is a dependant or a caretaker
                        query_result?.addSnapshotListener { snapshot, e ->
                            if (e != null) {
                                Log.w(TAG, "Listen failed.", e)
                                return@addSnapshotListener
                            }

                            //check if there are any pending writes on the database
                            val source = (snapshot != null && snapshot.metadata.hasPendingWrites())

                            //after checking if there are no pending write, the next activity is opened accordingly
                            if (snapshot != null && snapshot.exists()) {
                                Log.d(TAG, "$source data: ${snapshot.data}")
                                if(snapshot.getField<Boolean>("care_flag")==true) {
                                    Toast.makeText(this,"It is a caretaker" , Toast.LENGTH_SHORT).show()
                                    //goes to caretaker activity
                                    goToMainActivity(true)
                                }else{
                                    //goes to dependant activity
                                    Toast.makeText(this,"It is a dependant" , Toast.LENGTH_SHORT).show()
                                    //save owner's info
                                    owner_dependant = DependantDTO()
                                    with(owner_dependant) {
                                        dependantId = auth.currentUser?.uid
                                        name = auth.currentUser!!.displayName
                                        email = auth.currentUser!!.email
                                        caretakerId = snapshot.getField<String>("caretakerID")
                                    }

                                    //ownerAcct = GoogleSignIn.getLastSignedInAccount(this)!!
                                    print(owner_dependant.email)
                                    goToMainActivity(false)
                                }
                            }else {
                                Log.d(TAG, "$source data: null")
                            }
                        }
                        dataBinding.progressBar.hide()
                    }
                }
                is Failure -> {
                    print(response.errorMessage)
                    Toast.makeText(this,"Authentication Failed", Toast.LENGTH_SHORT).show()
                    dataBinding.progressBar.hide()
                }
            }
        })
    }

    private fun createCaretaker() {
        viewModel.createCaretaker().observe(this, { response ->
            when(response) {
                is Loading -> dataBinding.progressBar.show()
                is Success -> {
                    if (auth.currentUser != null) {
                        owner_caretaker = CaretakerDTO()
                        with(owner_caretaker) {
                            name = auth.currentUser!!.displayName
                            email = auth.currentUser!!.email
                        }
                        //save to the real time database
                        real_time_db_Reference.child("caretakers").child(auth.currentUser!!.uid).setValue(owner_caretaker)

                        owner_caretaker.caretakerId = auth.currentUser!!.uid
                        //owner_caretaker.ownerGoogleSignInClient =googleSignInClient
                    }

                    goToMainActivity(true)
                    Log.i(TAG,"User created successfully")
                    dataBinding.progressBar.hide()
                }
                is Failure -> {
                    print(response.errorMessage)
                    Toast.makeText(this,"Authentication Failed", Toast.LENGTH_SHORT).show()
                    dataBinding.progressBar.hide()
                }
            }
        })
    }


    private fun goToMainActivity(careFlag : Boolean) { //goToCaretakerMainActivity(){
        startActivity(Intent(this, MainActivity::class.java).putExtra("care_flag",careFlag.toString()))
        finish()
    }

    /*private fun goToDependantMainActivity()
        startActivity(Intent(this, DependantMainActivity::class.java).putExtra("owner",owner_caretaker))
        finish()
    }*/

}