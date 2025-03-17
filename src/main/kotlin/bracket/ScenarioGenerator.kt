package bracket

sealed interface ScenarioGenerator {
     fun getScenarios(): List<Scenario>
}

class GameScenarioGenerator(
    private val team1ScenarioGenerator: ScenarioGenerator,
    private val team2ScenarioGenerator: ScenarioGenerator,
    private val round: Int,
): ScenarioGenerator {
    override fun getScenarios(): List<Scenario> =
        calculateNewScenarios(
            team1Scenarios = team1ScenarioGenerator.getScenarios(),
            team2Scenarios = team2ScenarioGenerator.getScenarios(),
            round = round,
        )
}

class TeamScenarioGenerator(
    private val team: Team,
    private val teamSeed: Int,
): ScenarioGenerator {
    override fun getScenarios(): List<Scenario> =
        listOf(
            Scenario(
                team = team,
                teamSeed = teamSeed,
                points = 0,
                probability = 1.0,
                gameProbability = 1.0,
                pastWins = emptyList(),
            )
        )
}
