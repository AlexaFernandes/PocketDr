package com.example.pocketDR.medicine

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pocketDR.*
import com.example.pocketDR.dependant.AddDependantActivity
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

import com.example.pocketDR.DTO.DependantDTO
import com.example.pocketDR.DTO.NotificationMedDTO
import com.example.pocketDR.R
import com.example.pocketDR.dependant.DependantsActivity
import com.example.pocketDR.recyclerview.DependantsAdapter
import com.example.pocketDR.recyclerview.NotifMedAdapter
import com.example.pocketDR.recyclerview.NotifMedDontRemoveAdapter
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList

internal class DisplayDepMedicineActivity : AppCompatActivity() {

    lateinit var toogle: ActionBarDrawerToggle

    private companion object { //just for some logs
        const val TAG = "DisplayDepMedActivity"
    }

    private lateinit var auth: FirebaseAuth

    private lateinit var notificationMedArrayList: ArrayList<NotificationMedDTO>

    // Access a Cloud Firestore instance from your Activity
    val db = Firebase.firestore
    private var real_time_db_Reference = FirebaseDatabase.getInstance().getReference()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dep_uid = intent.getStringExtra("dep_uid")

        setContentView(R.layout.activity_dependant)
        val title : TextView = findViewById(R.id.title)
        title.setText("Dependant's Meds")


        val myFab: FloatingActionButton = findViewById(R.id.fab);
        myFab.setOnClickListener { view ->
            Snackbar.make(view, "Add a new med", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show()

            startActivity(Intent(this, AddMedicineActivity::class.java)
                .putExtra("dep_uid",dep_uid)
                .putExtra("care_flag","false"))
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
        notificationMedArrayList = arrayListOf<NotificationMedDTO>()

        if (dep_uid != null) {
            getUserData(dep_uid)
        }
        else
            Log.i(TAG, "o dep_uid veio a NULL")

    }

    fun navBar(navView: NavigationView) {
        navView.menu.getItem(1).isVisible=false //add medicine was added to the button
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_med -> {
                    startActivity(
                        Intent(this, MainActivity::class.java)
                            .putExtra("care_flag", "true")
                    )
                    finish()
                }
                R.id.nav_dependants -> {
                    startActivity(
                        Intent(this, DependantsActivity::class.java)
                            .putExtra("care_flag", "true")
                    )
                    finish()
                }
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toogle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getUserData(dep_uid:String) {

        real_time_db_Reference = FirebaseDatabase.getInstance().getReference().child("dependants").child(dep_uid).child("medicines")

        real_time_db_Reference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                notificationMedArrayList.clear()
                if (snapshot.exists()){
                    for (userSnapshot in snapshot.children){
                        val notifMed = userSnapshot.getValue(NotificationMedDTO::class.java)
                        if (notifMed != null) {
                                notificationMedArrayList.add(notifMed)
                        }
                    }
                    rv_dependants.adapter = NotifMedDontRemoveAdapter(notificationMedArrayList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }
}