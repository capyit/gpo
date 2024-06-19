package tables

import org.jetbrains.exposed.dao.id.IntIdTable

object MatchParticipants : IntIdTable("match_participants", "id") {

    val match = reference("match", Matches)
    val participant = reference("participant", Participants)

    val rank = integer("rank").default(0)
}
