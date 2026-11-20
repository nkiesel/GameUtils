import java.util.*

data class UndirectedEdge<T>(val from: T, val to: T, val weight: Int = 1)
data class DirectedEdge<T>(val from: T, val to: T, val weight: Int = 1)

// Fully encapsulated internal performance infrastructure
private data class Edge(val to: Int, val weight: Int)

private data class NodeDistance(val node: Int, val distance: Int) : Comparable<NodeDistance> {
    override fun compareTo(other: NodeDistance): Int = this.distance.compareTo(other.distance)
}

class ShortestPathSolver<T>(
    undirectedEdges: List<UndirectedEdge<T>> = emptyList(),
    directedEdges: List<DirectedEdge<T>> = emptyList()
) {
    // Unique list of all external node identifiers
    private val externalNodes: List<T>

    // Map for external to internal dense index lookups
    private val extToInternal: Map<T, Int>

    // Primitive performance optimized graph adjacency array
    private val graph: Array<MutableList<Edge>>
    private val numNodes: Int

    init {
        // 1. Gather all distinct nodes across both edge lists
        externalNodes = (
                undirectedEdges.flatMap { listOf(it.from, it.to) } +
                        directedEdges.flatMap { listOf(it.from, it.to) }
                ).distinct()

        numNodes = externalNodes.size
        extToInternal = externalNodes.withIndex().associate { it.value to it.index }
        graph = Array(numNodes) { mutableListOf() }

        // 2. Map and insert Undirected Edges (Symmetric / Both Ways)
        for ((from, to, weight) in undirectedEdges) {
            val internalFrom = extToInternal[from]!!
            val internalTo = extToInternal[to]!!
            graph[internalFrom].add(Edge(internalTo, weight))
            graph[internalTo].add(Edge(internalFrom, weight))
        }

        // 3. Map and insert Directed Edges (Asymmetric / One Way)
        for ((from, to, weight) in directedEdges) {
            val internalFrom = extToInternal[from]!!
            val internalTo = extToInternal[to]!!
            graph[internalFrom].add(Edge(internalTo, weight))
        }
    }

    /**
     * Finds the shortest path from [start] to [end], optionally through all [requiredPoints].
     * Compatible with mixed directed/undirected topologies.
     * Returns a Pair containing the total distance and the ordered list of external nodes.
     */
    fun shortestPath(
        start: T,
        end: T,
        requiredPoints: List<T> = emptyList(),
        maxDistance: Int = Int.MAX_VALUE
    ): Pair<Int, List<T>>? {
        val internalStart = extToInternal[start] ?: return null
        val internalEnd = extToInternal[end] ?: return null

        val internalMidPoints = requiredPoints
            .mapNotNull { extToInternal[it] }
            .distinct()
            .filter { it != internalStart && it != internalEnd }

        val m = internalMidPoints.size

        if (requiredPoints.distinct().filter { it != start && it != end }.size != m) return null

        if (m == 0) {
            val (distArray, parentArray) = dijkstra(internalStart)
            val distance = distArray[internalEnd]

            if (distance == Int.MAX_VALUE || distance >= maxDistance) return null

            val path = mutableListOf<T>()
            var curr = internalEnd
            while (curr != -1) {
                path.add(externalNodes[curr])
                curr = parentArray[curr]
            }
            path.reverse()

            if (path.isEmpty() || path.first() != start) return null
            return Pair(distance, path)
        }

        val keyNodes = (setOf(internalStart, internalEnd) + internalMidPoints).toList()
        val n = keyNodes.size
        val nodeToIndex = keyNodes.withIndex().associate { it.value to it.index }

        val distances = Array(n) { IntArray(n) { Int.MAX_VALUE } }
        val parentArrays = Array(n) { IntArray(0) }

        for (i in keyNodes.indices) {
            val (distArray, parentArray) = dijkstra(keyNodes[i])
            parentArrays[i] = parentArray
            for (j in keyNodes.indices) {
                distances[i][j] = distArray[keyNodes[j]]
            }
        }

        val numStates = 1 shl m
        val dp = Array(numStates) { IntArray(m) { Int.MAX_VALUE } }
        val parentState = Array(numStates) { IntArray(m) { -1 } }

        val startIndex = nodeToIndex[internalStart]!!
        for (i in 0 until m) {
            val midNodeIndex = nodeToIndex[internalMidPoints[i]]!!
            val d = distances[startIndex][midNodeIndex]
            if (d != Int.MAX_VALUE && d < maxDistance) {
                dp[1 shl i][i] = d
            }
        }

        for (mask in 1 until numStates) {
            for (i in 0 until m) {
                if ((mask and (1 shl i)) == 0 || dp[mask][i] == Int.MAX_VALUE) continue

                val currNodeIndex = nodeToIndex[internalMidPoints[i]]!!
                for (j in 0 until m) {
                    if ((mask and (1 shl j)) != 0) continue

                    val nextNodeIndex = nodeToIndex[internalMidPoints[j]]!!
                    val edgeWeight = distances[currNodeIndex][nextNodeIndex]
                    if (edgeWeight == Int.MAX_VALUE) continue

                    val newDist = dp[mask][i] + edgeWeight
                    if (newDist < maxDistance && newDist < dp[mask or (1 shl j)][j]) {
                        val nextMask = mask or (1 shl j)
                        dp[nextMask][j] = newDist
                        parentState[nextMask][j] = i
                    }
                }
            }
        }

        val finalMask = numStates - 1
        val endIndex = nodeToIndex[internalEnd]!!
        var minTotalDistance = maxDistance
        var lastVisitedIndex = -1

        for (i in 0 until m) {
            if (dp[finalMask][i] == Int.MAX_VALUE) continue
            val currNodeIndex = nodeToIndex[internalMidPoints[i]]!!
            val toEndDist = distances[currNodeIndex][endIndex]
            if (toEndDist != Int.MAX_VALUE) {
                val total = dp[finalMask][i] + toEndDist
                if (total < minTotalDistance) {
                    minTotalDistance = total
                    lastVisitedIndex = i
                }
            }
        }

        if (minTotalDistance >= maxDistance) return null

        val visitedOrder = mutableListOf<Int>()
        visitedOrder.add(internalEnd)

        var currMask = finalMask
        var currI = lastVisitedIndex
        while (currI != -1) {
            visitedOrder.add(internalMidPoints[currI])
            val prevI = parentState[currMask][currI]
            currMask = currMask xor (1 shl currI)
            currI = prevI
        }
        visitedOrder.add(internalStart)
        visitedOrder.reverse()

        val internalFullPath = reconstructFullPath(visitedOrder, parentArrays, nodeToIndex)

        return Pair(minTotalDistance, internalFullPath.map { externalNodes[it] })
    }

    private fun dijkstra(startNode: Int): Pair<IntArray, IntArray> {
        val distances = IntArray(numNodes) { Int.MAX_VALUE }
        val parents = IntArray(numNodes) { -1 }
        val priorityQueue = PriorityQueue<NodeDistance>()

        distances[startNode] = 0
        priorityQueue.add(NodeDistance(startNode, 0))

        while (priorityQueue.isNotEmpty()) {
            val (node, distance) = priorityQueue.poll()

            if (distance > distances[node]) continue

            graph[node].forEach { (to, weight) ->
                val newDist = distance + weight
                if (newDist < distances[to]) {
                    distances[to] = newDist
                    parents[to] = node
                    priorityQueue.add(NodeDistance(to, newDist))
                }
            }
        }
        return Pair(distances, parents)
    }

    private fun reconstructFullPath(
        order: List<Int>,
        parentArrays: Array<IntArray>,
        nodeToIndex: Map<Int, Int>
    ): List<Int> {
        val fullPath = mutableListOf<Int>()
        for (k in 0 until order.size - 1) {
            val fromNode = order[k]
            val toNode = order[k + 1]
            val fromKeyIndex = nodeToIndex[fromNode]!!

            val segment = mutableListOf<Int>()
            var curr = toNode
            while (curr != -1 && curr != fromNode) {
                segment.add(curr)
                curr = parentArrays[fromKeyIndex][curr]
            }
            segment.reverse()

            if (k == 0) fullPath.add(fromNode)
            fullPath.addAll(segment)
        }
        return fullPath
    }
}
