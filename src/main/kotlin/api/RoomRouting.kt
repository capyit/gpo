package api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction
import types.Room
import types.RoomResponse


fun Application.configureRoomRouting() {
    routing {
            get("/api/rooms") {
                val rooms = transaction {
                    Room.all().sortedBy { it.id }.toList()
                }
                val roomResponses: ArrayList<RoomResponse> = ArrayList()
                for (room in rooms) {
                    roomResponses.add(room.asResponse())
                }
                call.respond(HttpStatusCode.OK, roomResponses)
            }

            get("/api/room/{id}") {
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                val room = transaction {
                    Room.findById(id)?.asResponse()
                }
                if (room == null) {
                    call.respond(HttpStatusCode.BadRequest, "room_does_not_exist")
                    return@get
                }
                call.respond(HttpStatusCode.OK, room)
            }

            get("/api/room/{id}/matches") {
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                val matches = transaction {
                    Room.findById(id)?.asVerboseResponse()
                }?.matches
                if (matches == null) {
                    call.respond(HttpStatusCode.BadRequest, "bracket_does_not_exist")
                    return@get
                }
                call.respond(HttpStatusCode.OK, matches)
            }
        authenticate("auth-session") {
        }
    }
}