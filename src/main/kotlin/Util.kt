import java.math.BigInteger
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.readLines
import kotlin.math.absoluteValue

typealias IntPair = Pair<Int, Int>

fun lines(day: String) = Path("input/$day.txt").readLines()

/**
 * Creates a sequence of permutations. The number of permutations of a list of length n is n!. This is an implementation
 * of the approach described by Dijkstra in 1976. It creates an array of indices, and then in every iteration returns
 * the list of items ordered by the index array and computes the next permutation of the index array.
 */
fun <T> List<T>.permutations(): Sequence<List<T>> = sequence {
    val indices = IntArray(size) { it }
    while (true) {
        yield(indices.map { this@permutations[it] })
        val i = (indices.size - 2 downTo 0).firstOrNull { indices[it] < indices[it + 1] } ?: break
        var j = i + 1
        for (k in i + 2 until indices.size) if (indices[k] in indices[i]..indices[j]) j = k
        indices.swap(i, j)
        indices.reverse(i + 1, indices.size)
    }
}

fun IntArray.swap(i: Int, j: Int) {
    this[i] = this[j].also { this[j] = this[i] }
}

fun Int.factorial(): BigInteger {
    var result = BigInteger.ONE
    for (i in 1..this) {
        result = result.multiply(i.toBigInteger())
    }
    return result
}

// Called "bars and stars" in combinatorial math
fun combinations(parts: Int, total: Int = 100): Sequence<List<Int>> = sequence {
    val v = MutableList(parts) { 0 }
    v[0] = total
    while (true) {
        yield(v)
        if (v.last() == total) break
        if (v[0] > 0) {
            v[0] -= 1
            v[1] += 1
        } else {
            var nz = 1
            while (v[nz] == 0) nz++
            v[0] = v[nz] - 1
            v[nz + 1]++
            v[nz] = 0
        }
    }
}

private val n4 = [0 to -1, -1 to 0, 1 to 0, 0 to 1]
private val n8 = [-1 to -1, 0 to -1, 1 to -1, -1 to 0, 1 to 0, -1 to 1, 0 to 1, 1 to 1]

/**
 * This generates a list of the coordinates of the 4 neighbors of a cell in a 2-dimensional generic array
 */
fun <T> Array<Array<T>>.neighbors4(x: Int, y: Int): List<IntPair> =
    n4.map { [dx, dy] -> x + dx to y + dy }.filter { [cx, cy] -> cx in this[0].indices && cy in this.indices }

/**
 * This generates a list of the coordinates of the 4 neighbors of a cell in a 2-dimensional Int array
 */
fun Array<IntArray>.neighbors4(x: Int, y: Int): List<IntPair> =
    n4.map { [dx, dy] -> x + dx to y + dy }.filter { [cx, cy] -> cx in this[0].indices && cy in this.indices }

/**
 * This generates a list of the coordinates of the 4 neighbors of a cell in a 2-dimensional Char array
 */
fun Array<CharArray>.neighbors4(xy: IntPair) = neighbors4(xy.first, xy.second)

fun Array<CharArray>.neighbors4(x: Int, y: Int): List<IntPair> =
    n4.map { [dx, dy] -> x + dx to y + dy }.filter { [cx, cy] -> cx in this[0].indices && cy in this.indices }

/**
 * This generates a list of the coordinates of the 8 neighbors of a cell in a 2-dimensional generic array
 */
fun <T> Array<Array<T>>.neighbors8(x: Int, y: Int): List<IntPair> =
    n8.map { [dx, dy] -> x + dx to y + dy }.filter { [cx, cy] -> cx in this[0].indices && cy in this.indices }

/**
 * This generates a list of the coordinates of the 8 neighbors of a cell in a 2-dimensional Int array
 */
fun Array<IntArray>.neighbors8(x: Int, y: Int): List<IntPair> =
    n8.map { [dx, dy] -> x + dx to y + dy }.filter { [cx, cy] -> cx in this[0].indices && cy in this.indices }

/**
 * Breaks a list into a list of lists.  Elements which are delimiters between the lists are not included in the result
 */
fun <T> List<T>.chunkedBy(predicate: (T) -> Boolean): List<List<T>> =
    fold(mutableListOf(mutableListOf<T>())) { acc, item ->
        if (predicate(item)) {
            acc.add(mutableListOf())
        } else {
            acc.last().add(item)
        }
        acc
    }

/**
 * A map that counts occurrences of items. Can be initialized with a list, and then using inc method
 * to count additional occurrences. It uses a MutableLong class for the counts so that we can update
 * the counts w/o generating new map entries.
 */
