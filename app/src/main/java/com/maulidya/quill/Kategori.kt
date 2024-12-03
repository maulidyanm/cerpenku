package com.maulidya.quill

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Kategori : AppCompatActivity() {
    lateinit var semuaKarya : TextView
    lateinit var komedi : CardView
    lateinit var persahabatan : CardView
    lateinit var pendidikan : CardView
    lateinit var teknologi : CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_kategori)

        semuaKarya = findViewById(R.id.lihatkarya)
        komedi = findViewById(R.id.cvKomedi)
        persahabatan = findViewById(R.id.cvPersahabatan)
        pendidikan = findViewById(R.id.cvPendidikan)
        teknologi = findViewById(R.id.cvTeknologi)

        semuaKarya.setOnClickListener {
            val intent = Intent(this@Kategori, Search::class.java)
            startActivity(intent)
        }
        komedi.setOnClickListener {
            val intent = Intent(this@Kategori, Comedy::class.java)
            startActivity(intent)
        }
        persahabatan.setOnClickListener {
            val intent = Intent(this@Kategori, Persahabatan::class.java)
            startActivity(intent)
        }
        pendidikan.setOnClickListener {
            val intent = Intent(this@Kategori, Pendidikan::class.java)
            startActivity(intent)
        }
        teknologi.setOnClickListener {
            val intent = Intent(this@Kategori, Teknologi::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}