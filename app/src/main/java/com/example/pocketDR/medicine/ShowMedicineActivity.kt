package com.example.pocketDR.dependant

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
import com.example.pocketDR.DTO.MedicineDTO
import com.example.pocketDR.DTO.NotificationMedDTO
import com.example.pocketDR.medicine.AddMedicineActivity
import com.example.pocketDR.recyclerview.NotifMedAdapter
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_add_medicine.*
import kotlinx.android.synthetic.main.activity_dependant.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.properties.Delegates

class ShowMedicineActivity : AppCompatActivity() {

    lateinit var toogle: ActionBarDrawerToggle

    private companion object { //just for some logs
        public const val TAG = "ShowMedicineActivity"
    }

    private var careFlag by Delegates.notNull<Boolean>()
    private lateinit var auth: FirebaseAuth

    // Access a Cloud Firestore instance from your Activity
    private val db = Firebase.firestore
    private var real_time_db_Reference = FirebaseDatabase.getInstance().getReference()

    //private lateinit var medicine : MedicineDTO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val med_id = intent.getStringExtra("med_id")
        careFlag = intent.getStringExtra("care_flag").toBoolean()
        Log.i(TAG, "careFlag : $careFlag")
        setContentView(R.layout.activity_show_medicine)

        val drawer: DrawerLayout = findViewById(R.id.drawer)
        toogle = ActionBarDrawerToggle(this, drawer, R.string.open, R.string.close)
        drawer.addDrawerListener(toogle)
        toogle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val navView: NavigationView = findViewById(R.id.navView)

        navBar(navView)

        auth = Firebase.auth //connect to the authentication server
        val real_time_db = FirebaseDatabase.getInstance()
        val real_time_db_Reference = real_time_db.getReference()


        if (med_id != null) {
            real_time_db_Reference.child("medicines").child(med_id).get()
                .addOnSuccessListener {
                    var medicine= it.getValue(MedicineDTO::class.java)!!
                    Log.i("firebase", "Got value ${medicine.medicineName}")
                    var medName: TextView = findViewById(R.id.medName)
                    var medNotes: TextView = findViewById(R.id.medNotes)

                    var beginDate:TextView = findViewById(R.id.beginDate)
                    var endDate:TextView = findViewById(R.id.endDate)


                    medName.text=medicine.getMedicineName()
                    medNotes.text = medicine.getDescription()
                    beginDate.text=medicine.getStartDate()
                    endDate.text=medicine.getEndDate()
                    setWeeksDays(medicine)
                }.addOnFailureListener{
                    Log.e("firebase", "Error getting data", it)
                }
        }
    }

    private fun setWeeksDays(medicine: MedicineDTO) {

        for (pos in 0..6) {
            if (medicine.getOneDay(pos) == true) {
                when (pos) {
                    0 -> dv_sunday.isChecked=true
                    1 -> dv_monday.isChecked=true
                    2 -> dv_tuesday.isChecked=true
                    3 -> dv_wednesday.isChecked=true
                    4 -> dv_thursday.isChecked=true
                    5 -> dv_friday.isChecked=true
                    6 -> dv_saturday.isChecked=true
                }
            }
        }
    }

    fun navBar(navView: NavigationView) {

        navView.menu.getItem(1).isVisible=false //add medicine was added to the button
        if (careFlag) { //set definitions for Caretaker
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
        else {//set definitions for Dependant
            navView.menu.getItem(2).isVisible = false
            navView.setNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.nav_med -> {
                        startActivity(
                            Intent(this, MainActivity::class.java)
                                .putExtra("care_flag", "false")
                        )
                        finish()
                    }
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


}

