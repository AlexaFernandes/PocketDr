package com.example.pocketDR.medicine


import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.example.pocketDR.R
import java.text.SimpleDateFormat
import java.util.*
import com.example.pocketDR.DTO.MedicineDTO
import com.example.pocketDR.DTO.NotificationMedDTO
import com.example.pocketDR._LoginActivity
import com.example.pocketDR.MainActivity
import com.example.pocketDR.NotificationUtils
import com.example.pocketDR.dependant.DependantsActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_add_medicine.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates


class AddMedicineActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "AddMedicineActivity"
    }

    var dateFormat = SimpleDateFormat("dd MMM, yyyy", Locale.US)
    lateinit var beginDateInput: Date
    lateinit var endDateInput: Date

    private var careFlag by Delegates.notNull<Boolean>()
    private lateinit var dependantuid: String
    lateinit var path: String
    private lateinit var auth: FirebaseAuth

    // Access a Cloud Firestore instance from your Activity
    private val db = Firebase.firestore
    private lateinit var real_time_db: FirebaseDatabase
    private lateinit var real_time_db_Reference: DatabaseReference
    lateinit var toogle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        careFlag = intent.getStringExtra("care_flag").toBoolean()
        dependantuid = intent.getStringExtra("dep_uid").toString()
        Log.i(MainActivity.TAG, "careFlag : $careFlag       , $dependantuid")

        setContentView(R.layout.activity_add_medicine)

        auth = Firebase.auth

        val drawer: DrawerLayout = findViewById(R.id.drawer)
        toogle = ActionBarDrawerToggle(this, drawer, R.string.open, R.string.close)
        drawer.addDrawerListener(toogle)
        toogle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val navView: NavigationView = findViewById(R.id.navView)

        navBar(navView)

        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser == null) { //verifica se há um user logado
            Toast.makeText(this, "No signed user", Toast.LENGTH_SHORT).show()
            return
        }

        val cal = Calendar.getInstance()
        val beginDate: Button = findViewById(R.id.beginDate);
        var inputBeginDate: Date = Calendar.getInstance().time
        val endDate: Button = findViewById(R.id.end_Date);
        val addMedicine: Button = findViewById(R.id.addMedicine);

        beginDate.setOnClickListener {

            val beginDatePicker = DatePickerDialog(
                this, R.style.dateTimePicker,
                DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                    val selectDate = Calendar.getInstance()
                    selectDate.set(Calendar.YEAR, year)
                    selectDate.set(Calendar.MONTH, month)
                    selectDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    beginDateInput = selectDate.time
                    val date = dateFormat.format(selectDate.time)
                    beginDate.text = date
                    //Toast.makeText(this, "date :" + date, Toast.LENGTH_SHORT).show()
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            beginDatePicker.show()
            beginDatePicker.datePicker.minDate = System.currentTimeMillis() - 1000;
        }


        endDate.setOnClickListener {
            if (beginDate.text == "Begin Date") {
                Toast.makeText(this, "You should select the begin date first!", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val endDate = DatePickerDialog(
                    this, R.style.dateTimePicker,
                    DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                        val selectDate = Calendar.getInstance()
                        selectDate.set(Calendar.YEAR, year)
                        selectDate.set(Calendar.MONTH, month)
                        selectDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        endDateInput = selectDate.time
                        val date = dateFormat.format(selectDate.time)
                        endDate.text = date
                        //Toast.makeText(this, "date :" + date, Toast.LENGTH_SHORT).show()
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                )
                endDate.show()
                endDate.datePicker.minDate = inputBeginDate.time;
            }
        }

        val hours: ArrayList<Date> = ArrayList()

        fun hourFourButton(timeButton: Button) {
            timeButton.setOnClickListener {
                val timeSetListener =
                    TimePickerDialog.OnTimeSetListener { timePicker: TimePicker?, hour: Int, minute: Int ->
                        cal.set(Calendar.HOUR_OF_DAY, hour)
                        cal.set(Calendar.MINUTE, minute)
                        //set time to textView
                        timeButton.text = SimpleDateFormat("HH:mm").format(cal.time)
                        hours.add(cal.time)
                    }
                TimePickerDialog(
                    this, R.style.dateTimePicker,
                    timeSetListener,
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true
                ).show()
            }
        }


        val spinnerAlarms: Spinner = findViewById(R.id.spinnerAlarms)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.alarms,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerAlarms.adapter = adapter
        }

        spinnerAlarms.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {

            @SuppressLint("StaticFieldLeak")
            val button1: Button = findViewById(R.id.timeButton1);

            @SuppressLint("StaticFieldLeak")
            val button2: Button = findViewById(R.id.timeButton2);

            @SuppressLint("StaticFieldLeak")
            val button3: Button = findViewById(R.id.timeButton3);

            @SuppressLint("StaticFieldLeak")
            val button4: Button = findViewById(R.id.timeButton4);

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long,
            ) {


                val text: String = spinnerAlarms.getSelectedItem().toString()
                when (text) {
                    "Once a day" -> {
                        hourFourButton(button1)
                        button1.setVisibility(View.VISIBLE)
                        button2.setVisibility(View.INVISIBLE)
                        button3.setVisibility(View.INVISIBLE)
                        button4.setVisibility(View.INVISIBLE)
                    }
                    "Twice a day" -> {
                        hourFourButton(button1)
                        hourFourButton(button2)
                        button1.setVisibility(View.VISIBLE)
                        button2.setVisibility(View.VISIBLE)
                        button3.setVisibility(View.INVISIBLE)
                        button4.setVisibility(View.INVISIBLE)
                    }
                    "Three times a day" -> {
                        hourFourButton(button1)
                        hourFourButton(button2)
                        hourFourButton(button3)
                        button1.setVisibility(View.VISIBLE)
                        button2.setVisibility(View.VISIBLE)
                        button3.setVisibility(View.VISIBLE)
                        button4.setVisibility(View.INVISIBLE)
                    }
                    "Four times a day" -> {
                        hourFourButton(button1)
                        hourFourButton(button2)
                        hourFourButton(button3)
                        hourFourButton(button4)
                        button1.setVisibility(View.VISIBLE)
                        button2.setVisibility(View.VISIBLE)
                        button3.setVisibility(View.VISIBLE)
                        button4.setVisibility(View.VISIBLE)
                    }
                    else -> {
                        button1.setVisibility(View.INVISIBLE)
                        button2.setVisibility(View.INVISIBLE)
                        button3.setVisibility(View.INVISIBLE)
                        button4.setVisibility(View.INVISIBLE)
                        Toast.makeText(this@AddMedicineActivity, "ERROR", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                button1.setVisibility(View.INVISIBLE)
                button2.setVisibility(View.INVISIBLE)
                button3.setVisibility(View.INVISIBLE)
                button4.setVisibility(View.INVISIBLE)
            }
        }


        addMedicine.setOnClickListener { _ ->

            var medicine = MedicineDTO();

            with(medicine) {

                medicineName = input_med_name.text.toString()
                description = input_notes.text.toString()
                startDate = beginDate.text.toString()
                medicine.endDate = end_Date.text.toString()
                if (careFlag)
                    userId = currentUser.uid
                else
                    userId = dependantuid

            }
            setWeeksDays(medicine)
            setHours(medicine)

            real_time_db = FirebaseDatabase.getInstance()

            real_time_db_Reference = real_time_db.getReference()
            val newMedId: String = real_time_db_Reference.child("medicines").push().key.toString()
            medicine.medicineId = newMedId
            real_time_db_Reference.child("medicines").child(newMedId).setValue(medicine)

            setMedicineToDaysAndHours(medicine, hours)
            val addMedIntent = Intent(this, MainActivity::class.java).putExtra("care_flag", "true")


            startActivity(addMedIntent)
            finish()
        }

        cancelMedicine.setOnClickListener {
            val cancelMedIntent =
                Intent(this, MainActivity::class.java).putExtra("care_flag", "true")
            startActivity(cancelMedIntent)
            finish()
        }

    }

    private fun setWeeksDays(medicine: MedicineDTO) {
        var everyday =
            booleanArrayOf(true, true, true, true, true, true, true).toCollection(ArrayList())
        if (dv_every_day.isChecked) {
            medicine.setDays(everyday)
        } else {
            if (dv_sunday.isChecked) medicine.setOneDay(true, 0) else medicine.setOneDay(false, 0)
            if (dv_monday.isChecked) medicine.setOneDay(true, 1) else medicine.setOneDay(false, 1)
            if (dv_tuesday.isChecked) medicine.setOneDay(true, 2) else medicine.setOneDay(false, 2)
            if (dv_wednesday.isChecked) medicine.setOneDay(true, 3) else medicine.setOneDay(false,3)
            if (dv_thursday.isChecked) medicine.setOneDay(true, 4) else medicine.setOneDay(false, 4)
            if (dv_friday.isChecked) medicine.setOneDay(true, 5) else medicine.setOneDay(false, 5)
            if (dv_saturday.isChecked) medicine.setOneDay(true, 6) else medicine.setOneDay(false, 6)
        }
    }

    private fun setHours(medicine: MedicineDTO) {
        if (timeButton1.text != "Select Time") medicine.setOneHour(timeButton1.text.toString())
        if (timeButton2.text != "Select Time") medicine.setOneHour(timeButton2.text.toString())
        if (timeButton3.text != "Select Time") medicine.setOneHour(timeButton3.text.toString())
        if (timeButton4.text != "Select Time") medicine.setOneHour(timeButton4.text.toString())
    }

    private fun setMedicineToDaysAndHours(medicine: MedicineDTO, hours: List<Date>) {
        /*var snap_careFlag: Boolean = true

        val query_result = db.collection("users").document(medicine.userId)
        //according to the current's user data, it is possible to check if it is a dependant or a caretaker
        query_result.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            val source = (snapshot != null && snapshot.metadata.hasPendingWrites())
            //after checking if there are no pending write, the next activity is opened accordingly
            if (snapshot != null && snapshot.exists()) {
                snap_careFlag = snapshot.getField<Boolean>("care_flag")!!
            } else {
                Log.d(TAG, "$source data: null")
            }
        }*/

        val numberAlarms = medicine.hours.size;
        var actualDate: Date = beginDateInput
        var c = Calendar.getInstance()
        c.time = actualDate
        var dayOfWeek = c[Calendar.DAY_OF_WEEK];

        while (actualDate <= endDateInput) {

            dayOfWeek = c[Calendar.DAY_OF_WEEK];

            for (actualAlarm in 1..numberAlarms) {
                if (dayOfWeek == 1 && medicine.getOneDay(0)) {
                    saveNotifyMed(
                        actualDate.toString(),
                        medicine.getOneHour(actualAlarm - 1),
                        medicine.medicineId,
                        medicine.userId,
                        careFlag, actualDate, hours.get(actualAlarm - 1), medicine.medicineName
                    );
                } else if (dayOfWeek == 2 && medicine.getOneDay(1)) {
                    saveNotifyMed(
                        actualDate.toString(),
                        medicine.getOneHour(actualAlarm - 1),
                        medicine.medicineId,
                        medicine.userId,
                        careFlag,
                        actualDate,
                        hours.get(actualAlarm - 1),
                        medicine.medicineName
                    );
                } else if (dayOfWeek == 3 && medicine.getOneDay(2)) {
                    saveNotifyMed(
                        actualDate.toString(),
                        medicine.getOneHour(actualAlarm - 1),
                        medicine.medicineId,
                        medicine.userId,
                        careFlag,
                        actualDate,
                        hours.get(actualAlarm - 1),
                        medicine.medicineName
                    );
                } else if (dayOfWeek == 4 && medicine.getOneDay(3)) {
                    saveNotifyMed(
                        actualDate.toString(),
                        medicine.getOneHour(actualAlarm - 1),
                        medicine.medicineId,
                        medicine.userId,
                        careFlag,
                        actualDate,
                        hours.get(actualAlarm - 1),
                        medicine.medicineName
                    );
                } else if (dayOfWeek == 5 && medicine.getOneDay(4)) {
                    saveNotifyMed(
                        actualDate.toString(),
                        medicine.getOneHour(actualAlarm - 1),
                        medicine.medicineId,
                        medicine.userId,
                        careFlag,
                        actualDate,
                        hours.get(actualAlarm - 1),
                        medicine.medicineName
                    );
                } else if (dayOfWeek == 6 && medicine.getOneDay(5)) {
                    saveNotifyMed(
                        actualDate.toString(),
                        medicine.getOneHour(actualAlarm - 1),
                        medicine.medicineId,
                        medicine.userId,
                        careFlag,
                        actualDate,
                        hours.get(actualAlarm - 1),
                        medicine.medicineName
                    );
                } else if (dayOfWeek == 7 && medicine.getOneDay(6)) {
                    saveNotifyMed(
                        actualDate.toString(),
                        medicine.getOneHour(actualAlarm - 1),
                        medicine.medicineId,
                        medicine.userId,
                        careFlag,
                        actualDate,
                        hours.get(actualAlarm - 1),
                        medicine.medicineName
                    );
                }
            }

            c.time = actualDate
            c.add(Calendar.DATE, 1)
            actualDate = c.time

        }
    }

    private fun saveNotifyMed(
        actualDate: String,
        actualHour: String,
        medicineId: String,
        userId: String,
        careFlag: Boolean,
        dayOfNot: Date,
        hours: Date,
        nameMed: String
    ) {

        var medNoti = NotificationMedDTO();
        with(medNoti) {
            hour = actualHour
            date = actualDate.toString()
            isTaken = false;
            idMed = medicineId
            idUser = userId;
            this.nameMed = nameMed;
        }

        real_time_db = FirebaseDatabase.getInstance()
        real_time_db_Reference = real_time_db.getReference()

        if (careFlag) {
            real_time_db_Reference.child("caretakers").child(userId).child("medicines")
                .push().setValue(medNoti)
        } else {
            real_time_db_Reference.child("dependants").child(userId).child("medicines")
                .push().setValue(medNoti)
        }

        var cal = Calendar.getInstance()
        cal.time = dayOfNot
        val cal1 = Calendar.getInstance()
        cal1.time = hours
        cal.set(Calendar.HOUR_OF_DAY, cal1.get(Calendar.HOUR_OF_DAY))
        cal.set(Calendar.MINUTE, cal1.get(Calendar.MINUTE))

        Log.v(TAG, hours.toString())

        var mNotificationTime =cal.timeInMillis

        var mNotified = false

        if (!mNotified) {
            NotificationUtils().setNotification(
                mNotificationTime,
                this@AddMedicineActivity
            )
        }
    }

    fun navBar(navView: NavigationView) {
        navView.menu.getItem(1).isVisible = false //add medicine was added to the button
        navView.setNavigationItemSelectedListener {

            when (it.itemId) {
                R.id.nav_med -> startActivity(
                    Intent(this, MainActivity::class.java)
                        .putExtra("care_flag", "true")
                )
                R.id.nav_dependants -> startActivity(
                    Intent(this, DependantsActivity::class.java)
                        .putExtra("care_flag", "true")
                )
                R.id.nav_curr_user -> Toast.makeText(
                    this,
                    auth.currentUser?.email,
                    Toast.LENGTH_SHORT
                ).show()
                R.id.nav_logout,
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toogle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}