class CountingMap<T>(
    list: List<T> = emptyList(),
    init: Int = 1,
    private val m: MutableMap<T, MutableLong> = mutableMapOf()
) : MutableMap<T, CountingMap.MutableLong> by m {
    init {
        list.forEach { set(it, init) }
    }

    data class MutableLong(var value: Long)

    fun inc(k: T, amount: Long = 1L) {
        m.getOrPut(k) { MutableLong(0L) }.value += amount
    }

    fun inc(k: T, amount: Int) {
        m.getOrPut(k) { MutableLong(0L) }.value += amount
    }

    operator fun set(k: T, amount: Long) {
        m.getOrPut(k) { MutableLong(0L) }.value = amount
    }

    operator fun set(k: T, amount: Int) {
        m.getOrPut(k) { MutableLong(0L) }.value = amount.toLong()
    }

    fun count(k: T) = m[k]?.value ?: 0L

    fun entries() = m.mapValues { it.value.value }.entries

    fun keys() = m.keys

    fun values() = m.values.map { it.value }

    override fun toString(): String {
        return entries.joinToString(", ", prefix = "[", postfix = "]") { [key, count] -> "$key: ${count.value}" }
    }
}

/**
 * greatest common divisor of 2 Int values
 */
tailrec fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

fun gcd(ints: List<Int>): Int = ints.reduce { acc, i -> gcd(acc, i) }

fun gcd(ints: IntArray): Int = ints.reduce { acc, i -> gcd(acc, i) }

/**
 * greatest common divisor of 2 Long values
 */
tailrec fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)

fun gcdL(longs: List<Long>): Long = longs.reduce { acc, i -> gcd(acc, i) }

fun gcdL(longs: LongArray): Long = longs.reduce { acc, i -> gcd(acc, i) }

/**
 * least common multiple of 2 Int values
 */
fun lcm(a: Int, b: Int): Int = a / gcd(a, b) * b

fun lcm(ints: List<Int>): Int = ints.reduce { acc, i -> lcm(acc, i) }

fun lcm(ints: IntArray): Int = ints.reduce { acc, i -> lcm(acc, i) }

/**
 * least common multiple of 2 Long values
 */
fun lcm(a: Long, b: Long): Long = a / gcd(a, b) * b

fun lcmL(longs: List<Long>): Long = longs.reduce { acc, i -> lcm(acc, i) }

fun lcmL(longs: LongArray): Long = longs.reduce { acc, i -> lcm(acc, i) }

/**
 * Manhattan distance in a 2-dimensional array is the distance between 2 points moving only horizontally or vertically
 */
fun manhattanDistance(x1: Int, y1: Int, x2: Int, y2: Int) = (x1 delta x2) + (y1 delta y2)

fun manhattanDistance(p1: Point, p2: Point) = (p1.x delta p2.x) + (p1.y delta p2.y)

fun manhattanDistance(x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int) =
    (x1 delta x2) + (y1 delta y2) + (z1 delta z2)

fun manhattanDistance(p1: IntArray, p2: IntArray) = p1.zip(p2).sumOf { it.first delta it.second }

fun manhattanDistance(x1: Long, y1: Long, x2: Long, y2: Long) = (x1 delta x2) + (y1 delta y2)

fun manhattanDistance(x1: Long, y1: Long, z1: Long, x2: Long, y2: Long, z2: Long) =
    (x1 delta x2) + (y1 delta y2) + (z1 delta z2)

fun manhattanDistance(p1: LongArray, p2: LongArray) = p1.zip(p2).sumOf { it.first delta it.second }

/**
 * The size of a power-set of a set of size n is 2.0.pow(n)
 */
fun <T> Collection<T>.powerSet(): Set<Set<T>> = powerSet(this, setOf(emptySet()))

private tailrec fun <T> powerSet(left: Collection<T>, acc: Set<Set<T>>): Set<Set<T>> {
    return if (left.isEmpty()) {
        acc
    } else {
        powerSet(left.drop(1), acc + acc.map { it + left.first() })
    }
}

/**
 * The size of a power-set of a set of size n is 2.0.pow(n)
 */
fun <T> Collection<T>.powerSetSeq(): Sequence<Set<T>> {
    return sequence {
        val powerSet = mutableSetOf<Set<T>>(emptySet())
        yield(powerSet.first())
        for (element in this@powerSetSeq) {
            val newSubsets = powerSet.map { s -> (s + element).also { yield(it) } }
            powerSet.addAll(newSubsets)
        }
    }
}

val Int.isOdd: Boolean
    get() = this % 2 != 0

val Int.isEven: Boolean
    get() = this % 2 == 0

