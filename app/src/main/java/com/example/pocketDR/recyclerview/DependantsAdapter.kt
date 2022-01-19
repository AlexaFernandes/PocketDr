package com.example.pocketDR.recyclerview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketDR.DTO.DependantDTO
import com.example.pocketDR.DependantFB
import com.example.pocketDR.MainActivity
import com.example.pocketDR.R
import com.example.pocketDR.dependant.DependantsActivity
import com.example.pocketDR.medicine.DisplayDepMedicineActivity


class DependantsAdapter(private val DepList : ArrayList<DependantFB>) : RecyclerView.Adapter<DependantsAdapter.MyViewHolder>() {

    private companion object { //just for some logs
        public const val TAG = "DependantAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.list_dependant,
            parent,false)
        return MyViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentitem = DepList[position]

        //val tvCareflag: TextView = holder.itemView.findViewById(R.id.tv_care_flag)

        holder.tvName.text = currentitem.name
        //holder.tvEmail.text = currentitem.email
        //tvCareflag.text = currentitem.care_flag.toString()

        holder.cv.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                Log.i(TAG,"You clicked on ${currentitem.name}")
                //go to activity displayDepMeds
                if (v != null) {
                    Log.i(TAG,v.context.toString())

                    val context: Context = v.context

                    val intent = Intent(context, DisplayDepMedicineActivity::class.java)
                    Log.i(TAG,currentitem.uid)
                    intent.putExtra("dep_uid",currentitem.uid)
                    //intent.putExtra("care_flag",false)
                    context.startActivity(intent)
                }
            }
        })
    }


    override fun getItemCount(): Int {
        return DepList.size
    }

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val tvName: TextView = itemView.findViewById(R.id.tv_user_name)
        val cv : CardView = itemView.findViewById(R.id.cv_dependant)
    }

}
