package com.example.pocketDR

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketDR.dependant.DependantsActivity
import com.example.pocketDR.medicine.AddMedicineActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import android.annotation.SuppressLint
import androidx.core.view.isVisible
import com.example.pocketDR.DTO.NotificationMedDTO
import com.example.pocketDR.recyclerview.NotifMedDepAdapter
import com.example.pocketDR.recyclerview.NotifMedAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_dependant.*
import kotlin.properties.Delegates


class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class MainActivity : AppCompatActivity() {

    internal companion object {
        const val TAG = "MainActivity"
    }
    //private var owner_caretaker : CaretakerDTO? = intent.getParcelableExtra<CaretakerDTO>("owner")

    lateinit var toogle: ActionBarDrawerToggle
    private val mNotificationTime =
        Calendar.getInstance().timeInMillis + 5000 //Set after 5 seconds from the current time.
    private var mNotified = false

    private var careFlag by Delegates.notNull<Boolean>()
    private lateinit var auth: FirebaseAuth
    // Access a Cloud Firestore instance from your Activity
    private val db = Firebase.firestore
    private lateinit var real_time_db : FirebaseDatabase
    private lateinit var real_time_db_Reference : DatabaseReference

    private lateinit var notificationMedArrayList : ArrayList<NotificationMedDTO>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        careFlag = intent.getStringExtra("care_flag").toBoolean()
        Log.i(TAG, "careFlag : $careFlag")

        setContentView(R.layout.activity_dependant)
        val title : TextView = findViewById(R.id.title)
        title.setText("Personal Meds")


        val myFab: FloatingActionButton = findViewById(R.id.fab);
        if(!careFlag)
            myFab.isVisible=false

        myFab.setOnClickListener { view ->
            Snackbar.make(view, "Add a new medicine", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show()

            startActivity(Intent(this, AddMedicineActivity::class.java)
                .putExtra("dep_uid","")
                .putExtra("care_flag","true"))
        }

        val drawer: DrawerLayout = findViewById(R.id.drawer)
        toogle = ActionBarDrawerToggle(this, drawer, R.string.open, R.string.close)
        drawer.addDrawerListener(toogle)
        toogle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val navView: NavigationView = findViewById(R.id.navView)

        navBar(navView)

        AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                //display the current user email
                val tvCurrUserEmail: TextView = drawer.findViewById(R.id.tv_curr_user_mail)
                tvCurrUserEmail.text = auth.currentUser?.email
            }
        }

        auth = Firebase.auth //connect to the authentication server

        rv_dependants.layoutManager = LinearLayoutManager(this)
        rv_dependants.setHasFixedSize(true)

        notificationMedArrayList = arrayListOf<NotificationMedDTO>()
        getUserData()

    }

    fun navBar(navView: NavigationView) {
            navView.menu.getItem(1).isVisible=false //add medicine was added to the button
            if(careFlag){ //set definitions for Caretaker
                navView.setNavigationItemSelectedListener {
                    when (it.itemId) {
                        R.id.nav_med -> Toast.makeText(this, "This is the Medicine page", Toast.LENGTH_SHORT).show()
                        R.id.nav_dependants -> {startActivity(Intent(this, DependantsActivity::class.java)
                            .putExtra("care_flag",careFlag.toString()))
                            finish()
                        }
                        R.id.nav_curr_user -> Toast.makeText(this, auth.currentUser?.email, Toast.LENGTH_SHORT).show()
                        R.id.nav_logout
                        -> {
                            Log.i(TAG, "Logout")

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
                            finish()
                        }
                    }
                    true
                }
            }
            else{ //set definitions for Dependant
                navView.menu.getItem(2).isVisible=false
                navView.setNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.nav_med -> Toast.makeText(this, "This is the Medicine page", Toast.LENGTH_SHORT).show()
                    R.id.nav_curr_user -> Toast.makeText(this, auth.currentUser?.email, Toast.LENGTH_SHORT).show()
                    R.id.nav_logout
                    -> {
                        Log.i(TAG, "Logout")

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
                        finish()
                    }
                }
                true
            }
        }
    }

    private fun getUserData() {
        lateinit var path : String
        real_time_db = FirebaseDatabase.getInstance()
        //real_time_db_Reference = real_time_db.getReference()
        //auth.currentUser?.let {
            if(careFlag)
                path = "caretakers/${auth.currentUser?.uid}/medicines"
            else
                path = "dependants/${auth.currentUser?.uid}/medicines"

            Log.i(TAG,path)
            //real_time_db_Reference.child("caretakers").child(it.uid).child("medicines")
                real_time_db_Reference = real_time_db.getReference(path)
                real_time_db_Reference.addValueEventListener(object : ValueEventListener {

                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    notificationMedArrayList.clear()
                    if (snapshot.exists()){
                        for (userSnapshot in snapshot.children){
                            val notifMed : NotificationMedDTO = userSnapshot.getValue(NotificationMedDTO::class.java)!!
                            notifMed.addNotId(userSnapshot.key)
                            notificationMedArrayList.add(notifMed)
                        }

                        if(careFlag)
                            rv_dependants.adapter = NotifMedAdapter(notificationMedArrayList)
                        else
                            rv_dependants.adapter = NotifMedDepAdapter(notificationMedArrayList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        //}

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toogle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}

