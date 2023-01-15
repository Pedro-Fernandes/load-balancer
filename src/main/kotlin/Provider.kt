import java.util.UUID

class Provider {
    private val id: String = UUID.randomUUID().toString();
    fun get(): String = this.id

    fun check() : String = "$id alive"
}