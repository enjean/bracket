package bracket

import kotlin.math.pow

// Round 0 = Play-In
// Round 1 = Round of 64
// Round 2 = Round of 32
// Round 3 = Sweet 16
// Round 4 = Elite Eight
// Round 5 = Final Four
// Round 6 = Championship Game

val pointsForWin = mapOf(
    0 to 0,
    1 to 1,
    2 to 2,
    3 to 4,
    4 to 8,
    5 to 16,
    6 to 32,
)


fun roundSupportsUpsetPoints(round: Int) = round in 1..4

fun calculateNewScenarios(
    team1Scenarios: List<Scenario>,
    team2Scenarios: List<Scenario>,
    round: Int
): List<Scenario> {
//    val allScenarios = team1Scenarios.flatMap { team1Scenario ->
//        team2Scenarios.flatMap { team2Scenario ->
//            listOf(
//                calculateNewScenario(
//                    winningTeamScenario = team1Scenario,
//                    losingTeamScenario = team2Scenario,
//                    round = round,
//                ),
//                calculateNewScenario(
//                    winningTeamScenario = team2Scenario,
//                    losingTeamScenario = team1Scenario,
//                    round = round,
//                ),
//            )
//        }
//    }.sortedByDescending(Scenario::expectedPoints)
//    return if (allScenarios.size > 4) allScenarios.take((allScenarios.size * 0.4).toInt()) else allScenarios
    val team1Scenario = team1Scenarios.single()
    val team2Scenario = team2Scenarios.single()
    val bothScenarios = listOf(
        calculateNewScenario(
            winningTeamScenario = team1Scenario,
            losingTeamScenario = team2Scenario,
            round = round,
        ),
        calculateNewScenario(
            winningTeamScenario = team2Scenario,
            losingTeamScenario = team1Scenario,
            round = round,
        )
    ).sortedByDescending(Scenario::gameProbability)
//    val singleScenario = bothScenarios.first()
    val scenario1 = bothScenarios.first()
    val singleScenario = if (scenario1.gameProbability > 0.67) scenario1 else bothScenarios.last()
    return listOf(singleScenario)
}

fun calculateNewScenario(
    winningTeamScenario: Scenario,
    losingTeamScenario: Scenario,
    round: Int
): Scenario {
    val winProbability = calculateWinProbability(team1 = winningTeamScenario.team, team2 = losingTeamScenario.team)

    val seedDifference = winningTeamScenario.teamSeed - losingTeamScenario.teamSeed
    val upsetBonus = if (roundSupportsUpsetPoints(round) && seedDifference > 0) seedDifference else 0

    val pointsForWin = pointsForWin[round]!! + upsetBonus

    return winningTeamScenario.copy(
        points = winningTeamScenario.points + losingTeamScenario.points + pointsForWin,
        probability = winningTeamScenario.probability * losingTeamScenario.probability * winProbability,
        gameProbability = winProbability,
        pastWins = winningTeamScenario.pastWins + losingTeamScenario.pastWins + "${winningTeamScenario.team.name} def ${losingTeamScenario.team.name} - $winProbability $pointsForWin"
    )
}

// Based on https://fivethirtyeight.com/features/how-our-march-madness-predictions-work-2/
fun calculateWinProbability(
    team1: Team,
    team2: Team,
): Double =
    1.0 / (1.0 + 10.0.pow(-1.0 * (team1.powerRating - team2.powerRating) * 30.464 / 400.0))

fun getScenarioForSeed(
    teams: List<List<Team>>,
    seed: Int
): ScenarioGenerator {
    val teamsForSeed = teams[seed - 1]
    return if (teamsForSeed.size == 1) {
        TeamScenarioGenerator(teamsForSeed.single(), seed)
    } else {
        require(teamsForSeed.size == 2)
        GameScenarioGenerator(
            round = 0,
            team1ScenarioGenerator = TeamScenarioGenerator(teamsForSeed[0], seed),
            team2ScenarioGenerator = TeamScenarioGenerator(teamsForSeed[1], seed),
        )
    }
}

