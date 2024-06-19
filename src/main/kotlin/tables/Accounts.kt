package tables

import org.jetbrains.exposed.dao.id.IntIdTable

object Accounts : IntIdTable("accounts", "id") {

    val username = varchar("username", 255).uniqueIndex()
    val hash = varchar("hash", 255)
    val admin = bool("admin").default(false)
    val edit = bool("edit").default(false)
    val email = varchar("email", 255)
}
