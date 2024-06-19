package tables

import org.jetbrains.exposed.dao.id.IntIdTable

object Rounds : IntIdTable("rounds", "id") {

    val name = varchar("name", 255)
    val running = bool("running")
}