fun buildRegion(
    teams: List<List<Team>>
): ScenarioGenerator =
    GameScenarioGenerator(
        round = 4,
        team1ScenarioGenerator = GameScenarioGenerator(
            round = 3,
            team1ScenarioGenerator = GameScenarioGenerator(
                round = 2,
                team1ScenarioGenerator = GameScenarioGenerator(
                    round = 1,
                    team1ScenarioGenerator = getScenarioForSeed(teams, 1),
                    team2ScenarioGenerator = getScenarioForSeed(teams, 16),
                ),
                team2ScenarioGenerator = GameScenarioGenerator(
                    round = 1,
                    team1ScenarioGenerator = getScenarioForSeed(teams, 8),
                    team2ScenarioGenerator = getScenarioForSeed(teams, 9)
                ),
            ),
            team2ScenarioGenerator = GameScenarioGenerator(
                round = 2,
                team1ScenarioGenerator = GameScenarioGenerator(
                    round = 1,
                    team1ScenarioGenerator = getScenarioForSeed(teams, 5),
                    team2ScenarioGenerator = getScenarioForSeed(teams, 12),
                ),
                team2ScenarioGenerator = GameScenarioGenerator(
                    round = 1,
                    team1ScenarioGenerator = getScenarioForSeed(teams, 4),
                    team2ScenarioGenerator = getScenarioForSeed(teams, 13),
                ),
            )
        ),
        team2ScenarioGenerator = GameScenarioGenerator(
            round = 3,
            team1ScenarioGenerator = GameScenarioGenerator(
                round = 2,
                team1ScenarioGenerator = GameScenarioGenerator(
                    round = 1,
                    team1ScenarioGenerator = getScenarioForSeed(teams, 6),
                    team2ScenarioGenerator = getScenarioForSeed(teams, 11),
                ),

                team2ScenarioGenerator = GameScenarioGenerator(
                    round = 1,
                    team1ScenarioGenerator = getScenarioForSeed(teams, 3),
                    team2ScenarioGenerator = getScenarioForSeed(teams, 14),
                ),
            ),
            team2ScenarioGenerator = GameScenarioGenerator(
                round = 2,
                team1ScenarioGenerator = GameScenarioGenerator(
                    round = 1,
                    team1ScenarioGenerator = getScenarioForSeed(teams, 7),
                    team2ScenarioGenerator = getScenarioForSeed(teams, 10),
                ),
                team2ScenarioGenerator = GameScenarioGenerator(
                    round = 1,
                    team1ScenarioGenerator = getScenarioForSeed(teams, 2),
                    team2ScenarioGenerator = getScenarioForSeed(teams, 15),
                ),
            )
        )
    )

