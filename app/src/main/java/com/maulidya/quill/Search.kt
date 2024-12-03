package com.maulidya.quill

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class Search : AppCompatActivity(), AdapterKarya.OnFavoriteClickListener {

    private val database = FirebaseDatabase.getInstance("https://maulidyaquill-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val myRef = database.getReference("karya")
    private val usersRef = database.getReference("user")
    private lateinit var karyaList: ArrayList<ModelKarya>
    private lateinit var searchList: ArrayList<ModelKarya>
    private val favoriteList: ArrayList<ModelKarya> = arrayListOf()
    private lateinit var adapter: AdapterKarya
    private lateinit var searchView: SearchView
    private lateinit var recyclerview: RecyclerView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        FirebaseApp.initializeApp(this)

        firebaseAuth = FirebaseAuth.getInstance()

        recyclerview = findViewById(R.id.recyclerview)
        searchView = findViewById(R.id.search)
        recyclerview.layoutManager = LinearLayoutManager(this)
        recyclerview.setHasFixedSize(true)

        karyaList = arrayListOf()
        searchList = arrayListOf()

        // Inisialisasi adapter dengan meneruskan 'this' sebagai listener
        adapter = AdapterKarya(this, searchList, this)
        recyclerview.adapter = adapter

        progressBar = findViewById(R.id.progressBar)

        getData()

        searchView.clearFocus()
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchList.clear()
                val searchText = newText?.toLowerCase(Locale.getDefault()).orEmpty()

                if (searchText.isNotEmpty()) {
                    karyaList.forEach {
                        if (
                            it.judul?.toLowerCase(Locale.getDefault())?.contains(searchText) == true ||
                            it.penulis?.toLowerCase(Locale.getDefault())?.contains(searchText) == true ||
                            it.kategori?.toLowerCase(Locale.getDefault())?.contains(searchText) == true
                        ) {
                            searchList.add(it)
                        }
                    }
                } else {
                    searchList.addAll(karyaList)
                }
                adapter.notifyDataSetChanged()
                return false
            }
        })

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun getData() {
        progressBar.visibility = View.VISIBLE

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    progressBar.visibility = View.GONE
                    karyaList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val karya = dataSnapshot.getValue(ModelKarya::class.java)
                        if (karya != null) {
                            // Ambil jumlah komentar tanpa bergantung pada status login
                            val komentarRef = myRef.child(karya.idKarya!!).child("komentar")
                            komentarRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(komentarSnapshot: DataSnapshot) {
                                    karya.jumlahKomentar = komentarSnapshot.childrenCount.toInt()
                                    adapter.notifyDataSetChanged() // Update setiap kali jumlah komentar berubah
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@Search, error.message, Toast.LENGTH_SHORT).show()
                                }
                            })

                            karyaList.add(karya)
                        }
                    }

                    val userId = firebaseAuth.currentUser?.uid
                    if (userId != null) {
                        // Ambil data favorit jika pengguna login
                        usersRef.child(userId).child("karyaFavorite")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(favoriteSnapshot: DataSnapshot) {
                                    if (favoriteSnapshot.exists()) {
                                        for (karya in karyaList) {
                                            karya.favorite = favoriteSnapshot.hasChild(karya.idKarya.toString())
                                        }
                                    }
                                    searchList.clear()
                                    searchList.addAll(karyaList)
                                    adapter.notifyDataSetChanged()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@Search, error.message, Toast.LENGTH_SHORT).show()
                                }
                            })
                    } else {
                        // Jika tidak login, tetap tampilkan semua karya
                        searchList.clear()
                        searchList.addAll(karyaList)
                        adapter.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(this@Search, "Data Kosong", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Search, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun addToFavorite(karya: ModelKarya) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            val favoriteRef = usersRef.child(userId).child("karyaFavorite").child(karya.idKarya.toString())
            favoriteRef.setValue(true)
                .addOnSuccessListener {
                    karya.favorite = true
                    Toast.makeText(this, "Karya berhasil ditambahkan ke favorit", Toast.LENGTH_SHORT).show()
                    adapter.notifyDataSetChanged() // Update adapter setelah sukses menambahkan ke favorit
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal menambahkan karya ke favorit", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Anda perlu login untuk menambahkan favorit", Toast.LENGTH_SHORT).show()
        }
    }

    override fun removeFromFavorite(karya: ModelKarya) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            val favoriteRef = usersRef.child(userId).child("karyaFavorite").child(karya.idKarya.toString())
            favoriteRef.removeValue()
                .addOnSuccessListener {
                    karya.favorite = false
                    Toast.makeText(this, "Karya berhasil dihapus dari favorit", Toast.LENGTH_SHORT).show()
                    adapter.notifyDataSetChanged() // Update adapter setelah sukses menghapus dari favorit
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal menghapus karya dari favorit", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Anda perlu login untuk menghapus favorit", Toast.LENGTH_SHORT).show()
        }
    }

}
