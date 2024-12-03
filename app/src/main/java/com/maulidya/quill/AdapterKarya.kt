package com.maulidya.quill

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class AdapterKarya(
    private val context: Context,
    private val listHasil: MutableList<ModelKarya>,
    private val listener: OnFavoriteClickListener
) : RecyclerView.Adapter<AdapterKarya.MyViewHolder>() {

    interface OnFavoriteClickListener {
        fun addToFavorite(karya: ModelKarya)
        fun removeFromFavorite(karya: ModelKarya)
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvJudul: TextView = itemView.findViewById(R.id.tvJudul)
        val tvNamaPenulis: TextView = itemView.findViewById(R.id.tvPenulis)
        val tvKategori: TextView = itemView.findViewById(R.id.tvKategori)
        val cvCard: CardView = itemView.findViewById(R.id.cvKarya)
        val favoriteButton: ImageView = itemView.findViewById(R.id.favorite)
        val tvKomentar: TextView = itemView.findViewById(R.id.tvKomentar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.card_book, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount() = listHasil.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = listHasil[position]

        holder.tvJudul.text = item.judul
        holder.tvNamaPenulis.text = item.penulis
        holder.tvKategori.text = item.kategori
        holder.tvKomentar.text = "Komentar: "+item.jumlahKomentar.toString()
        holder.favoriteButton.setImageResource(if (item.favorite) R.drawable.favorite else R.drawable.favorite_border)

        holder.cvCard.setOnClickListener {
            val intent = Intent(context, DetailActivity::class.java).apply {
                putExtra("judul", item.judul)
                putExtra("penulis", item.penulis)
                putExtra("isi", item.isi)
                putExtra("idKarya", item.idKarya)
            }
            context.startActivity(intent)
        }

        holder.favoriteButton.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                if (item.favorite) {
                    listener.removeFromFavorite(item)
                } else {
                    listener.addToFavorite(item)
                }
            } else {
                Toast.makeText(context, "Anda perlu login untuk menambahkan ke favorit", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
