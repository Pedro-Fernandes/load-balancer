import org.junit.jupiter.api.Test
import io.mockk.*
import org.junit.jupiter.api.Assertions.*

class LoadBalancerTest{

    private lateinit var loadBalancer: LoadBalancer
    private val mockedProvider = mockk<Provider>()
    private val secondMockedProvider = mockk<Provider>()

    @Test
    fun `when only one provider and load balancer get is called, return provider id`(){
        loadBalancer = LoadBalancer()
        every { mockedProvider.get() } returns "1234"

        loadBalancer.registerProvider(mockedProvider)

        val result = loadBalancer.get()

        assertEquals("1234", result)
    }

    @Test
    fun `when two providers and load balancer get is called twice, different providers return their id`(){
        loadBalancer = LoadBalancer()
        every { mockedProvider.get() } returns "1234"
        every { secondMockedProvider.get() } returns "4567"

        loadBalancer.registerProvider(mockedProvider)
        loadBalancer.registerProvider(secondMockedProvider)

        val firstResult = loadBalancer.get()
        val secondResult = loadBalancer.get()

        assertTrue(listOf("1234", "4567").contains(firstResult))
        assertTrue(listOf("1234", "4567").contains(secondResult))
        assertFalse(firstResult == secondResult)
    }

    @Test
    fun `removes provider when no heartbeat response`(){
        loadBalancer = LoadBalancer()
        every {
            mockedProvider.check()
        } returns null
        every { mockedProvider.get() } returns "1234"

        loadBalancer.registerProvider(mockedProvider)
        loadBalancer.checkProviders()

        val result = loadBalancer.get()

        assertEquals("Request denied. Too many requests", result)
    }

    @Test
    fun `re-adds provider when enough heartBeat was reached`(){
        loadBalancer = LoadBalancer()
        every {
            mockedProvider.check()
        } returns null
        every { mockedProvider.get() } returns "1234"

        loadBalancer.registerProvider(mockedProvider)
        loadBalancer.checkProviders()

        var result = loadBalancer.get()

        assertEquals("Request denied. Too many requests", result)

        every {
            mockedProvider.check()
        } returns "Alive"

        repeat(2){
            loadBalancer.checkProviders()
        }

        result = loadBalancer.get()

        assertEquals("1234", result)
    }

    @Test
    fun `refuses new request when limit is reached`(){
        loadBalancer = LoadBalancer(
            parallelRequests = 100
        )
        loadBalancer.fillProvidersList()

        val result = loadBalancer.get()

        assertEquals("Request denied. Too many requests", result)
    }
}