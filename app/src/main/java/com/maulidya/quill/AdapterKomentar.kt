package com.maulidya.quill

import ModelKomentar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdapterKomentar(private val listKomentar: MutableList<ModelKomentar>
) : RecyclerView.Adapter<AdapterKomentar.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        val tvIsiKomen: TextView = itemView.findViewById(R.id.tvIsiKomen)
        val tvWaktuKomen: TextView = itemView.findViewById(R.id.tvWaktuKomen)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.card_komentar, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount() = listKomentar.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val komentar = listKomentar[position]
        holder.tvIsiKomen.text = komentar.isiKomentar
        holder.tvUsername.text = komentar.username

        val formatter = SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.getDefault())
        val waktuDate: Date? = formatter.parse(komentar.waktuKomentar)
        val waktuTampil = if (waktuDate != null) {
            SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(waktuDate)
        } else {
            "Waktu tidak valid"
        }
        holder.tvWaktuKomen.text = waktuTampil

    }

}