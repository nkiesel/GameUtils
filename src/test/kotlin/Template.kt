import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe

object Template {
    private fun parse(input: List<String>) = input

    fun one(input: List<String>): Int {
        return 0
    }

    fun two(input: List<String>): Int {
        return 0
    }
}

val TemplateTest by testSuite {
    val input = lines("Template")

    val sample = """""".trimIndent().lines()

    with(Template) {
        test("one") {
            one(sample) shouldBe 0
//            one(input) shouldBe 0
        }

        test("two") {
            two(sample) shouldBe 0
//            two(input) shouldBe 0
        }
    }
}
