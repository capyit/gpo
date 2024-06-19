package types

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import tables.Sessions
import java.util.*

class Session(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Session>(Sessions)

    var account by Account referencedOn Sessions.account
}
