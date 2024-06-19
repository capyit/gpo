package api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import tables.Participants
import types.*


fun Application.configureParticipantRouting() {
    routing {
            get("/api/participants") {
                val participants = transaction {
                    Participant.all().sortedBy { it.id }.toList()
                }
                val participantResponses: ArrayList<ParticipantResponse> = ArrayList()
                for (participant in participants) {
                    participantResponses.add(participant.asResponse())
                }
                call.respond(HttpStatusCode.OK, participantResponses)
            }

            get("/api/participants/check") {
                val participants = transaction {
                    Participant.find{
                        Participants.checkedIn eq false
                    }.sortedBy { it.id }.toList()
                }
                val participantResponses: ArrayList<ParticipantResponse> = ArrayList()
                for (participant in participants) {
                    participantResponses.add(participant.asResponse())
                }
                call.respond(HttpStatusCode.OK, participantResponses)
            }



        get("/api/participants/checkin") {
            val participants = transaction {
                Participant.find{
                    Participants.checkedIn eq true
                }.sortedBy { it.id }.toList()
            }
            val participantResponses: ArrayList<ParticipantResponse> = ArrayList()
            for (participant in participants) {
                participantResponses.add(participant.asResponse())
            }
            call.respond(HttpStatusCode.OK, participantResponses)
        }

            get("/api/participant/{id}") {
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                val participant = transaction {
                    Participant.findById(id)?.asResponse()
                }
                if (participant == null) {
                    call.respond(HttpStatusCode.BadRequest, "participant_does_not_exist")
                    return@get
                }
                call.respond(HttpStatusCode.OK, participant)
            }

            get("/api/participant/{id}/matches") {
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                val matches = transaction {
                    Participant.findById(id)
                }?.assignedMatches
                if (matches == null) {
                    call.respond(HttpStatusCode.BadRequest, "participant_does_not_exist")
                    return@get
                }
                call.respond(HttpStatusCode.OK, matches)
            }

        authenticate("auth-session") {
            put("/api/participant/{id}") {
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                val participant = transaction {
                    Participant.findById(id)
                }
                if (participant == null) {
                    call.respond(HttpStatusCode.BadRequest, "participant_does_not_exist")
                    return@put
                }
                transaction {
                    participant.checkedIn = true
                }

                val matchId = transaction {
                    Round.findById(1)!!.asVerboseResponse().matches.minByOrNull { (it).participants.size }!!.id
                }

                val match = transaction {
                    Match.findById(matchId)
                }

                transaction {
                    MatchParticipant.new {
                        this.match = match!!
                        this.participant = participant
                    }
                }

                call.respond(HttpStatusCode.OK, participant.checkedIn)
            }


            post("api/participant") {
                val participant = call.receive<ParticipantRequest>()
                val participantID = try {
                    Participant.new {
                        name = participant.name
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "participant_not_added: ${e.message}")
                }
                call.respond(HttpStatusCode.Created, participantID)
            }
        }
    }
}