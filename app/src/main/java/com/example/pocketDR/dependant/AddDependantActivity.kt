package com.example.pocketDR.dependant

import com.example.pocketDR.DTO.DependantDTO
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.pocketDR.DTO.NotificationMedDTO
import com.example.pocketDR.MainActivity
import com.example.pocketDR.R
import com.example.pocketDR._LoginActivity
import com.example.pocketDR.databinding.ActivityDependantLoginBinding
import com.example.pocketDR.dependant.Response.*
import com.example.pocketDR.medicine.AddMedicineActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddDependantActivity : AppCompatActivity()  {

    lateinit var toogle: ActionBarDrawerToggle

    private companion object {
        private const val TAG = "DependantLoginActivity"
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var caretakerAcct : GoogleSignInAccount
    private lateinit var caretakerUser: FirebaseUser
    private lateinit var googleSignInClient : GoogleSignInClient
    private lateinit var dataBinding: ActivityDependantLoginBinding
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private val viewModel by viewModels<AuthViewModel>()

    private val real_time_db = FirebaseDatabase.getInstance()
    private val real_time_db_Reference = real_time_db.getReference()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = ActivityDependantLoginBinding.inflate(layoutInflater)
        setContentView(dataBinding.root)

        //nav Bar is disabled here to prevent someone from not completing the add dependant

        auth = Firebase.auth
        caretakerUser = auth.currentUser!!
        caretakerAcct =  GoogleSignIn.getLastSignedInAccount(this)!! //TODO: cannot be this, must be the original one

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("421019481551-8crg3ur6gomsc7sl508uja9427vrinen.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInClient.revokeAccess()
        auth.signOut()

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
                        Log.i(TAG,"Creating dependant...")
                        createDependant()
                    } else {
                        Log.i(TAG,"${auth.currentUser?.email} == ${caretakerUser.email}")
                        if(auth.currentUser?.email == caretakerUser.email){
                            //Log.i(TAG,"${auth.currentUser?.email} == ${caretakerUser.email}")
                            goToDepActivity()
                        }
                        else{
                            Log.i(TAG,"whaaa?")
                            signOutOfGoogle()
                            goToDepActivity()
                        }
                        //Log.i(TAG,"o dependant já existia")
                        //goToMainActivity()
                        dataBinding.progressBar.hide()
                    }
                }
                is Failure -> {
                    print(response.errorMessage)
                    dataBinding.progressBar.hide()
                }
            }
        })
    }

    private fun signOutOfGoogle() {
        viewModel.signOutOfGoogle().observe(this, { response ->
            when(response) {
                is Loading -> dataBinding.progressBar.show()
                is Success -> {
                    val currUser = response.data
                    if (currUser == null) {//previous user is signed out
                        caretakerAcct.apply {
                            idToken?.let { idToken ->
                                signInWithGoogle(idToken)
                            }
                        }

                    } else {
                        Log.i(TAG,"")
                        dataBinding.progressBar.hide()
                    }
                }
                is Failure -> {
                    print(response.errorMessage)
                    dataBinding.progressBar.hide()
                }
            }
        })
    }



    private fun createDependant() {
        viewModel.createDependant().observe(this, { response ->
            when(response) {
                is Loading -> dataBinding.progressBar.show()
                is Success -> {
                    Log.i(TAG,"Sucess viewModel.createDependant")
                    //create dependant on Real time Database
                    val newDependant = DependantDTO()
                    with(newDependant) {
                        if (auth.currentUser != null) {
                            dependantId = auth.currentUser!!.uid
                            name= auth.currentUser!!.displayName
                            email = auth.currentUser!!.email
                            caretakerId = caretakerUser.uid
                        }
                    }
                    if (auth.currentUser != null) {
                        //adds dependant to the dependant's table
                        real_time_db_Reference.child("dependants").child(auth.currentUser!!.uid).setValue(newDependant)
                        .addOnCompleteListener {
                            //adds dependant's id to the list of dependants of the caretaker
                            real_time_db_Reference.child("caretakers").child(caretakerUser.uid)
                                    .child("dependants").child(auth.currentUser!!.uid).setValue(auth.currentUser!!.displayName)
                            .addOnCompleteListener {
                                Log.i(TAG,"User created successfully")
                                dataBinding.progressBar.hide()
                                signOutOfGoogle()
                            }
                        }
                    }
                }
                is Failure -> {
                    print(response.errorMessage)
                    dataBinding.progressBar.hide()
                }
            }
        })
    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java)
            .putExtra("care_flag","true"))
        finish()
    }

    private fun goToDepActivity() {
        startActivity(Intent(this, DependantsActivity::class.java)
            .putExtra("care_flag","true"))
        finish()
    }


    /*fun navBar(navView: NavigationView) {
        navView.setNavigationItemSelectedListener {

            when (it.itemId) {
                R.id.nav_med -> startActivity(Intent(this, MainActivity::class.java))
                R.id.nav_addMed -> startActivity(Intent(this, AddMedicineActivity::class.java))
                R.id.nav_dependants -> startActivity(Intent(this, DependantsActivity::class.java))
                //R.id.nav_add_dependant -> Toast.makeText(this,"This is the Add dependant's screen" , Toast.LENGTH_SHORT).show()
                R.id.nav_curr_user -> Toast.makeText(this, auth.currentUser?.email, Toast.LENGTH_SHORT).show()
                R.id.nav_logout
                -> {
                    Log.i(MainActivity.TAG,"Logout")

                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken("421019481551-8crg3ur6gomsc7sl508uja9427vrinen.apps.googleusercontent.com")
                        .requestEmail()
                        .build()

                    val client = GoogleSignIn.getClient(this, gso)

                    client.revokeAccess()
                    auth.signOut()

                    val logoutIntent = Intent(this, _LoginActivity::class.java)
                    logoutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(logoutIntent)
                }
            }
            true
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toogle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }*/
}