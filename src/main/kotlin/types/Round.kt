package types

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import tables.Matches
import tables.Rounds

class Round(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Round>(Rounds)

    var name by Rounds.name
    val matches by Match referrersOn Matches.round
    var running by Rounds.running

    fun asVerboseResponse(): RoundVerboseResponse {
        val matchResponses: ArrayList<MatchSerialized> = ArrayList()
        for (match in matches.sortedWith(
            compareBy({ it.time }, { it.name })
        )) {
            matchResponses.add(match.serialize())
        }
        return RoundVerboseResponse(id.value, name, running, matchResponses)
    }

    fun asResponse(): RoundResponse {
        return RoundResponse(id.value, name, running)
    }
}

@Serializable
data class RoundVerboseResponse(val id: Int, val name: String, val running: Boolean, val matches: ArrayList<MatchSerialized>)

@Serializable
data class RoundResponse(val id: Int, val name: String, val running: Boolean)