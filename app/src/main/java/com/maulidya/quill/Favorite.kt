package com.maulidya.quill

import android.os.Bundle
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
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.Locale

class Favorite : AppCompatActivity(), AdapterKarya.OnFavoriteClickListener {
    private lateinit var adapter: AdapterKarya
    private val favoriteList = ArrayList<ModelKarya>()
    private val searchFavoriteList = ArrayList<ModelKarya>()
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var searchView: SearchView
    private lateinit var progressBar: ProgressBar
    private lateinit var noFavoriteText: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_favorite)

        firebaseAuth = FirebaseAuth.getInstance()

        // Inisialisasi Firebase
        database = FirebaseDatabase.getInstance("https://maulidyaquill-default-rtdb.asia-southeast1.firebasedatabase.app/")
        myRef = database.getReference("karya")

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerviewfav)
        recyclerView.layoutManager = LinearLayoutManager(this)

        searchView = findViewById(R.id.searchfav) // Inisialisasi SearchView
        setupSearchView()

        noFavoriteText = findViewById(R.id.noFavoriteText)
        progressBar = findViewById(R.id.progressbarfav)

        getFavoriteList() // Panggil method untuk mengambil data dari Firebase

        // Inisialisasi adapter dengan favoriteList
        adapter = AdapterKarya(this, searchFavoriteList, this)
        recyclerView.adapter = adapter

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
                searchFavoriteList.clear() // Kosongkan list hasil pencarian

                if (searchText.isNotEmpty()) {
                    // Filter favoriteList berdasarkan judul, penulis, atau kategori
                    favoriteList.forEach { karya ->
                        if (
                            karya.judul?.toLowerCase(Locale.getDefault())?.contains(searchText) == true ||
                            karya.penulis?.toLowerCase(Locale.getDefault())?.contains(searchText) == true ||
                            karya.kategori?.toLowerCase(Locale.getDefault())?.contains(searchText) == true
                        ) {
                            searchFavoriteList.add(karya)
                        }
                    }
                } else {
                    // Tampilkan semua karya favorit jika teks pencarian kosong
                    searchFavoriteList.addAll(favoriteList)
                }
                adapter.notifyDataSetChanged()
                return false
            }
        })
    }

    private fun getFavoriteList() {
        progressBar.visibility = View.VISIBLE
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            val favoriteRef = database.getReference("user/$userId/karyaFavorite")
            favoriteRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    progressBar.visibility = View.GONE
                    favoriteList.clear()
                    searchFavoriteList.clear()

                    if (snapshot.exists()) {
                        val karyaList = mutableListOf<ModelKarya>()
                        val tasks = mutableListOf<Task<DataSnapshot>>()

                        for (favoriteSnapshot in snapshot.children) {
                            val karyaId = favoriteSnapshot.key
                            if (karyaId != null) {
                                val task = myRef.child(karyaId).get()
                                tasks.add(task)
                            }
                        }

                        Tasks.whenAllSuccess<DataSnapshot>(*tasks.toTypedArray()).addOnSuccessListener { results ->
                            results.forEach { karyaSnapshot ->
                                val karya = karyaSnapshot.getValue(ModelKarya::class.java)
                                if (karya != null) {
                                    karya.favorite = true
                                    // Ambil jumlah komentar
                                    val komentarRef = myRef.child(karya.idKarya!!).child("komentar")
                                    komentarRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(komentarSnapshot: DataSnapshot) {
                                            karya.jumlahKomentar = komentarSnapshot.childrenCount.toInt()
                                            adapter.notifyDataSetChanged()
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            Toast.makeText(this@Favorite, "Gagal memuat jumlah komentar: ${error.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    })
                                    karyaList.add(karya)
                                }
                            }
                            favoriteList.addAll(karyaList)
                            searchFavoriteList.addAll(favoriteList)
                            adapter.notifyDataSetChanged()

                            // Periksa apakah favoriteList kosong
                            if (favoriteList.isEmpty()) {
                                noFavoriteText.visibility = View.VISIBLE
                            } else {
                                noFavoriteText.visibility = View.GONE
                            }
                        }.addOnFailureListener { exception ->
                            Toast.makeText(this@Favorite, "Terjadi kesalahan: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        adapter.notifyDataSetChanged()
                        // Periksa jika favoriteList kosong
                        if (favoriteList.isEmpty()) {
                            noFavoriteText.visibility = View.VISIBLE
                        } else {
                            noFavoriteText.visibility = View.GONE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Favorite, "Terjadi kesalahan: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "Pengguna belum login", Toast.LENGTH_SHORT).show()
        }
    }




    private var isUpdating = false

    override fun addToFavorite(karya: ModelKarya) {

    }

    override fun removeFromFavorite(karya: ModelKarya) {
        if (isUpdating) return
        isUpdating = true

        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            val updates = mapOf("favorite" to false)

            myRef.child(karya.idKarya.toString()).updateChildren(updates)
                .addOnSuccessListener {
                    karya.favorite = false
                    updateFavoriteStatusInLists(karya.idKarya.toString(), false)

                    // Hapus karya dari node favorit pengguna
                    database.getReference("user/$userId/karyaFavorite")
                        .child(karya.idKarya.toString()).removeValue()

                    favoriteList.remove(karya) // Menghapus karya dari daftar favorit lokal
                    Toast.makeText(this, "Karya berhasil dihapus dari favorit", Toast.LENGTH_SHORT).show()
                    adapter.notifyDataSetChanged()
                }
                .addOnCompleteListener { isUpdating = false }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal menghapus karya dari favorit", Toast.LENGTH_SHORT).show()
                    isUpdating = false
                }
        } else {
            Toast.makeText(this, "Pengguna belum login", Toast.LENGTH_SHORT).show()
            isUpdating = false
        }
    }

    private fun updateFavoriteStatusInLists(karyaId: String, isFavorite: Boolean) {
        // Cari karya berdasarkan karyaId di dalam favoriteList dan perbarui status favoritnya
        favoriteList.find { it.idKarya == karyaId }?.let {
            it.favorite = isFavorite
        }
    }
}