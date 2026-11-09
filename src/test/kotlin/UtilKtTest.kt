import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.math.BigInteger
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.time.measureTimedValue

val UtilKtTest  by testSuite {
    test("powerSet") {
        val s = setOf("A", "B", "C")
        val ps = s.powerSet().filter { it.isNotEmpty() && it.size != s.size }
        val pss = ps.map { it to (s - it) }
        pss shouldHaveSize 6
        println(pss)
        println(2.0.pow(16).toInt())
        val sn = (1..16).toSet()
        println(sn)
        println(measureTimedValue { sn.powerSet().size })
        println(measureTimedValue { sn.powerSetSeq().count() })
    }

    test("powerSetSeq") {
        val s = setOf("A", "B", "C")

        val ps1 = s.powerSetSeq().toList()
        ps1.size shouldBe 2.0.pow(s.size).toInt()

        val ps2 = s.powerSet().toList()
        ps1 shouldBe ps2
    }

    test("Int.pow") {
        2.pow(0) shouldBe 1L
        2.pow(1) shouldBe 2L
        2.pow(2) shouldBe 4L
        2.pow(3) shouldBe 8L
        2.pow(16) shouldBe 65536L
        3.pow(16) shouldBe 43046721L
        (-1).pow(0) shouldBe 1L
        (-1).pow(1) shouldBe -1L
        (-1).pow(2) shouldBe 1L
        (-1).pow(3) shouldBe -1L
        shouldThrow<ArithmeticException> { 4.pow(66) }
        shouldThrow<IllegalArgumentException> { 4.pow(-1) }
    }

    test("Int.isOdd") {
        0.isOdd shouldBe false
        1.isOdd shouldBe true
        2.isOdd shouldBe false
        3.isOdd shouldBe true
        4.isOdd shouldBe false
        5.isOdd shouldBe true
        (-4).isOdd shouldBe false
        (-5).isOdd shouldBe true
    }

    test("Int.isEven") {
        0.isEven shouldBe true
        1.isEven shouldBe false
        2.isEven shouldBe true
        3.isEven shouldBe false
        4.isEven shouldBe true
        5.isEven shouldBe false
        (-4).isEven shouldBe true
        (-5).isEven shouldBe false
    }

    test("permutations") {
        val list = ["a", "b", "c"]
        val permutations = list.permutations().toList()
        println(permutations)
        permutations shouldHaveSize list.size.factorial().toInt()
    }

    test("factorial") {
        6.factorial().toInt() shouldBe 720
        25.factorial() shouldBe BigInteger("15511210043330985984000000")
    }

    test("neighbors4") {
        val ia = Array(5) { IntArray(5) }

        ia.neighbors4(0, 0) shouldHaveSize 2
        ia.neighbors4(0, 1) shouldHaveSize 3
        ia.neighbors4(0, 4) shouldHaveSize 2
        ia.neighbors4(1, 1) shouldHaveSize 4
    }

    test("neighbors8") {
        val ia = Array(5) { IntArray(5) }

        ia.neighbors8(0, 0) shouldHaveSize 3
        ia.neighbors8(0, 1) shouldHaveSize 5
        ia.neighbors8(0, 4) shouldHaveSize 3
        ia.neighbors8(1, 1) shouldHaveSize 8
    }

    test("chunkedBy") {
        [3, 1, 4, 1, 5, 9].chunkedBy { it.isEven } shouldBe [[3, 1], [1, 5, 9]]
    }

    test("countingMap") {
        val map = CountingMap<String>()
        map.inc("a")
        map.inc("b")
        map.inc("c", 3L)
        map.inc("a")

        map shouldHaveSize 3
        map.count("a") shouldBe 2L
        map.count("d") shouldBe 0L

        map.entries().sumOf { it.value } shouldBe 6L

        map.set("a", 5)
        map.count("a") shouldBe 5L
        map["a"] = 7L
        map.count("a") shouldBe 7L
    }

    test("countingMapWithInit") {
        val map = CountingMap(["a", "b", "c", "d"])
        map.inc("a")
        map.inc("b")
        map.inc("c", 3L)
        map.inc("a")

        map shouldHaveSize 4
        map.count("a") shouldBe 3L
        map.count("d") shouldBe 1L

        map.entries().sumOf { it.value } shouldBe 10L
    }

    test("countingMapWithInitAndStart") {
        val map = CountingMap(["a", "b", "c", "d"], 0)
        map.inc("a")
        map.inc("b")
        map.inc("c", 3L)
        map.inc("a")

        map shouldHaveSize 4
        map.count("a") shouldBe 2L
        map.count("d") shouldBe 0L

        map.entries().sumOf { it.value } shouldBe 6L
    }

    test("gcd") {
        gcd(10, 5) shouldBe 5
        gcd(10, 15) shouldBe 5
        gcd(10L, 15L) shouldBe 5L
        gcd(10, 10) shouldBe 10
        gcd(101, 103) shouldBe 1
        gcd(101 * 3, 103 * 6) shouldBe 3
        gcd(0, 5) shouldBe 5
        gcd(5, 0) shouldBe 5
        gcd(listOf(2, 4, 8)) shouldBe 2
        gcdL(listOf(2L, 4L, 8L)) shouldBe 2L
    }

    test("numbers") {
        "a3b1c4d".ints() shouldBe [3, 1, 4]
        "-1,3-".ints() shouldBe [-1, 3]
        "a3b1c4d".longs() shouldBe [3L, 1L, 4L]
        "-1,3-".longs() shouldBe [-1L, 3L]
    }

    test("lcm") {
        lcm(10, 5) shouldBe 10
        lcm(10, 15) shouldBe 30
        lcm(10L, 15L) shouldBe 30L
        lcm(10, 10) shouldBe 10
        lcm(101, 103) shouldBe 101 * 103
        lcm(101 * 3, 103 * 6) shouldBe 101 * 103 * 6
        [2, 3, 4, 5, 6, 9, 10, 12, 100, 150].reduce { a, b -> lcm(a, b) } shouldBe 900
        lcm(listOf(2, 3, 4, 5, 6, 9, 10, 12, 100, 150)) shouldBe 900
    }

    test("minMax") {
        [3, 1, 4, 1, 5, 9].minMax() shouldBe intArrayOf(1, 9)
        val [i, a] = [3, 1, 4].minMax()
        i shouldBe 1
        a shouldBe 4
        [3L, 1L, 4L, 1L, 5L, 9L].minMax() shouldBe longArrayOf(1L, 9L)
    }

    test("minMax using multiFold") {
        fun minMax(l: List<Int>) = l.multiFold([Int.MAX_VALUE, Int.MIN_VALUE], [::min, ::max])
        minMax([3, 1, 4, 1, 5, 9]) shouldBe intArrayOf(1, 9)
    }

    test("minMax using multiReduce") {
        fun minMax(l: List<Int>) = l.multiReduce(::min, ::max)
        minMax([3, 1, 4, 1, 5, 9]) shouldBe intArrayOf(1, 9)
    }

    test("multiFold") {
        [3, 1, 4].multiFold([Int.MAX_VALUE, Int.MIN_VALUE], [::min, ::max]) shouldBe [1, 4]
    }

    test("multiReduce") {
        [3, 1, 4].reduce(::min) shouldBe 1
        [3, 1, 4].reduce(::max) shouldBe 4
        [3, 1, 4].multiReduce(::min, ::max) shouldBe [1, 4]
    }

    test("manhattanDistance") {
        manhattanDistance(0, 0, 2, 2) shouldBe 4
        manhattanDistance(0L, 0L, 2L, 2L) shouldBe 4L
        manhattanDistance(Point(0, 0), Point(2, 2)) shouldBe 4
        manhattanDistance(0, 0, 0, 2, 2, 2) shouldBe 6
        manhattanDistance(0L, 0L, 0L, 2L, 2L, 2L) shouldBe 6L
        manhattanDistance(intArrayOf(0, 0), intArrayOf(1, 2)) shouldBe 3
        manhattanDistance(longArrayOf(0L, 0L), longArrayOf(1L, 2L)) shouldBe 3L
    }

    test("LongPos") {
        val p1 = LongPos(11, 42)
        p1.x shouldBe 11
        p1.y shouldBe 42
        val p2 = LongPos(p1.value)
        p1.x shouldBe 11
        p1.y shouldBe 42
        p1 shouldBe p2
        val p3 = LongPos(-3, -5)
        p3.x shouldBe -3
        p3.y shouldBe -5
    }

    test("primes") {
        primes(1000).size shouldBe 168
        primes(200000).size shouldBe 17984
        primes(10) shouldBe listOf(2, 3, 5, 7)
    }
}
