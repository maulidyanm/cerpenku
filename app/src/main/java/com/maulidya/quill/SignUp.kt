package com.maulidya.quill

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.maulidya.quill.databinding.ActivitySignUpBinding
import com.maulidya.quill.ModelUser

class SignUp : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.btdaftar.setOnClickListener {
            val username = binding.usrnamedaftar.text.toString()
            val email = binding.emaildaftar.text.toString()
            val password = binding.passworddaftar.text.toString()

            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = FirebaseAuth.getInstance().currentUser?.uid

                            // Membuat objek ModelUser
                            val user = ModelUser(userId!!, username, email)

                            // Simpan data pengguna ke Realtime Database
                            val database = FirebaseDatabase.getInstance("https://maulidyaquill-default-rtdb.asia-southeast1.firebasedatabase.app/")
                            val userRef = database.getReference("user")
                            userRef.child(userId).setValue(user)
                                .addOnSuccessListener {
                                    // Kirim email verifikasi (hanya jika akun berhasil dibuat)
                                    val user = FirebaseAuth.getInstance().currentUser
                                    user?.sendEmailVerification()
                                        ?.addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Toast.makeText(this,
                                                    "Registrasi berhasil! Silahkan cek email untuk verifikasi.", Toast.LENGTH_LONG).show()
                                                val intent = Intent(this, MainActivity::class.java)
                                                startActivity(intent)
                                                finish()
                                            } else {
                                                Toast.makeText(this, "Gagal mengirim email verifikasi", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Gagal menyimpan data pengguna", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            // Menampilkan pesan error dengan detail
                            task.exception?.localizedMessage?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
                        }
                    }
            } else {
                Toast.makeText(this, "Data tidak boleh ada yang kosong", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvsudahpunyaakun.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}