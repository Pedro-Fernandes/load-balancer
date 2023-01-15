import java.lang.Thread.sleep
import java.util.UUID
import kotlin.random.Random
import kotlin.random.nextInt

class Provider {
    private val id: String = UUID.randomUUID().toString();
    fun get(): String = this.id

    fun check() : String? {
        /*
        Simulating a scenario where it will eventually not be alive
         */
        return if(Random.nextInt(0 until 6) < 4){
            "Alive"
        }else {
            sleep(5)
            null
        }
    }
}