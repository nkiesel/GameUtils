import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.sequences.shouldHaveSize
import io.kotest.matchers.shouldBe

val CharAreaTest by testSuite {
    test("Simple CharArea") {
        val area = CharArea(10, 10, '.')
        area[1, 1] = '*'
        val p = Point(2, 2)
        area[p] = '#'
        area.show()
        area.png()
    }

    test("CharArea with border") {
        val area = CharArea(10, 10, ' ')
        area.edges().forEach { area[it] = '#' }
        for (x in area.xRange) {
            val p = Point(x, x)
            if (area.valid(p) && p !in area.edges()) {
                area[p] = '\\'
            }
            val q = Point(x, area.xRange.last - x)
            if (area.valid(q) && q !in area.edges()) {
                area[q] = '/'
            }
        }
        area.show()
        area.png()
    }

    test("Some CharArea ops") {
        val area = CharArea(10, 10, ' ')

        area.neighbors4(Point(0, 0)) shouldHaveSize 2
        area.neighbors4(Point(0, 1)) shouldHaveSize 3
        area.neighbors4(Point(1, 1)) shouldHaveSize 4
        area.neighbors4(1, 1) shouldHaveSize 4

        area.neighbors8(Point(0, 0)) shouldHaveSize 3
        area.neighbors8(Point(0, 1)) shouldHaveSize 5
        area.neighbors8(Point(1, 1)) shouldHaveSize 8
        area.neighbors8(1, 1) shouldHaveSize 8

        area[1,1] shouldBe ' '
        area[1, 1] = '.'
        area[2, 2] = '.'
        area.tiles().filter { area[it] != ' ' } shouldHaveSize 2
        area.tiles { it != ' ' } shouldHaveSize 2
        area.first('.') shouldBe Point(1, 1)

        area.corners() shouldHaveSize 4
        area.corners() shouldContain Point(0, 0)

        area.edges() shouldHaveSize 36
    }

    test("CharArea rotation") {
        val area = CharArea(2, 3, ' ')
        area.tiles().forEach { area[it] = (it.x + it.y).digitToChar() }
        area.show()
        area.rotated().show()
    }

    test("Points") {
        val p1 = Point(1, 3)
        val p2 = p1.move(Direction.E)
        val d = p1 - p2
        d.x shouldBe -1
        d.y shouldBe 0
        p1.direction(p2) shouldBe Direction.E
    }

    test("Manhattan for area") {
        val area = CharArea(10, 10, ' ')
        area.manhattan(Point(0, 0), 1) shouldHaveSize 2
        area.manhattan(Point(0, 0), 2) shouldHaveSize 5
        area.manhattan(Point(5, 5), 2) shouldHaveSize 12
    }
}
