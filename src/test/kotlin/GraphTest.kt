import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrowMessage
import io.kotest.matchers.shouldBe

val GraphTest by testSuite {
    test("basic test for BFS") {
        data class TNode(val name: String) {
            override fun toString() = name
        }
        data class TEdge(val a: TNode, val b: TNode)

        val n1 = TNode("one")
        val n2 = TNode("two")
        val n3 = TNode("three")
        val n4 = TNode("four")
        val edges = [TEdge(n1, n2), TEdge(n2, n3), TEdge(n3, n4), TEdge(n2, n4), TEdge(n1, n4)]
        bfs(n1) { n -> edges.filter { e -> e.a == n || e.b == n }.flatMap { [it.a, it.b] }.filter { it != n } }.forEach { println(it) }
        println()
        bfs(n1) { n -> edges.mapNotNull { e -> if (e.a == n) e.b else if (e.b == n) e.a else null }.filter { it != n } }.forEach { println(it) }
    }

    test("another basic test for BFS") {
        data class TNode(val name: String) {
            override fun toString() = name
        }

        val n1 = TNode("one")
        val n2 = TNode("two")
        val n3 = TNode("three")
        val n4 = TNode("four")
        val edges: List<Set<TNode>> = [[n1, n2], [n2, n3], [n3, n4], [n2, n4], [n1, n4]]
        bfs(n1) { n -> edges.filter { n in it }.map { e -> e.first { it != n } } }.forEach { println(it) }
    }

    test("basic test for BFS of directed graph") {
        data class TNode(val name: String, val next: Set<TNode>) {
            override fun toString() = name
        }

        val n1 = TNode("one", [])
        val n5 = TNode("five", [])
        val n2 = TNode("two", [n1, n5])
        val n3 = TNode("three", [n2])
        val n4 = TNode("four", [n1, n3])
        bfs(n4) { it.next }.forEach { println(it) }
    }

    test("basic test for DFS of directed graph") {
        data class TNode(val name: String, val next: Set<TNode>) {
            override fun toString() = name
        }

        val n1 = TNode("one", [])
        val n5 = TNode("five", [])
        val n2 = TNode("two", [n1, n5])
        val n3 = TNode("three", [n2])
        val n4 = TNode("four", [n1, n3])
        dfs(n4) { it.next }.forEach { println(it) }
    }

    test("shortest path") {
        shortestPath(
            'a',
            'e',
            toEdges('a' to 'b', 'a' to 'd', 'b' to 'c', 'c' to 'd', 'd' to 'e')
        ) shouldBe ['a', 'd', 'e']

        shortestPath(
            'a',
            'e',
            'a' to 'b', 'a' to 'd', 'b' to 'c', 'c' to 'd', 'd' to 'e'
        ) shouldBe ['a', 'd', 'e']

        shouldThrowMessage("No path from a to z") { shortestPath('a', 'z', 'a' to 'b') }
    }
}
