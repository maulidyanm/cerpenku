package com.maulidya.quill

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Locale

class Write : AppCompatActivity() {
    val database = FirebaseDatabase.getInstance("https://maulidyaquill-default-rtdb.asia-southeast1.firebasedatabase.app/")
    val myRef = database.getReference("karya")
    val usersRef = database.getReference("user")
    lateinit var tvTanggal : TextView
    lateinit var etJudul : EditText
    lateinit var etPenulis : EditText
    lateinit var etIsi : EditText
    lateinit var btPublik : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_write)
        tvTanggal = findViewById(R.id.tvTanggalRilis)
        etJudul = findViewById(R.id.etJudul)
        etPenulis = findViewById(R.id.etPenulis)
        etIsi = findViewById(R.id.etIsi)
        btPublik = findViewById(R.id.btPublikKarya)

        // Membatasi panjang karakter input di etJudul
        val maxJudulLength = 45
        val filter = InputFilter.LengthFilter(maxJudulLength)
        etJudul.filters = arrayOf(filter)

        // Mendapatkan UID pengguna
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Membuat referensi ke node pengguna
        val userReference = usersRef.child(uid)

        // Mendengarkan perubahan data
        userReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(ModelUser::class.java)

                if (user != null) {
                    val username = user.username
                    etPenulis.setText(username)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Failed to read user data", error.toException())
                Toast.makeText(this@Write, "Terjadi kesalahan: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        etJudul.requestFocus()

        // Mengambil tanggal dan waktu saat ini
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        tvTanggal.text = dateFormat.format(calendar.time)

        //kategori
        val items = listOf("Komedi", "Persahabatan", "Pendidikan", "Teknologi")
        val autoComplete : AutoCompleteTextView = findViewById(R.id.dpKategori)
        val adapterKategori = ArrayAdapter(this, R.layout.list_kategori, items)
        autoComplete.setAdapter(adapterKategori)
        autoComplete.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val itemSelected = adapterView.getItemAtPosition(i)
        }

        //menyimpan data di firebase
        btPublik.setOnClickListener {
            val penulis = etPenulis.text.toString()
            val judul = etJudul.text.toString()
            val isi = etIsi.text.toString()
            val tanggal = tvTanggal.text.toString()
            val kategori = autoComplete.text.toString()


            if (penulis.isEmpty() || judul.isEmpty() || isi.isEmpty() || tanggal.isEmpty() || kategori.isEmpty()) {
                Toast.makeText(this, "Harap isi semua isian", Toast.LENGTH_SHORT).show()
            } else {
                val karyaBaru = ModelKarya(null, penulis, judul, isi, tanggal, kategori)
                val newPostRef = myRef.push()
                val idKarya = newPostRef.key.toString()

                karyaBaru.idKarya = idKarya
                newPostRef.setValue(karyaBaru)
                    .addOnSuccessListener {
                        Log.d("Firebase", "Data berhasil disimpan")
                        Toast.makeText(this, "Karya baru '$judul' berhasil disimpan", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { exception ->
                        Log.e("Firebase", "Error saving data", exception)
                        Toast.makeText(this, "Gagal menyimpan karya baru: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }

                val userKaryaRef = usersRef.child(uid).child("koleksi_karya").push()
                userKaryaRef.setValue(idKarya)
                    .addOnSuccessListener {
                        Log.d("Firebase", "Data user karya berhasil disimpan")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("Firebase", "Gagal menyimpan data user karya", exception)
                    }

                // Menghapus input setelah penyimpanan
                etPenulis.setText("")
                etJudul.setText("")
                etIsi.setText("")
                tvTanggal.setText("")
                autoComplete.setText("", false)
                finish()
            }
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}