val Long.isOdd: Boolean
    get() = this % 2L != 0L

val Long.isEven: Boolean
    get() = this % 2L == 0L

fun Int.pow(n: Int): Long {
    require(n >= 0) { "Negative exponents are not supported for integer powers." }

    if (this == 2 && n in 0..62) return 1L shl n
    if (n == 0) return 1L
    if (this == 0) return 0L
    if (this == 1) return 1L
    if (this == -1) return if (n.isEven) 1L else -1L

    if (n > 64) throw ArithmeticException("Calculation of $this.pow($n) exceeded maximum capacity of Long.MAX_VALUE")

    var base = this.toLong()
    if (n == 1) return base
    var result = 1L
    var exp = n

    try {
        while (true) {
            if (exp.isOdd) result *= base
            exp /= 2
            if (exp == 0) return result
            base *= base
        }
    } catch (_: ArithmeticException) {
        throw ArithmeticException("Calculation of $this.pow($n) exceeded maximum capacity of Long.MAX_VALUE")
    }
}

/**
 * Computes min and max of an Int collection in a single loop
 */
fun Collection<Int>.minMax(): IntArray {
    return fold(intArrayOf(Int.MAX_VALUE, Int.MIN_VALUE)) { acc, i ->
        acc[0] = minOf(acc[0], i); acc[1] = maxOf(acc[1], i); acc
    }
}

/**
 * Computes min and max of a Long collection in a single loop
 */
fun Collection<Long>.minMax(): LongArray {
    return fold(longArrayOf(Long.MAX_VALUE, Long.MIN_VALUE)) { acc, i ->
        acc[0] = minOf(acc[0], i); acc[1] = maxOf(acc[1], i); acc
    }
}

/**
 * Computes min and max of a Double collection in a single loop
 */
fun Collection<Double>.minMax(): DoubleArray {
    return fold(doubleArrayOf(Double.MAX_VALUE, Double.MIN_VALUE)) { acc, i ->
        acc[0] = minOf(acc[0], i); acc[1] = maxOf(acc[1], i); acc
    }
}

/**
 * Computes min and max of a Float collection in a single loop
 */
fun Collection<Float>.minMax(): FloatArray {
    return fold(floatArrayOf(Float.MAX_VALUE, Float.MIN_VALUE)) { acc, i ->
        acc[0] = minOf(acc[0], i); acc[1] = maxOf(acc[1], i); acc
    }
}

/**
 * Computes applying transformers over all elements of a collection of Ints, starting with a list of Ints
 */
fun Collection<Int>.multiFold(start: List<Int>, transformers: List<(Int, Int) -> Int>): List<Int> {
    require(start.size == transformers.size)
    return fold(start) { acc, i -> acc.mapIndexed { index, a -> transformers[index](i, a) } }
}

/**
 * Computes applying transformers over all elements of a collection of Ints, starting with first element of the collection
 */
fun Collection<Int>.multiReduce(vararg transformers: (Int, Int) -> Int): List<Int> {
    require(transformers.isNotEmpty()) { "transformers must not be empty" }
    if (isEmpty()) return emptyList()
    val start = first().let { f -> List(transformers.size) { f } }
    return drop(1).fold(start) { acc, i -> acc.mapIndexed { index, a -> transformers[index](i, a) } }
}

fun String.ints(withNegative: Boolean = true) = Regex(if (withNegative) """-?\d+""" else """\d+""")
    .findAll(this)
    .map { it.value.toInt() }
    .toList()

fun String.longs(withNegative: Boolean = true) = Regex(if (withNegative) """-?\d+""" else """\d+""")
    .findAll(this)
    .map { it.value.toLong() }
    .toList()

infix fun Int.delta(other: Int) = (this - other).absoluteValue
infix fun Long.delta(other: Long) = (this - other).absoluteValue

fun List<Int>.product() = reduceOrNull(Int::times) ?: 0
fun List<Long>.product() = reduceOrNull(Long::times) ?: 0

enum class Part { ONE, TWO }

private
val md: MessageDigest = MessageDigest.getInstance("MD5")

fun String.md5(): String {
    return BigInteger(1, md.digest(this.toByteArray())).toString(16).padStart(32, '0')
}

fun primes(limit: Int): List<Int> {
    val isPrime = BooleanArray(limit + 1) { true }
    val primes = mutableListOf<Int>()
    val longLimit = limit.toLong()

    for (p in 2..limit) {
        if (isPrime[p]) {
            primes.add(p)
            var i = p.toLong() * p
            while (i <= longLimit) {
                isPrime[i.toInt()] = false
                i += p
            }
        }
    }
    return primes
}
