import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class LoadBalancer(
    private val registeredProviders: MutableList<Provider> = LinkedList(),
    private val availableProviders: MutableList<Provider> = LinkedList(),
    private val heartBeatManager: HeartBeatManager = HeartBeatManager(),
    private var lastInvocation : Int = -1,
    private var parallelRequests : Int = 0
) {
    companion object {
        private const val LIMIT : Int = 10
        private const val HEARTBEAT_INTERVAL : Long = 2
        private const val PROVIDERS_MAXIMUM_PARALLEL_REQUESTS = 10
    }

    init {
        val worker = Executors.newSingleThreadScheduledExecutor()
        worker.scheduleAtFixedRate({ checkProviders() }, 1, HEARTBEAT_INTERVAL, TimeUnit.SECONDS)

        println("init ok")
    }

    fun fillProvidersList() {
        for(i in 1..LIMIT) {
            registerProvider(Provider())
        }
    }

    fun get(): String {
        if(parallelRequests >= availableProviders.size*PROVIDERS_MAXIMUM_PARALLEL_REQUESTS){
            return "Request denied. Too many requests"
        }

        parallelRequests ++
        var nextIndex = lastInvocation +1

        if(nextIndex >= availableProviders.size){
            nextIndex = 0
        }

        val invocationResult = availableProviders[nextIndex].get()
        lastInvocation = nextIndex

        parallelRequests--
        return invocationResult
    }

    fun registerProvider(provider: Provider) {
        if(registeredProviders.size < LIMIT){
            heartBeatManager.addProvider(provider)
            registeredProviders.add(provider)
            availableProviders.add(provider)
        }
    }

    fun makeProviderUnavailable(id: String) {
        availableProviders.removeIf { provider : Provider -> provider.get() == id}
    }

    fun unregisterProvider(id: String) {
        registeredProviders.find { provider -> id == provider.get() }?.let { provider ->
            heartBeatManager.removeProvider(provider)
            availableProviders.remove(provider)
            registeredProviders.remove(provider)
        }
    }

    fun checkProviders() {
        for (provider in registeredProviders) {
            provider.check()?.let {
                heartBeatManager.processSuccessfulBeat(provider)
                if(hasEnoughConsecutiveBeats(provider) && isProviderUnavailable(provider)){
                        availableProviders.add(provider)
                        println("Re-added ${provider.get()}")
                }
            } ?: run {
                disableProvider(provider)
            }
        }
    }

    private fun disableProvider(provider: Provider) {
        heartBeatManager.processMissingBeat(provider)
        availableProviders.remove(provider)
        println("Disabled ${provider.get()}")
    }

    private fun isProviderUnavailable(provider: Provider) = !availableProviders.contains(provider)

    private fun hasEnoughConsecutiveBeats(provider: Provider) =
        heartBeatManager.getConsecutiveBeatCount(provider) == HeartBeatManager.HEALTHY_MIN_CONSECUTIVE_BEATS

}