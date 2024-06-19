package tables

import org.jetbrains.exposed.dao.id.IntIdTable

object NextMatches : IntIdTable("next_matches", "id") {

    val match = reference("match", Matches)
    val nextMatch = reference("next_match", Matches)

    val rank = integer("rank")
}
