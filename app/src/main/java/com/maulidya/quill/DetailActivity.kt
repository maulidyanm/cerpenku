package com.maulidya.quill

import ModelKomentar
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDragHandleView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.maulidya.quill.databinding.ActivityDetailBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var dialog: BottomSheetDialog
    private lateinit var database: FirebaseDatabase
    private lateinit var etKomentar: EditText
    private lateinit var komentarAdapter: AdapterKomentar
    private lateinit var rvKomentar: RecyclerView
    private lateinit var listKomentar: MutableList<ModelKomentar>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = FirebaseDatabase.getInstance("https://maulidyaquill-default-rtdb.asia-southeast1.firebasedatabase.app/")

        dialog = BottomSheetDialog(this)

        // Inisialisasi adapter dengan list kosong
        listKomentar = mutableListOf()
        komentarAdapter = AdapterKomentar(listKomentar)

        var bundle = intent.extras
        if (bundle != null){
            binding.tvjudul.text = bundle.getString("judul")
            binding.tvpenulis.text = bundle.getString("penulis")
            binding.tvisi.text = bundle.getString("isi")
        }

        binding.showKomentar.setOnClickListener {
            showDialog()
            dialog.show()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    private fun showDialog() {
        // Inflate layout bottom sheet
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet, null, false)

        // Inisialisasi UI dari layout bottom sheet
        etKomentar = view.findViewById(R.id.etTulisKomentar)
        rvKomentar = view.findViewById(R.id.recyclerkomentar)


        // Mengatur layout manager untuk RecyclerView
        rvKomentar.layoutManager = LinearLayoutManager(this)
        rvKomentar.adapter = komentarAdapter


        // Ambil ID karya
        val idKarya = intent.getStringExtra("idKarya")
        if (idKarya.isNullOrEmpty()) {
            Toast.makeText(this, "ID karya tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        // Ambil data komentar dari Firebase dan perbarui adapter
        val komentarRef = database.getReference("karya").child(idKarya).child("komentar")
        komentarRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listKomentar.clear()
                for (data in snapshot.children) {
                    val komentar = data.getValue(ModelKomentar::class.java)
                    if (komentar != null) {
                        listKomentar.add(komentar)
                    }
                }
                komentarAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DetailActivity, "Gagal memuat komentar: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })


        // Set tombol untuk mengirim komentar
        val btnSimpanKomentar = view.findViewById<ImageView>(R.id.kirimKomentar)
        btnSimpanKomentar.setOnClickListener {
            val komentar = etKomentar.text.toString().trim()
            if (komentar.isNotEmpty()) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId.isNullOrEmpty()) {
                    Toast.makeText(this, "Silakan masuk/daftar sebelum mengirimkan komentar", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                    return@setOnClickListener
                }

                val userRef = database.getReference("user").child(userId)
                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot)
                    {
                        val
                        user = dataSnapshot.getValue(ModelUser::class.java)
                        val username = user?.username ?: "Unknown"

                        val newKomentar = HashMap<String, Any>()
                        newKomentar["isiKomentar"] = komentar
                        newKomentar["userId"] = userId
                        newKomentar["username"] = username

                        val formatter = SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.getDefault())
                        val waktuSekarang = formatter.format(Date())
                        newKomentar["waktuKomentar"] = waktuSekarang

                        val key = database.reference.push().key ?: ""

                        val komentarRef = database.getReference("karya").child(idKarya).child("komentar").child(key)
                        komentarRef.setValue(newKomentar)
                            .addOnSuccessListener {
                                Toast.makeText(this@DetailActivity, "Komentar berhasil disimpan", Toast.LENGTH_SHORT).show()
                                etKomentar.text.clear()

                                val komentarId = key
                                // TODO: Gunakan komentarId sesuai kebutuhan Anda (misalnya, untuk menampilkan detail komentar)
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@DetailActivity, "Gagal menyimpan komentar", Toast.LENGTH_SHORT).show()
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.w("DetailActivity", "Failed to read user data:", error.toException())
                    }
                })
            } else {
                Toast.makeText(this, "Silakan isi komentar terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }

        // Tampilkan bottom sheet
        dialog.setContentView(view)
        dialog.show()
    }
}