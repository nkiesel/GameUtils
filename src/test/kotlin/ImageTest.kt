import de.infix.testBalloon.framework.core.testSuite

val ImageTest  by testSuite {
    test("show sample2 from 2023 Day 10") {
        CharArea(
            """
                ...........
                .S-------7.
                .|F-----7|.
                .||.....||.
                .||.....||.
                .|L-7.F-J|.
                .|..|.|..|.
                .L--J.L--J.
                ...........
                """
        ).png()
    }

    test("show sample3 from 2023 Day 10") {
        CharArea(
            """
                .F----7F7F7F7F-7....
                .|F--7||||||||FJ....
                .||.FJ||||||||L7....
                FJL7L7LJLJ||LJ.L-7..
                L--J.L7...LJS7F-7L7.
                ....F-J..F7FJ|L7L7L7
                ....L7.F7||L7|.L7L7|
                .....|FJLJ|FJ|F7|.LJ
                ....FJL-7.||.||||...
                ....L---J.LJ.LJLJ...
                """
        ).png(Tiles.PATH)
    }

    test("show sample4 from 2023 Day 10") {
        CharArea(
            """
                FF7FSF7F7F7F7F7F---7
                L|LJ||||||||||||F--J
                FL-7LJLJ||||||LJL-77
                F--JF--7||LJLJ7F7FJ-
                L---JF-JLJ.||-FJLJJ7
                |F|F-JF---7F7-L7L|7|
                |FFJF7L7F-JF7|JL---7
                7-L-JL7||F7|L7F-7F7|
                L.L7LFJ|||||FJL7||LJ
                L7JLJL-JLJLJL--JLJ.L
                """
        ).png(Tiles.PATH)
    }

    test("show digits") {
        CharArea(
            """
                .........
                ..1.2.3..
                .4.5.6.7.
                ..8.9.0..
                ..--+--..
                """
        ).png(Tiles.DIGIT)
    }
}
