package types

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SizedIterable
import tables.MatchParticipants
import tables.Participants


class Participant(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Participant>(Participants)

    var name by Participants.name
    val assignedMatches: SizedIterable<Match>? by Match via MatchParticipants
    var account by Account optionalReferencedOn Participants.account
    var checkedIn by Participants.checkedIn
    var controller by Participants.controller
    var room by Participants.room

    fun asResponse(): ParticipantResponse {
        val assignedMatchIDs = ArrayList<Int>()
        assignedMatches?.forEach {
            assignedMatchIDs += it.id.value
        }
        return ParticipantResponse(id.value, name, assignedMatchIDs, checkedIn, controller, room)
    }
}


@Serializable
data class ParticipantRequest(val name: String)

@Serializable
data class ParticipantResponse(val id: Int, val name: String, val assignedMatches: List<Int>,
                               var checkedIn: Boolean, val controller: Int, val room: Int)