import java.util.*

data class UndirectedEdge(val from: Int, val to: Int, val weight: Int)

data class Edge(val to: Int, val weight: Int)

private data class NodeDistance(val node: Int, val distance: Int) : Comparable<NodeDistance> {
    override fun compareTo(other: NodeDistance): Int = this.distance.compareTo(other.distance)
}

class ShortestPathSolver(private val graph: Map<Int, List<Edge>>) {

    constructor(undirectedEdges: List<UndirectedEdge>) : this(constructGraph(undirectedEdges))

    companion object {
        private fun constructGraph(edges: List<UndirectedEdge>): Map<Int, List<Edge>> {
            val adjacencyMap = mutableMapOf<Int, MutableList<Edge>>()
            for ((from, to, weight) in edges) {
                adjacencyMap.getOrPut(from) { mutableListOf() }.add(Edge(to, weight))
                adjacencyMap.getOrPut(to) { mutableListOf() }.add(Edge(from, weight))
            }
            return adjacencyMap
        }
    }

    /**
     * Finds the shortest path from [start] to [end], optionally through all [requiredPoints].
     * Returns a Pair containing the total distance and the ordered list of nodes in the path.
     */
    fun shortedPath(
        start: Int,
        end: Int,
        requiredPoints: List<Int> = emptyList(),
        maxDistance: Int = Int.MAX_VALUE
    ): Pair<Int, List<Int>>? {
        val midPoints = requiredPoints.distinct().filter { it != start && it != end }
        val m = midPoints.size

        if (m == 0) {
            val [distMap, parentMap] = dijkstra(start)
            val distance = distMap[end] ?: Int.MAX_VALUE

            if (distance == Int.MAX_VALUE || distance >= maxDistance) return null

            val path = mutableListOf<Int>()
            var curr: Int? = end
            while (curr != null) {
                path.add(curr)
                curr = parentMap[curr]
            }
            path.reverse()

            if (path.isEmpty() || path.first() != start) return null
            return Pair(distance, path)
        }

        val keyNodes = (setOf(start, end) + midPoints).toList()
        val n = keyNodes.size
        val nodeToIndex = keyNodes.withIndex().associate { it.value to it.index }

        val distances = Array(n) { IntArray(n) { Int.MAX_VALUE } }
        val parentMaps = Array(n) { emptyMap<Int, Int>() }

        for (i in keyNodes.indices) {
            val [distMap, parentMap] = dijkstra(keyNodes[i])
            parentMaps[i] = parentMap
            for (j in keyNodes.indices) {
                distances[i][j] = distMap[keyNodes[j]] ?: Int.MAX_VALUE
            }
        }

        val numStates = 1 shl m
        val dp = Array(numStates) { IntArray(m) { Int.MAX_VALUE } }
        val parentState = Array(numStates) { IntArray(m) { -1 } }

        val startIndex = nodeToIndex[start]!!
        for (i in 0 until m) {
            val midNodeIndex = nodeToIndex[midPoints[i]]!!
            val d = distances[startIndex][midNodeIndex]
            if (d != Int.MAX_VALUE && d < maxDistance) {
                dp[1 shl i][i] = d
            }
        }

        for (mask in 1 until numStates) {
            for (i in 0 until m) {
                if ((mask and (1 shl i)) == 0 || dp[mask][i] == Int.MAX_VALUE) continue

                val currNodeIndex = nodeToIndex[midPoints[i]]!!
                for (j in 0 until m) {
                    if ((mask and (1 shl j)) != 0) continue

                    val nextNodeIndex = nodeToIndex[midPoints[j]]!!
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
        val endIndex = nodeToIndex[end]!!
        var minTotalDistance = maxDistance
        var lastVisitedIndex = -1

        for (i in 0 until m) {
            if (dp[finalMask][i] == Int.MAX_VALUE) continue
            val currNodeIndex = nodeToIndex[midPoints[i]]!!
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
        visitedOrder.add(end)

        var currMask = finalMask
        var currI = lastVisitedIndex
        while (currI != -1) {
            visitedOrder.add(midPoints[currI])
            val prevI = parentState[currMask][currI]
            currMask = currMask xor (1 shl currI)
            currI = prevI
        }
        visitedOrder.add(start)
        visitedOrder.reverse()

        return Pair(minTotalDistance, reconstructFullPath(visitedOrder, parentMaps, nodeToIndex))
    }

    private fun dijkstra(startNode: Int): Pair<Map<Int, Int>, Map<Int, Int>> {
        val distances = mutableMapOf<Int, Int>().withDefault { Int.MAX_VALUE }
        val parents = mutableMapOf<Int, Int>()
        val priorityQueue = PriorityQueue<NodeDistance>()

        distances[startNode] = 0
        priorityQueue.add(NodeDistance(startNode, 0))

        while (priorityQueue.isNotEmpty()) {
            val (node, distance) = priorityQueue.poll()

            if (distance > distances.getValue(node)) continue

            graph[node]?.forEach { (to, weight) ->
                val newDist = distance + weight
                if (newDist < distances.getValue(to)) {
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
        parentMaps: Array<Map<Int, Int>>,
        nodeToIndex: Map<Int, Int>
    ): List<Int> {
        val fullPath = mutableListOf<Int>()
        for (k in 0 until order.size - 1) {
            val fromNode = order[k]
            val toNode = order[k + 1]
            val fromKeyIndex = nodeToIndex[fromNode]!!

            val segment = mutableListOf<Int>()
            var curr: Int? = toNode
            while (curr != null && curr != fromNode) {
                segment.add(curr)
                curr = parentMaps[fromKeyIndex][curr]
            }
            segment.reverse()

            if (k == 0) fullPath.add(fromNode)
            fullPath.addAll(segment)
        }
        return fullPath
    }
}
