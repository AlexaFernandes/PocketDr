package com.example.pocketDR.dependant

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pocketDR.*
import com.example.pocketDR.medicine.AddMedicineActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_dependant.*
import kotlinx.android.synthetic.main.activity_main.*

import com.example.pocketDR.DTO.DependantDTO
import com.example.pocketDR.R
import com.example.pocketDR.medicine.DisplayDepMedicineActivity
import com.example.pocketDR.recyclerview.DependantsAdapter
import com.example.pocketDR.recyclerview.NotifMedAdapter
import com.google.firebase.database.*
import java.util.ArrayList
import kotlin.properties.Delegates

internal class DependantsActivity : AppCompatActivity() {

    lateinit var toogle: ActionBarDrawerToggle

    private companion object { //just for some logs
        public const val TAG = "DependantActivity"
    }

    private var careFlag by Delegates.notNull<Boolean>()
    private lateinit var auth: FirebaseAuth
    lateinit private var real_time_db: FirebaseDatabase
    lateinit private var real_time_db_Reference: DatabaseReference

    private lateinit var DependantsArrayList: ArrayList<DependantFB>

    // Access a Cloud Firestore instance from your Activity
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        careFlag = intent.getStringExtra("care_flag").toBoolean()
        Log.i(MainActivity.TAG, "careFlag : $careFlag")

        setContentView(R.layout.activity_dependant)
        val title : TextView = findViewById(R.id.title)
        title.setText("Dependants")

        val myFab: FloatingActionButton = findViewById(R.id.fab);
        myFab.setOnClickListener { view ->
            Snackbar.make(view, "Add a new dependant", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show()

            startActivity(Intent(this, AddDependantActivity::class.java))
        }

        val drawer: DrawerLayout = findViewById(R.id.drawer)
        toogle = ActionBarDrawerToggle(this, drawer, R.string.open, R.string.close)
        drawer.addDrawerListener(toogle)
        toogle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val navView: NavigationView = findViewById(R.id.navView)

        navBar(navView)

        auth = Firebase.auth //connect to the authentication server

        rv_dependants.layoutManager = LinearLayoutManager(this)
        rv_dependants.setHasFixedSize(true)

        DependantsArrayList = arrayListOf<DependantFB>()
        getUserData()

    }

    fun navBar(navView: NavigationView) {
        navView.menu.getItem(1).isVisible=false //add medicine was added to the button
        if(careFlag) { //set definitions for Caretaker
            navView.setNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.nav_med -> {
                        startActivity(
                            Intent(this, MainActivity::class.java)
                                .putExtra("care_flag", "true")
                        )
                        finish()
                    }
                    R.id.nav_dependants -> Toast.makeText(
                        this,
                        "This is the Dependant's screen",
                        Toast.LENGTH_SHORT
                    ).show()
                    R.id.nav_curr_user -> Toast.makeText(
                        this,
                        auth.currentUser?.email,
                        Toast.LENGTH_SHORT
                    ).show()
                    R.id.nav_logout
                    -> {
                        Log.i(MainActivity.TAG, "Logout")

                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken("421019481551-8crg3ur6gomsc7sl508uja9427vrinen.apps.googleusercontent.com")
                            .requestEmail()
                            .build()

                        val client = GoogleSignIn.getClient(this, gso)

                        client.revokeAccess()
                        auth.signOut()

                        val logoutIntent = Intent(this, _LoginActivity::class.java)
                        logoutIntent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(logoutIntent)
                    }
                }
                true
            }
        }
        else{ //never going in here bc only caretakers can access this activity!
            navView.menu.getItem(2).isVisible=false
            navView.setNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.nav_med -> startActivity(Intent(this, MainActivity::class.java))
                    //R.id.nav_add_dependant -> startActivity(Intent(this, AddDependantActivity::class.java))
                    R.id.nav_curr_user -> Toast.makeText(
                        this,
                        auth.currentUser?.email,
                        Toast.LENGTH_SHORT
                    ).show()
                    R.id.nav_logout
                    -> {
                        Log.i(MainActivity.TAG, "Logout")

                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken("421019481551-8crg3ur6gomsc7sl508uja9427vrinen.apps.googleusercontent.com")
                            .requestEmail()
                            .build()

                        val client = GoogleSignIn.getClient(this, gso)

                        client.revokeAccess()
                        auth.signOut()

                        val logoutIntent = Intent(this, _LoginActivity::class.java)
                        logoutIntent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(logoutIntent)
                    }
                }
                true
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toogle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getUserData() {
        val dependant = DependantFB()
        real_time_db = FirebaseDatabase.getInstance()
        real_time_db_Reference = real_time_db.getReference()
        auth.currentUser?.let {
            real_time_db_Reference.child("caretakers").child(it.uid).child("dependants")
                .addValueEventListener(object : ValueEventListener {

                    @SuppressLint("NotifyDataSetChanged")
                    override fun onDataChange(snapshot: DataSnapshot) {
                        DependantsArrayList.clear()
                        if (snapshot.exists()) {
                            for (userSnapshot in snapshot.children) {
                                dependant.name = userSnapshot.value as String?
                                dependant.uid = userSnapshot.key

                                DependantsArrayList.add(dependant)
                            }
                            rv_dependants.adapter = DependantsAdapter(DependantsArrayList)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        }

    }
}