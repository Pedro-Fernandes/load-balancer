class HeartBeatManager {
    private val registeredProvidersHeartBeat: MutableMap<Provider, Int> = HashMap()
    companion object {
        const val HEALTHY_MIN_CONSECUTIVE_BEATS: Int = 2
    }
    fun addProvider(provider: Provider){
        registeredProvidersHeartBeat[provider] = HEALTHY_MIN_CONSECUTIVE_BEATS
    }

    fun removeProvider(provider: Provider){
        registeredProvidersHeartBeat.remove(provider);
    }

    fun processMissingBeat(provider: Provider){
        if(registeredProvidersHeartBeat.containsKey(provider)){
            registeredProvidersHeartBeat[provider] = 0
        }
    }

    fun processSuccessfulBeat(provider: Provider){
        if(registeredProvidersHeartBeat.containsKey(provider)){
            registeredProvidersHeartBeat[provider] = registeredProvidersHeartBeat[provider]!!.inc()
        }
    }

    fun getConsecutiveBeatCount(provider: Provider): Int {
        return if(registeredProvidersHeartBeat.containsKey(provider)){
            registeredProvidersHeartBeat[provider]!!
        }else 0
    }
}