import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

val ShortestPathSolverTest by testSuite {

    // Construct an undirected graph
    // 0 --- (2) --- 1 --- (4) --- 2
    // |              |             |
    // (6)           (1)           (2)
    // |              |             |
    // 3 --- (3) --- 4 --- (1) --- 5
    val edges = [
        UndirectedEdge(0, 1, 2),
        UndirectedEdge(0, 3, 6),
        UndirectedEdge(1, 2, 4),
        UndirectedEdge(1, 4, 1),
        UndirectedEdge(2, 5, 2),
        UndirectedEdge(3, 4, 3),
        UndirectedEdge(4, 5, 1),
    ]

    test("from 0 to 2") {
        val result = ShortestPathSolver(edges).shortedPath(0, [3, 5], 2)
        result shouldNotBe null
        if (result != null) {
            result.first shouldBe 12
            result.second shouldBe [0, 3, 4, 5, 2]
        }
    }

    test("from 0 to 0") {
        val result = ShortestPathSolver(edges).shortedPath(0, (1..5).toList(), 0)
        result shouldNotBe null
        if (result != null) {
            result.first shouldBe 18
            result.second shouldBe [0, 1, 4, 3, 4, 5, 2, 1, 0]
        }
    }
}
