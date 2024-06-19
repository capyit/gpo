package tables

import org.jetbrains.exposed.dao.id.IntIdTable

object Participants : IntIdTable("participants", "id") {

    val name = varchar("name", 255).uniqueIndex()
    val account = reference("account", Accounts).nullable()
    val checkedIn = bool("checked_in")
    val controller = integer("controller")
    val room = integer("room")
}
