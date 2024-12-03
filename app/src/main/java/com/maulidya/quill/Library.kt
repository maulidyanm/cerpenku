package com.maulidya.quill

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale


class Library : AppCompatActivity() {

    val database = FirebaseDatabase.getInstance("https://maulidyaquill-default-rtdb.asia-southeast1.firebasedatabase.app/")
    val myRef = database.getReference("karya")
    val usersRef = database.getReference("user")
    private lateinit var karyaList: ArrayList<ModelKarya>
    private val searchDaftarList = ArrayList<ModelKarya>()
    private lateinit var recyclerview: RecyclerView
    private lateinit var adapter: AdapterLibrary
    private lateinit var searchView: SearchView
    private lateinit var progressBar: ProgressBar
    private lateinit var noDataText: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_library)

        recyclerview = findViewById(R.id.rView)
        recyclerview.layoutManager = LinearLayoutManager(this)
        recyclerview.setHasFixedSize(true)
        karyaList = arrayListOf()

        adapter = AdapterLibrary(this, searchDaftarList)
        recyclerview.adapter = adapter

        searchView = findViewById(R.id.svdaftar)
        setupSearchView()

        noDataText = findViewById(R.id.noDataText)
        progressBar = findViewById(R.id.progresslibrary)

        getData()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupSearchView() {
        searchView.clearFocus()
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val searchText = newText?.toLowerCase(Locale.getDefault()).orEmpty()
                searchDaftarList.clear()

                if (searchText.isNotEmpty()) {
                    karyaList.forEach { karya ->
                        if (
                            karya.judul?.toLowerCase(Locale.getDefault())?.contains(searchText) == true ||
                            karya.penulis?.toLowerCase(Locale.getDefault())?.contains(searchText) == true ||
                            karya.tanggal?.toLowerCase(Locale.getDefault())?.contains(searchText) == true
                        ) {
                            searchDaftarList.add(karya)
                        }
                    }
                } else {
                    // Tampilkan semua karya jika teks pencarian kosong
                    searchDaftarList.addAll(karyaList)
                }
                adapter.notifyDataSetChanged()
                return false
            }
        })
    }

    private fun getData() {
        progressBar.visibility = View.VISIBLE
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val koleksiKaryaRef = usersRef.child(uid).child("koleksi_karya")

        koleksiKaryaRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val karyaId = dataSnapshot.getValue(String::class.java)
                if (karyaId != null) {
                    val karyaRef = myRef.child(karyaId)
                    karyaRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            progressBar.visibility = View.GONE
                            val karya = snapshot.getValue(ModelKarya::class.java)
                            if (karya != null) {
                                karyaList.add(karya)
                            }

                            // Sinkronkan searchDaftarList dengan karyaList
                            searchDaftarList.clear()
                            searchDaftarList.addAll(karyaList)
                            adapter.notifyDataSetChanged()

                            // Cek apakah karyaList kosong atau tidak
                            if (karyaList.isEmpty()) {
                                noDataText.visibility = View.VISIBLE
                            } else {
                                noDataText.visibility = View.GONE
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("Firebase", "Error getting karya details", error.toException())
                            Toast.makeText(this@Library, "Gagal mengambil detail karya: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error listening for karya changes", error.toException())
                Toast.makeText(this@Library, "Gagal mendengarkan perubahan karya: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    fun hapusDataDariFirebase(karyaId: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Referensi ke path "karya" dan "user/{userId}/koleksi_karya"
        val karyaRef = database.getReference("karya").child(karyaId)
        val userKoleksiRef = usersRef.child(uid).child("koleksi_karya").child(karyaId)


        val deleteTasks = Tasks.whenAll(
            karyaRef.removeValue(),
            userKoleksiRef.removeValue()
        )

        deleteTasks.addOnSuccessListener {
            Log.d("Firebase", "Karya dengan ID $karyaId berhasil dihapus dari kedua path")
            Toast.makeText(this, "Karya berhasil dihapus dari koleksi dan database", Toast.LENGTH_SHORT).show()

        }.addOnFailureListener { exception ->
            Log.e("Firebase", "Gagal menghapus karya dari koleksi atau database", exception)
            Toast.makeText(this, "Terjadi kesalahan saat menghapus data: ${exception.message}", Toast.LENGTH_LONG).show()
        }
    }

}
