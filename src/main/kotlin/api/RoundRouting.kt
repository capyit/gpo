package api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction
import types.RoundResponse
import types.Round


fun Application.configureRoundRouting() {
    routing {
            get("/api/rounds") {
                val rounds = transaction {
                    Round.all().sortedBy { it.id }.toList()
                }
                val roundResponses: ArrayList<RoundResponse> = ArrayList()
                for (round in rounds) {
                    roundResponses.add(round.asResponse())
                }
                call.respond(HttpStatusCode.OK, roundResponses)
            }

            get("/api/round/{id}") {
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                val round = transaction {
                    Round.findById(id)?.asResponse()
                }
                if (round == null) {
                    call.respond(HttpStatusCode.BadRequest, "round_does_not_exist")
                    return@get
                }
                call.respond(HttpStatusCode.OK, round)
            }


            get("/api/round/{id}/matches") {
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                val matches = transaction {
                    Round.findById(id)?.asVerboseResponse()
                }?.matches
                if (matches == null) {
                    call.respond(HttpStatusCode.BadRequest, "round_does_not_exist")
                    return@get
                }
                call.respond(HttpStatusCode.OK, matches)
            }
        authenticate("auth-session") {
            put("/api/round/{id}"){
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                val round = transaction {
                    Round.findById(id)
                }
                if (round == null) {
                    call.respond(HttpStatusCode.BadRequest, "round_does_not_exist")
                    return@put
                }
                transaction {
                    round.running = !round.running
                }
                call.respond(HttpStatusCode.OK, round.asResponse())
            }
        }
    }
}