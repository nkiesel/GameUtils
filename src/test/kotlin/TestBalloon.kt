import de.infix.testBalloon.framework.core.TestConfig
import de.infix.testBalloon.framework.core.TestElement
import de.infix.testBalloon.framework.core.TestSession
import de.infix.testBalloon.framework.core.aroundEachTest
import kotlin.time.measureTime

class ModuleTestSession : TestSession(testConfig = DefaultConfiguration.timingReport())

private val name = Regex("""↘(.+)»""")

private fun TestElement.Path.name() = name.find(toString())!!.groupValues[1]

fun TestConfig.timingReport(): TestConfig {
    return aroundEachTest { elementAction ->
        val duration = measureTime { elementAction() }
        println("${testElementPath.name()}: $duration")
    }
}
