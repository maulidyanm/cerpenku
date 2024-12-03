package com.maulidya.quill

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class Profile : AppCompatActivity() {

    private lateinit var usernameText: TextView
    private lateinit var emailText: TextView
    private lateinit var passwordText: TextView
    private lateinit var gantipassword: TextView
    private lateinit var logout: AppCompatButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        usernameText = findViewById(R.id.username)
        emailText = findViewById(R.id.email)
        passwordText = findViewById(R.id.password)
        gantipassword = findViewById(R.id.tvgantipassword)
        logout = findViewById(R.id.btlogout)

        getUserData()

        // Tampilkan pesan default sementara data sedang diambil
        usernameText.text = "Memuat data pengguna..."
        emailText.text = ""
        passwordText.text = ""

        gantipassword.setOnClickListener {
            openChangePasswordDialog()
        }

        logout.setOnClickListener {
            // Tampilkan dialog konfirmasi logout
            AlertDialog.Builder(this)
                .setTitle("Konfirmasi Logout")
                .setMessage("Apakah Anda yakin ingin logout?")
                .setPositiveButton("Ya") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Tidak", null)
                .show()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun openChangePasswordDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.dialog_change_password, null)
        builder.setView(view)

        // Inisialisasi komponen dalam dialog
        val passwordLama = view.findViewById<EditText>(R.id.passwordlama)
        val passwordBaru = view.findViewById<EditText>(R.id.passwordbaru)
        val konfirmasiPassword = view.findViewById<EditText>(R.id.konfirmasipassword)
        val btganti = view.findViewById<Button>(R.id.btganti)
        val btcancel = view.findViewById<Button>(R.id.btcancel)

        val dialog = builder.create()

        btganti.setOnClickListener {
            val oldPassword = passwordLama.text.toString().trim()
            val newPassword = passwordBaru.text.toString().trim()
            val confirmPassword = konfirmasiPassword.text.toString().trim()

            // Validasi jika ada input yang kosong
            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Mohon isikan semua kolom", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validasi jika password baru tidak cocok dengan konfirmasi
            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Password baru tidak cocok dengan konfirmasi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Proses perubahan password
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null && user.email != null) {
                val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)
                user.reauthenticate(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Toast.makeText(this, "Password berhasil diubah", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            } else {
                                Toast.makeText(this, "Gagal mengubah password: ${updateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Password lama salah", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }



        btcancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }



    //mendapatkan data user
    private fun getUserData() {
        val idUser = FirebaseAuth.getInstance().currentUser?.uid

        if (idUser != null) {
            val database = FirebaseDatabase.getInstance("https://maulidyaquill-default-rtdb.asia-southeast1.firebasedatabase.app/")
            val userRef = database.getReference("user").child(idUser)

            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val user = snapshot.getValue(ModelUser::class.java)
                    if (user != null) {
                        updateProfileUI(user.username, user.email)
                    } else {
                        Toast.makeText(this, "Data pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Data pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { error ->
                Log.e("Profile", "Error getting user data: ${error.message}")
                Toast.makeText(this, "Terjadi kesalahan: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateProfileUI(username: String, email: String) {
        if (username.isNotEmpty()) {
            usernameText.text = username
        } else {
            usernameText.text = "Username tidak tersedia"
        }
        if (email.isNotEmpty()) {
            emailText.text = email
        } else {
            emailText.text = "Email tidak tersedia"
        }
        passwordText.text = "Password telah diatur"
    }
}