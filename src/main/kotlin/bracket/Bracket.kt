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
    val allScenarios = team1Scenarios.flatMap { team1Scenario ->
        team2Scenarios.flatMap { team2Scenario ->
            listOf(
                calculateNewScenario(
                    winningTeamScenario = team1Scenario,
                    losingTeamScenario = team2Scenario,
                    round = round,
                ),
                calculateNewScenario(
                    winningTeamScenario = team2Scenario,
                    losingTeamScenario = team1Scenario,
                    round = round,
                ),
            )
        }
    }.sortedByDescending(Scenario::expectedPoints)
    return if (allScenarios.size > 4) allScenarios.take((allScenarios.size * 0.4).toInt()) else allScenarios
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
    val south = buildRegion(
        listOf(
            listOf(MensTeams.alabama),
            listOf(MensTeams.arizona),
            listOf(MensTeams.baylor),
            listOf(MensTeams.virginia),
            listOf(MensTeams.sdsu),
            listOf(MensTeams.creighton),
            listOf(MensTeams.missouri),
            listOf(MensTeams.maryland),
            listOf(MensTeams.wvu),
            listOf(MensTeams.utahSt),
            listOf(MensTeams.ncSt),
            listOf(MensTeams.charleston),
            listOf(MensTeams.furman),
            listOf(MensTeams.ucsb),
            listOf(MensTeams.princeton),
            listOf(MensTeams.tamuCC),
        )
    )

    val east = buildRegion(
        listOf(
            listOf(MensTeams.purdue),
            listOf(MensTeams.marquette),
            listOf(MensTeams.kansasSt),
            listOf(MensTeams.tennessee),
            listOf(MensTeams.duke),
            listOf(MensTeams.kentucky),
            listOf(MensTeams.michiganSt),
            listOf(MensTeams.memphis),
            listOf(MensTeams.fau),
            listOf(MensTeams.usc),
            listOf(MensTeams.providence),
            listOf(MensTeams.oralRoberts),
            listOf(MensTeams.uLaLa),
            listOf(MensTeams.montanaSt),
            listOf(MensTeams.vermont),
            listOf(MensTeams.fDickinson),
        )
    )

    val midwest = buildRegion(
        listOf(
            listOf(MensTeams.houston),
            listOf(MensTeams.texas),
            listOf(MensTeams.xavier),
            listOf(MensTeams.indiana),
            listOf(MensTeams.miami),
            listOf(MensTeams.iowaState),
            listOf(MensTeams.texasAM),
            listOf(MensTeams.iowa),
            listOf(MensTeams.auburn),
            listOf(MensTeams.pennState),
            listOf(MensTeams.pitt),
            listOf(MensTeams.drake),
            listOf(MensTeams.kentState),
            listOf(MensTeams.kennState),
            listOf(MensTeams.colgate),
            listOf(MensTeams.nKentucky),
        )
    )

    val west = buildRegion(
        listOf(
            listOf(MensTeams.kansas),
            listOf(MensTeams.ucla),
            listOf(MensTeams.gonzaga),
            listOf(MensTeams.uconn),
            listOf(MensTeams.stMarys),
            listOf(MensTeams.tcu),
            listOf(MensTeams.northwestern),
            listOf(MensTeams.arkansas),
            listOf(MensTeams.illinois),
            listOf(MensTeams.boiseSt),
            listOf(MensTeams.arizonaSt),
            listOf(MensTeams.vcu),
            listOf(MensTeams.iona),
            listOf(MensTeams.grCanyon),
            listOf(MensTeams.uncAsh),
            listOf(MensTeams.howard),
        )
    )

    val bracket =
        GameScenarioGenerator(
            round = 6,
            team1ScenarioGenerator = GameScenarioGenerator(
                round = 5,
                team1ScenarioGenerator = south,
                team2ScenarioGenerator = east,
            ),
            team2ScenarioGenerator = GameScenarioGenerator(
                round = 5,
                team1ScenarioGenerator = midwest,
                team2ScenarioGenerator = west,
            )
        )
    val bracketScenarios = bracket.getScenarios()
    bracketScenarios.take(10).forEach {
        println("${it.expectedPoints} ${it.probability} ${it.points}: ${it.team.name} - ${it.pastWins}")
    }
}