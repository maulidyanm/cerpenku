package com.maulidya.quill

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    lateinit var cvTelusur : CardView
    lateinit var cvTulis : CardView
    lateinit var cvPerpus : CardView
    lateinit var cvSuka : CardView
    lateinit var profile : ImageView

    private fun checkUserLogin(): Boolean {
        val user = FirebaseAuth.getInstance().currentUser
        return user != null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        cvTelusur = findViewById(R.id.cvTelusuri)
        cvTulis = findViewById(R.id.cvTulis)
        cvPerpus = findViewById(R.id.cvPerpus)
        cvSuka = findViewById(R.id.cvFavorite)
        profile = findViewById(R.id.profil)

        cvTelusur.setOnClickListener {
            val keTelusur =
                Intent(this@MainActivity,
                    Kategori::class.java)
            startActivity(keTelusur)
        }

        cvTulis.setOnClickListener {
            if (checkUserLogin()) {
                // Pengguna sudah login
                val intent = Intent(this, Write::class.java)
                startActivity(intent)
            } else {
                // Pengguna belum login
                Toast.makeText(this, "Anda harus masuk terlebih dahulu untuk menulis karya", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
            }
        }

        cvPerpus.setOnClickListener {
            if (checkUserLogin()){
                //jika pengguna sudah login
                val kePerpus =
                    Intent(this@MainActivity,
                        Library::class.java)
                startActivity(kePerpus)
            } else {
                //jika pengguna belum login
                Toast.makeText(this, "Anda harus masuk terlebih dahulu untuk mengakses daftar karya", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
            }
        }

        cvSuka.setOnClickListener {
            if (checkUserLogin()) {
                //jika pengguna sudah login
                val intent = Intent(this, Favorite::class.java)
                startActivity(intent)
            } else {
                // Pengguna belum login
                Toast.makeText(this, "Anda harus masuk terlebih dahulu untuk mengakses halaman ini", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
            }
        }

        profile.setOnClickListener {
            if (checkUserLogin()) {
                //jika pengguna sudah login
                val intent = Intent(this, Profile::class.java)
                startActivity(intent)
            } else {
                // Pengguna belum login
                Toast.makeText(this, "Anda harus masuk terlebih dahulu untuk mengakses halaman ini", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

}