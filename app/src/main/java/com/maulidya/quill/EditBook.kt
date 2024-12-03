

package com.maulidya.quill

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditBook : AppCompatActivity() {
    lateinit var editjudul:EditText
    lateinit var editpenulis:EditText
    lateinit var editisi:EditText
    lateinit var tvtangal:TextView
    lateinit var editkategori: AutoCompleteTextView
    lateinit var btperbarui: Button
    lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_book)

        database=FirebaseDatabase.getInstance("https://maulidyaquill-default-rtdb.asia-southeast1.firebasedatabase.app/")
        editjudul=findViewById(R.id.etjudul)
        editpenulis=findViewById(R.id.etpenulis)
        editisi=findViewById(R.id.etisi)
        tvtangal=findViewById(R.id.tvtanggalpublik)
        editkategori=findViewById(R.id.kategoriedit)
        btperbarui=findViewById(R.id.btperbarui)

        // Membatasi panjang karakter input di etJudul
        val maxJudulLength = 45
        val filter = InputFilter.LengthFilter(maxJudulLength)
        editjudul.filters = arrayOf(filter)

        editjudul.requestFocus()

        val karyaId = intent.getStringExtra("idKarya")
        val judul = intent.getStringExtra("judul")
        val penulis = intent.getStringExtra("penulis")
        val isi = intent.getStringExtra("isi")
        val kategori = intent.getStringExtra("kategori")

        //set data
        editjudul.setText(judul)
        editpenulis.setText(penulis)
        editisi.setText(isi)
        editkategori.setText(kategori)

        //set tanggal
        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDate)

        //kategori
        val categories = listOf("Komedi", "Persahabatan", "Pendidikan", "Teknologi")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
        editkategori.setAdapter(adapter)

        tvtangal.text = formattedDate

        btperbarui.setOnClickListener {
            val judul = editjudul.text.toString().trim()
            val penulis = editpenulis.text.toString().trim()
            val isi = editisi.text.toString().trim()
            val kategori = editkategori.text.toString().trim()

            // Memeriksa apakah judul melebihi batas maksimal karakter (misalnya, 50 karakter)
            val maxJudulLength = 30
            if (judul.length > maxJudulLength) {
                Toast.makeText(this, "Judul maksimal $maxJudulLength karakter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (judul.isEmpty() || penulis.isEmpty() || isi.isEmpty() || kategori.isEmpty()) {
                Toast.makeText(this, "Semua kolom harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val diperbarui = ModelKarya(karyaId, penulis, judul, isi, formattedDate, kategori)

            val referensiDatabase = database.getReference("karya/$karyaId")
            referensiDatabase
                .setValue(diperbarui)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Data buku berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                        finish() // Tutup activity setelah update sukses
                    } else {
                        val error = task.exception?.localizedMessage
                        Toast.makeText(this, "Gagal memperbarui data buku: $error", Toast.LENGTH_LONG).show()
                    }
                }
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}