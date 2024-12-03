class ModelKomentar(
    val idKomentar: String?=null,
    val userId: String?=null,
    val idKarya: String? = null,
    val isiKomentar: String? = null,
    val username: String? = null,
    val waktuKomentar: String? = null // Simpan waktu sebagai string
) {
    constructor() : this(null, null, null, null, null, null)
}