fun main() {
//    val south = buildRegion(
//        listOf(
//            listOf(MensTeams2024.houston),
//            listOf(MensTeams2024.marquette),
//            listOf(MensTeams2024.kentucky),
//            listOf(MensTeams2024.duke),
//            listOf(MensTeams2024.wisconsin),
//            listOf(MensTeams2024.texasTech),
//            listOf(MensTeams2024.florida),
//            listOf(MensTeams2024.nebraska),
//            listOf(MensTeams2024.tamu),
//            listOf(MensTeams2024.colorado),
//            listOf(MensTeams2024.ncState),
//            listOf(MensTeams2024.jmu),
//            listOf(MensTeams2024.vermont),
//            listOf(MensTeams2024.oakland),
//            listOf(MensTeams2024.westernKY),
//            listOf(MensTeams2024.longwood),
//        )
//    )
//
//    val east = buildRegion(
//        listOf(
//            listOf(MensTeams2024.connecticut),
//            listOf(MensTeams2024.iowaSt),
//            listOf(MensTeams2024.illinois),
//            listOf(MensTeams2024.auburn),
//            listOf(MensTeams2024.sanDiegoSt),
//            listOf(MensTeams2024.byu),
//            listOf(MensTeams2024.washSt),
//            listOf(MensTeams2024.fau),
//            listOf(MensTeams2024.northwestern),
//            listOf(MensTeams2024.drake),
//            listOf(MensTeams2024.duquesne),
//            listOf(MensTeams2024.uab),
//            listOf(MensTeams2024.yale),
//            listOf(MensTeams2024.moreheadSt),
//            listOf(MensTeams2024.southDakotaSt),
//            listOf(MensTeams2024.stetson),
//        )
//    )
//
//    val midwest = buildRegion(
//        listOf(
//            listOf(MensTeams2024.purdue),
//            listOf(MensTeams2024.tennessee),
//            listOf(MensTeams2024.creighton),
//            listOf(MensTeams2024.kansas),
//            listOf(MensTeams2024.gonzaga),
//            listOf(MensTeams2024.southCarolina),
//            listOf(MensTeams2024.texas),
//            listOf(MensTeams2024.utahSt),
//            listOf(MensTeams2024.tcu),
//            listOf(MensTeams2024.coloradoSt),
//            listOf(MensTeams2024.oregon),
//            listOf(MensTeams2024.mcNeeseSt),
//            listOf(MensTeams2024.samford),
//            listOf(MensTeams2024.akron),
//            listOf(MensTeams2024.stPeters),
//            listOf(MensTeams2024.grambling),
//        )
//    )
//
//    val west = buildRegion(
//        listOf(
//            listOf(MensTeams2024.northCarolina),
//            listOf(MensTeams2024.arizona),
//            listOf(MensTeams2024.baylor),
//            listOf(MensTeams2024.alabama),
//            listOf(MensTeams2024.saintMarys),
//            listOf(MensTeams2024.clemson),
//            listOf(MensTeams2024.dayton),
//            listOf(MensTeams2024.missSt),
//            listOf(MensTeams2024.michSt),
//            listOf(MensTeams2024.nevada),
//            listOf(MensTeams2024.newMexico),
//            listOf(MensTeams2024.grandCanyon),
//            listOf(MensTeams2024.charleston),
//            listOf(MensTeams2024.colgate),
//            listOf(MensTeams2024.longBeachSt),
//            listOf(MensTeams2024.wagner),
//        )
//    )

    val albany1 = buildRegion(
        listOf(
            listOf(WomensTeams2024.southCarolina),
            listOf(WomensTeams2024.notreDame),
            listOf(WomensTeams2024.oregonSt),
            listOf(WomensTeams2024.indiana),
            listOf(WomensTeams2024.oklahoma),
            listOf(WomensTeams2024.nebraska),
            listOf(WomensTeams2024.oleMiss),
            listOf(WomensTeams2024.northCarolina),
            listOf(WomensTeams2024.michSt),
            listOf(WomensTeams2024.marquette),
            listOf(WomensTeams2024.tamu),
            listOf(WomensTeams2024.fgcu),
            listOf(WomensTeams2024.fairfield),
            listOf(WomensTeams2024.easternWashington),
            listOf(WomensTeams2024.kentSt),
            listOf(WomensTeams2024.presbyterian),
        )
    )

    val portland4 = buildRegion(
        listOf(
            listOf(WomensTeams2024.texas),
            listOf(WomensTeams2024.stanford),
            listOf(WomensTeams2024.ncState),
            listOf(WomensTeams2024.gonzaga),
            listOf(WomensTeams2024.utah),
            listOf(WomensTeams2024.tennessee),
            listOf(WomensTeams2024.iowaSt),
            listOf(WomensTeams2024.alabama),
            listOf(WomensTeams2024.floridaSt),
            listOf(WomensTeams2024.maryland),
            listOf(WomensTeams2024.greenBay),
            listOf(WomensTeams2024.southDakotaSt),
            listOf(WomensTeams2024.ucIrvine),
            listOf(WomensTeams2024.chattanooga),
            listOf(WomensTeams2024.norfolkSt),
            listOf(WomensTeams2024.drexel),
        )
    )

    val albany2 = buildRegion(
        listOf(
            listOf(WomensTeams2024.iowa),
            listOf(WomensTeams2024.ucla),
            listOf(WomensTeams2024.lsu),
            listOf(WomensTeams2024.kansasSt),
            listOf(WomensTeams2024.colorado),
            listOf(WomensTeams2024.louisville),
            listOf(WomensTeams2024.creighton),
            listOf(WomensTeams2024.westVA),
            listOf(WomensTeams2024.princeton),
            listOf(WomensTeams2024.unlv),
            listOf(WomensTeams2024.middleTennessee),
            listOf(WomensTeams2024.drake),
            listOf(WomensTeams2024.portland),
            listOf(WomensTeams2024.rice),
            listOf(WomensTeams2024.californiaBaptist),
            listOf(WomensTeams2024.holyCross),
        )
    )

    val portland3 = buildRegion(
        listOf(
            listOf(WomensTeams2024.usc),
            listOf(WomensTeams2024.ohioSt),
            listOf(WomensTeams2024.uconn),
            listOf(WomensTeams2024.virginiaTech),
            listOf(WomensTeams2024.baylor),
            listOf(WomensTeams2024.syracuse),
            listOf(WomensTeams2024.duke),
            listOf(WomensTeams2024.kansas),
            listOf(WomensTeams2024.michigan),
            listOf(WomensTeams2024.richmond),
            listOf(WomensTeams2024.auburn),
            listOf(WomensTeams2024.vanderbilt),
            listOf(WomensTeams2024.marshall),
            listOf(WomensTeams2024.jacksonSt),
            listOf(WomensTeams2024.maine),
            listOf(WomensTeams2024.tamuCC),
        )
    )

    val bracket =
        GameScenarioGenerator(
            round = 6,
            team1ScenarioGenerator = GameScenarioGenerator(
                round = 5,
                team1ScenarioGenerator = albany1,
                team2ScenarioGenerator = portland4,
            ),
            team2ScenarioGenerator = GameScenarioGenerator(
                round = 5,
                team1ScenarioGenerator = albany2,
                team2ScenarioGenerator = portland3,
            )
        )
    val bracketScenarios = bracket.getScenarios()
    bracketScenarios.take(10).forEach {
        println("${it.expectedPoints} ${it.probability} ${it.points}: ${it.team.name} - ${it.pastWins}")
    }
}