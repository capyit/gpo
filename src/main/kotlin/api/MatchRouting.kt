package api

import auth.UserSession
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import tables.MatchParticipants
import types.*
import java.util.*


fun Application.configureMatchRouting() {
    routing {
            get("/api/matches") {
                val matches = transaction {
                    Match.all().with(Match::round).toList()
                }
                val matchResponses: ArrayList<MatchSerialized> = ArrayList()
                transaction {
                    for (match in matches) {
                        matchResponses.add(match.serialize())
                    }
                }
                call.respond(HttpStatusCode.OK, matchResponses)
            }

            get("/api/match/{id}") {
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                val match = transaction {
                    Match.findById(id)?.serialize()
                }
                if (match == null) {
                    call.respond(HttpStatusCode.BadRequest, "match_does_not_exist")
                    return@get
                }
                call.respond(HttpStatusCode.OK, match)
            }

        authenticate("auth-session") {
            post("api/match") {
                val match = call.receive<MatchSerialized>()
                val matchID = try {
                    Match.insertFromRequest(match)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "match_not_added: ${e.message}")
                }
                call.respond(HttpStatusCode.Created, matchID)
            }

            //Add Participant to Match
            post("api/match/{m_id}/participant/{p_id}"){
                val userSession = call.principal<UserSession>() ?: return@post
                //Session.findById(UUID.fromString(userSession.id))!!.account.scope()

                val matchId = call.parameters["m_id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                val participantId = call.parameters["p_id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")

                val match = transaction {
                    Match.findById(matchId)
                }
                if (match == null) {
                    call.respond(HttpStatusCode.BadRequest, "match_does_not_exist")
                    return@post
                }

                val participant = transaction {
                    Participant.findById(participantId)
                }
                if (participant == null) {
                    call.respond(HttpStatusCode.BadRequest, "participant_does_not_exist")
                    return@post
                }

                transaction {
                    MatchParticipant.new {
                        this.match = match
                        this.participant = participant
                    }
                }
                call.respond(HttpStatusCode.OK, "Added")
            }


            //Set Rank of Participant in Match
            put("api/match/{m_id}/participant/{p_id}/{rank}"){
                val userSession = call.principal<UserSession>() ?: return@put
                //Session.findById(UUID.fromString(userSession.id))!!.account.scope()

                val matchId = call.parameters["m_id"]?.toInt() ?: throw IllegalArgumentException("Invalid m_ID")
                val participantId = call.parameters["p_id"]?.toInt() ?: throw IllegalArgumentException("Invalid p_ID")
                val rank = call.parameters["rank"]?.toInt() ?: throw IllegalArgumentException("Invalid Rank")

                val match = transaction {
                    Match.findById(matchId)
                }
                if (match == null) {
                    call.respond(HttpStatusCode.BadRequest, "match_does_not_exist")
                    return@put
                }

                val participant = transaction {
                    Participant.findById(participantId)
                }
                if (participant == null) {
                    call.respond(HttpStatusCode.BadRequest, "participant_does_not_exist")
                    return@put
                }

                val matchParticipants = transaction {
                    MatchParticipant.find {
                        (MatchParticipants.match eq matchId) and (MatchParticipants.participant eq participantId)
                    }
                }
                transaction {
                    matchParticipants.firstOrNull()?.updateRank(rank)
                }
                call.respond(HttpStatusCode.OK, "Updated")
            }

            put("api/match/{id}") {
                val userSession = call.principal<UserSession>() ?: return@put
                //Session.findById(UUID.fromString(userSession.id))!!.account.scope()
                val match = call.receive<MatchSerialized>()
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")

                val matchDB = transaction {
                    Match.findById(id)
                }
                if (matchDB == null) {
                    call.respond(HttpStatusCode.BadRequest, "match_does_not_exist")
                    return@put
                }
                try {
                    transaction {
                        matchDB.updateFromRequest(match)
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "match_not_updated: ${e.message}")
                }
                call.respond(HttpStatusCode.OK, "Updated")
            }

            delete("api/match/{id}") {
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                try {
                    transaction {
                        Match.findById(id)!!.delete()
                    }
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "match_not_deleted")
                }
            }
        }
    }
}