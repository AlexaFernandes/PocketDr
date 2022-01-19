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


class NotifMedDontRemoveAdapter(private val notifList: ArrayList<NotificationMedDTO>) :
    RecyclerView.Adapter<NotifMedDontRemoveAdapter.MyViewHolder>() {

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
        if(currentitem.isTaken)
            holder.isTaken.text = "Taken"
        else
            holder.isTaken.text="Not taken"

        holder.cv.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                Log.i(TAG,"You clicked on ${currentitem.nameMed}")
                //go to activity ShowMedicineActivity
                if (v != null) {
                    val context: Context = v.context
                    val intent = Intent(context, ShowMedicineActivity::class.java)
                    Log.i(TAG,currentitem.idMed)
                    intent.putExtra("med_id",currentitem.idMed)
                    intent.putExtra("care_flag","true")
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

}
