package bracket

data class Scenario(
    val team: Team,
    val teamSeed: Int,
    val points: Int,
    val probability: Double,
    val pastWins: List<String>,
) {
    val expectedPoints: Double = points * probability
}
