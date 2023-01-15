import org.apache.logging.log4j.LogManager
import java.lang.Thread.sleep

fun main(args: Array<String>) {
    val lb = LoadBalancer()

    lb.fillProvidersList()

    while (true){
        println(lb.get())
        sleep(2)
    }

}