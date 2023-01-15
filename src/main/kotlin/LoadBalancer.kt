import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.scheduleAtFixedRate

class LoadBalancer {
    private val providers: MutableList<Provider> = LinkedList()
    private var lastInvocation : Int = -1

    init {
        val worker = Executors.newSingleThreadScheduledExecutor()
        worker.scheduleAtFixedRate({ checkProviders() }, 1, HEARTBEAT_INTERVAL, TimeUnit.SECONDS)

        println("init ok")
    }

    companion object {
        private const val LIMIT : Int = 10
        private const val HEARTBEAT_INTERVAL : Long = 2
    }

    fun fillProvidersList(){
        for(i in 1..LIMIT){
            registerProvider(Provider())
        }
    }

    fun get(): String{
        var nextIndex = lastInvocation +1

        if(nextIndex >= LIMIT){
            nextIndex = 0
        }

        val invocationResult = providers[nextIndex].get()
        lastInvocation = nextIndex

        return invocationResult
    }

    fun registerProvider(provider: Provider){
        if(providers.size < LIMIT) providers.add(provider)
    }

    fun excludeProvider(id: String){
        providers.removeIf {provider : Provider -> provider.get() == id}
    }

    private fun checkProviders(){
        for (provider in providers){
            println(provider.check())
        }
    }

}