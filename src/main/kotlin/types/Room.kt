package types

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import tables.Matches
import tables.Rooms

class Room(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Room>(Rooms)

    var name by Rooms.name
    var floor by Rooms.floor
    val matches by Match referrersOn Matches.room


    fun asResponse(): RoomResponse {
        return RoomResponse(id.value, name, floor)
    }

    fun asVerboseResponse(): RoomVerboseResponse {
        val matchResponses: ArrayList<MatchSerialized> = ArrayList()
        for (match in matches.sortedWith(
            compareBy({ it.time }, { it.name })
        )) {
            matchResponses.add(match.serialize())
        }
        return RoomVerboseResponse(id.value, name, floor, matchResponses)
    }
}


@Serializable
data class RoomVerboseResponse(val id: Int, val name: String, val floor: Int, val matches: ArrayList<MatchSerialized>)

@Serializable
data class RoomResponse(val id: Int, val name: String, val floor: Int)