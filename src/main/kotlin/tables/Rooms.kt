package tables

import org.jetbrains.exposed.dao.id.IntIdTable

object Rooms : IntIdTable("rooms", "id") {

    val name = varchar("name", 255)
    val floor = integer("floor")
}
