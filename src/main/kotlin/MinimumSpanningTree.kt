private class DisjointSet<T> {
    private val parent = mutableMapOf<T, T>()
    private val rank = mutableMapOf<T, Int>()

    fun find(item: T): T {
        val currentParent = parent.getOrPut(item) { item }
        if (currentParent != item) parent[item] = find(currentParent)
        return parent[item]!!
    }

    fun union(u: T, v: T): Boolean {
        val rootU = find(u)
        val rootV = find(v)
        if (rootU == rootV) return false

        val rankU = rank.getOrDefault(rootU, 0)
        val rankV = rank.getOrDefault(rootV, 0)

        if (rankU < rankV) {
            parent[rootU] = rootV
        } else if (rankU > rankV) {
            parent[rootV] = rootU
        } else {
            parent[rootV] = rootU
            rank[rootU] = rankU + 1
        }
        return true
    }
}

fun <T> minimumSpanningTree(edges: List<UndirectedEdge<T>>, vertexCount: Int): Pair<List<UndirectedEdge<T>>, Int> {
    val mst = mutableListOf<UndirectedEdge<T>>()
    var totalWeight = 0
    val dsu = DisjointSet<T>()

    for (edge in edges.sortedBy { it.weight }) {
        if (dsu.union(edge.from, edge.to)) {
            mst.add(edge)
            totalWeight += edge.weight
            if (mst.size == vertexCount - 1) break
        }
    }

    return mst to totalWeight
}
