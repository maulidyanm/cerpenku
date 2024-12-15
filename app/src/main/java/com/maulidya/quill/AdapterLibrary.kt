package com.maulidya.quill

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class AdapterLibrary(private val context: Context, private val karyaList: MutableList<ModelKarya>) :
    RecyclerView.Adapter<AdapterLibrary.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvJudul: TextView = itemView.findViewById(R.id.ljudul)
        var tvKategori: TextView = itemView.findViewById(R.id.lkategori)
        var tvTanggal: TextView = itemView.findViewById(R.id.ltanggal)
        var menu: ImageView = itemView.findViewById(R.id.menu)
        var card: CardView = itemView.findViewById(R.id.cvLibrary)

        init {
            menu.setOnClickListener {
                showPopupMenu(it, adapterPosition)
            }
        }

        private fun showPopupMenu(view: View, position: Int) {
            val popupMenu = PopupMenu(context, view)
            popupMenu.inflate(R.menu.show_menu)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.editText -> {
                        val karya = karyaList[position]
                        val intent = Intent(context, EditBook::class.java)
                        // Kirim semua data karya
                        intent.putExtra("idKarya", karya.idKarya)
                        intent.putExtra("judul", karya.judul)
                        intent.putExtra("kategori", karya.kategori)
                        intent.putExtra("tanggal", karya.tanggal)
                        intent.putExtra("penulis", karya.penulis)
                        intent.putExtra("isi", karya.isi)
                        context.startActivity(intent)
                        true
                    }
                    R.id.delete -> {
                        // Konfirmasi penghapusan
                        AlertDialog.Builder(context)
                            .setTitle("Hapus Karya")
                            .setMessage("Apakah Anda yakin ingin menghapus karya ini?")
                            .setPositiveButton("Ya") { _, _ ->
                                val karyaId = karyaList[position].idKarya.toString()

                                // Panggil fungsi hapus data dari Firebase
                                (context as Library).hapusDataDariFirebase(karyaId)

                                // Hapus dari list dan update UI setelah penghapusan dari Firebase berhasil
                                karyaList.removeAt(position)
                                notifyItemRemoved(position)
                                Toast.makeText(context, "Karya berhasil dihapus", Toast.LENGTH_SHORT).show()
                            }
                            .setNegativeButton("Tidak", null)
                            .show()
                        true
                    }

                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_library, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount() = karyaList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val karya = karyaList[position]
        holder.tvJudul.text = karya.judul
        holder.tvKategori.text = karya.kategori
        holder.tvTanggal.text = karya.tanggal

        holder.card.setOnClickListener {
            val intent = Intent(context, EditBook::class.java).apply {
                putExtra("idKarya", karya.idKarya)
                putExtra("judul", karya.judul)
                putExtra("kategori", karya.kategori)
                putExtra("tanggal", karya.tanggal)
                putExtra("penulis", karya.penulis)
                putExtra("isi", karya.isi)
            }
            context.startActivity(intent)
        }
    }
}