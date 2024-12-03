package com.maulidya.quill

data class ModelUser (
    val idUser: String = "",
    val username: String = "",
    val email: String = "",
    val koleksiKarya: List<String> = emptyList(),
    val karyaFavorit: MutableMap<String, Boolean> = mutableMapOf()
) {
    constructor() : this("", "", "")
}
