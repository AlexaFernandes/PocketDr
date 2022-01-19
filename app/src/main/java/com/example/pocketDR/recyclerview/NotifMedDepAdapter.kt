package com.example.pocketDR.recyclerview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketDR.DTO.NotificationMedDTO
import com.example.pocketDR.R
import com.example.pocketDR.dependant.ShowMedicineActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList


class NotifMedDepAdapter(private val notifList: ArrayList<NotificationMedDTO>) :
    RecyclerView.Adapter<NotifMedDepAdapter.MyViewHolder>() {

    private companion object { //just for some logs
        const val TAG = "NotMedAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.list_med,
            parent, false
        )
        return MyViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentitem = notifList[position]

        holder.hours.text = currentitem.hour
        holder.nameMed.text = currentitem.nameMed
        holder.isTaken.text = "\u2713"


        holder.isTaken.setOnClickListener {
            Log.i(TAG, "Click")
            remove(currentitem.idUser, currentitem.id);

        }

        holder.cv.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                //go to activity ShowMedicineActivity
                if (v != null) {
                    val context: Context = v.context
                    val intent = Intent(context, ShowMedicineActivity::class.java)
                    intent.putExtra("med_id",currentitem.idMed)
                    intent.putExtra("care_flag","false")
                    context.startActivity(intent)
                }
            }
        })

    }

    override fun getItemCount(): Int {
        return notifList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val hours: TextView = itemView.findViewById(R.id.tv_hours)
        val nameMed: TextView = itemView.findViewById(R.id.tv_medicineName)
        val isTaken: TextView = itemView.findViewById(R.id.tv_takeMed)
        val cv : CardView = itemView.findViewById(R.id.cv_care_med)
    }

    fun remove(idUser: String, idMed: String) {
        val ref = FirebaseDatabase.getInstance().getReference()


        ref.child("dependants").child(idUser).child("medicines").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.ref.child(idMed).removeValue()
                notifyDataSetChanged();
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException())
            }
        })

    }

}
