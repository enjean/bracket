package bracket

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BracketKtTest {
    @Test
    fun `evaluate matchup`() {
        val team1Scenario = TeamScenarioGenerator(MensTeams2024.auburn, 4).getScenarios().single()
        val team2Scenario = TeamScenarioGenerator(MensTeams2024.alabama, 4).getScenarios().single()
        val scenario = calculateNewScenario(team1Scenario, team2Scenario, 5)
        println(scenario)
    }
}
