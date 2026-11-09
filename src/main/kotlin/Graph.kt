import java.util.PriorityQueue
import kotlin.collections.forEach

// from https://github.com/alexhwoods/alexhwoods.com/blob/master/kotlin-algorithms/src/main/kotlin/com/alexhwoods/graphs/datastructures/Graph.kt

fun <T> List<Pair<T, T>>.getUniqueValuesFromPairs(predicate: (T) -> Boolean): Set<T> = this
    .flatMap { [a, b] -> [a, b] }
    .filter(predicate)
    .toSet()

class Graph<T>(
    val vertices: Set<T>,
    val edges: Map<T, Set<T>>,
    val weights: Map<Pair<T, T>, Int>
) {
    constructor(weights: Map<Pair<T, T>, Int>) : this(
        vertices = weights.keys.toList().getUniqueValuesFromPairs { true },
        edges = weights.keys
            .groupBy { it.first }
            .mapValues { it.value.getUniqueValuesFromPairs { x -> x != it.key } }
            .withDefault { emptySet() },
        weights = weights
    )
}

fun <T> dijkstra(graph: Graph<T>, start: T): Map<T, T?> {
    val s = mutableSetOf<T>() // a subset of vertices, for which we know the true distance

    val delta = graph.vertices.associateWith { Int.MAX_VALUE }.toMutableMap()
    delta[start] = 0

    val previous: MutableMap<T, T?> = graph.vertices.associateWith { null }.toMutableMap()

    while (s != graph.vertices) {
        val v: T = delta
            .filter { !s.contains(it.key) }
            .minBy { it.value }
            .key

        graph.edges.getValue(v).minus(s).forEach { neighbor ->
            val newPath = delta.getValue(v) + graph.weights.getValue(Pair(v, neighbor))

            if (newPath < delta.getValue(neighbor)) {
                delta[neighbor] = newPath
                previous[neighbor] = v
            }
        }

        s.add(v)
    }

    return previous
}

fun <T> toEdges(vararg list: Pair<T, T>) = list
    .fold(mutableMapOf<T, MutableSet<T>>()) { acc, i -> acc.getOrPut(i.first) { mutableSetOf() }.add(i.second); acc }
    .mapValues { it.value.toSet() }
    .toMap()

fun <T> shortestPath(start: T, end: T, vararg edges: Pair<T, T>): List<T> = shortestPath(start, end, toEdges(*edges))

fun <T> shortestPath(start: T, end: T, edges: Map<T, Set<T>>): List<T> {
    val parent = mutableMapOf<T, T>()
    val seen = mutableSetOf(start)
    val queue = ArrayDeque([start])
    queue@ while (queue.isNotEmpty()) {
        val a = queue.removeFirst()
        for (b in edges.getOrDefault(a, emptySet())) {
            parent[b] = a
            if (b == end) break@queue
            if (seen.add(b)) {
                queue.add(b)
            }
        }
    }
    return buildList {
        var t = end
        while (true) {
            add(t)
            if (t == start) break
            t = parent[t] ?: error("No path from $start to $end")
        }
    }.reversed()
}

enum class Walk { Breadth, Depth }

fun <T> dfs(start: T, next: (T) -> Iterable<T>): Sequence<IndexedValue<T>> = walk(start, next, Walk.Depth)
fun <T> bfs(start: T, next: (T) -> Iterable<T>): Sequence<IndexedValue<T>> = walk(start, next, Walk.Breadth)

// initially copied from https://github.com/ephemient/aoc2022/blob/main/kt/src/commonMain/kotlin/com/github/ephemient/aoc2022/Day12.kt
fun <T> walk(start: T, next: (T) -> Iterable<T>, walk: Walk): Sequence<IndexedValue<T>> = sequence {
    val seen = mutableSetOf(start)
    val queue = ArrayDeque([IndexedValue(0, start)])
    val enqueue = if (walk == Walk.Breadth) queue::add else queue::addFirst
    while (queue.isNotEmpty()) {
        val a = queue.removeFirst()
        yield(a)
        for (b in next(a.value)) {
            if (seen.add(b)) {
                enqueue(IndexedValue(a.index + 1, b))
            }
        }
    }
}

data class ANode<T>(val name: T, val goalHeuristic: Int)
data class AEdge<T>(val to: ANode<T>, val weight: Int)

class AStarGraph<T>(val adjacencyMap: Map<ANode<T>, List<AEdge<T>>>) {

    private data class ANodeScore<T>(val node: ANode<T>, val fScore: Int)

    fun findShortestPath(
        start: ANode<T>,
        goal: ANode<T>,
    ): List<ANode<T>>? {
        val openSet = PriorityQueue<ANodeScore<T>>(compareBy { it.fScore })
        val gScores = mutableMapOf<ANode<T>, Int>().withDefault { Int.MAX_VALUE }
        val cameFrom = mutableMapOf<ANode<T>, ANode<T>>()

        gScores[start] = 0
        openSet.add(ANodeScore(start, start.goalHeuristic))

        while (openSet.isNotEmpty()) {
            val current = openSet.poll().node
            if (current == goal) return reconstructPath(cameFrom, goal)

            adjacencyMap[current]?.forEach { edge ->
                val tentativeGScore = gScores.getValue(current) + edge.weight
                if (tentativeGScore < gScores.getValue(edge.to)) {
                    cameFrom[edge.to] = current
                    gScores[edge.to] = tentativeGScore
                    val fScore = tentativeGScore + edge.to.goalHeuristic

                    // Add to openSet if not already present with a better score
                    if (openSet.none { it.node == edge.to }) {
                        openSet.add(ANodeScore(edge.to, fScore))
                    }
                }
            }
        }
        return null // No path found
    }

    private fun reconstructPath(cameFrom: Map<ANode<T>, ANode<T>>, current: ANode<T>): List<ANode<T>> {
        val path = mutableListOf(current)
        var temp = current
        while (cameFrom.containsKey(temp)) {
            temp = cameFrom[temp]!!
            path.add(0, temp)
        }
        return path
    }
}
