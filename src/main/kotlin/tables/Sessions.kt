package tables

import org.jetbrains.exposed.dao.id.UUIDTable

object Sessions : UUIDTable("sessions", "id") {

    val account = reference("account", Accounts)
}
