package tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.time

object Matches : IntIdTable("matches", "id") {

    val name = varchar("name", 255)
    val round = reference("round", Rounds)
    val competitorAmount = integer("competitor_amount")
    val room = reference("room", Rooms)
    val startTime = time("start_time")
    // staff nullable
}
