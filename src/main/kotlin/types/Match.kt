package types

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import tables.MatchParticipants
import tables.Matches
import tables.NextMatches
import kotlin.collections.set

class Match(id: EntityID<Int>) : IntEntity(id) {
    var name by Matches.name
    var round by Round referencedOn Matches.round
    var competitorAmount by Matches.competitorAmount

    var room by Room referencedOn Matches.room
    var time by Matches.startTime
    var ranks: ArrayList<Int> = arrayListOf()
    val matchParticipants by MatchParticipant referrersOn MatchParticipants.match

    companion object : IntEntityClass<Match>(Matches) {
        fun insertFromRequest(matchRequest: MatchSerialized): Int {
            val id = transaction {
                val newMatch = Match.new {
                    name = matchRequest.name
                    round = Round.findById(matchRequest.round)!!
                    competitorAmount = matchRequest.competitorAmount
                    room = Room.findById(matchRequest.room)!!
                    time = kotlinx.datetime.LocalTime.fromMillisecondOfDay(matchRequest.time)
                }
                newMatch.setMatchParticipants(matchRequest.participants)
                newMatch.setNextMatches(matchRequest.nextMatches)
                newMatch.setRanks()

                return@transaction newMatch.id.value
            }
            return id
        }
    }

    fun getNextMatches(): Map<Int, Match> {
        val nextMatches: MutableMap<Int, Match> = mutableMapOf()

        val nextMatchesDB = NextMatches.select {
            NextMatches.match eq id
        }
        transaction {
            nextMatchesDB.forEach {
                nextMatches[it[NextMatches.rank]] = Match.findById(it[NextMatches.nextMatch].value)!!
            }
        }
        return nextMatches
    }

    fun getNextMatchIDs(): Map<Int, Int> {
        val nextMatches: MutableMap<Int, Int> = mutableMapOf()

        val nextMatchesDB = NextMatches.select {
            NextMatches.match eq id
        }
        transaction {
            nextMatchesDB.forEach {
                nextMatches[it[NextMatches.rank]] = it[NextMatches.nextMatch].value
            }
        }
        return nextMatches
    }

    fun setMatchParticipants(matchParticipants: Map<Int, Int>) {
        for (matchParticipantID in matchParticipants.keys) {
            val matchParticipant = Participant.findById(matchParticipantID)
                ?: throw Exception("Participant $matchParticipantID does not exist.")
            MatchParticipant.new {
                match = this@Match
                participant = matchParticipant
                rank = matchParticipants[matchParticipantID]!!
            }
        }
    }

    fun setNextMatches(nextMatches: Map<Int, Int>) {
        for (nextMatchID in nextMatches.keys) {
            Match.findById(nextMatchID) ?: throw Exception("Match $nextMatchID does not exist.")
            NextMatches.insert {
                it[match] = this@Match.id.value
                it[nextMatch] = nextMatchID
                it[rank] = nextMatches[nextMatchID]!!
            }
        }
    }

    // Collects all distinct Ranks from the Participant and nextMatches list
    // Helper function
    fun setRanks() {
        val ranks = arrayListOf<Int>()
        getNextMatchIDs().keys.forEach {
            ranks.add(it)
        }
        matchParticipants.forEach {
            ranks.add(it.rank)
        }
        this.ranks = ranks.distinct().toCollection(ArrayList())
    }

    fun updateFromRequest(matchRequest: MatchSerialized) {
        transaction {

            // Update Match object
            name = matchRequest.name
            round = Round.findById(matchRequest.round)!!
            competitorAmount = matchRequest.competitorAmount
            room = Room.findById(matchRequest.room)!!
            time = kotlinx.datetime.LocalTime.fromMillisecondOfDay(matchRequest.time)

            // Update Match-Participant references
            MatchParticipants.deleteWhere {
                match eq this@Match.id.value
            }
            setMatchParticipants(matchRequest.participants)

            // Update Match-NextMatch references
            NextMatches.deleteWhere {
                match eq this@Match.id.value
            }
            setNextMatches(matchRequest.nextMatches)

            setRanks()

        }
    }

    fun verbose(): MatchVerbose {
        val participantIDs: MutableMap<Int, Int> = mutableMapOf()
        for (matchParticipant in matchParticipants) {
            participantIDs[matchParticipant.participant.id.value] = matchParticipant.rank
        }
        return MatchVerbose(
            id.value,
            name,
            ranks,
            round.asResponse(),
            competitorAmount,
            room.asResponse(),
            time.toMillisecondOfDay(),
            getNextMatchIDs(),
            participantIDs
        )
    }

    fun serialize(): MatchSerialized {
        val participantIDs: MutableMap<Int, Int> = mutableMapOf()
        for (matchParticipant in matchParticipants) {
            participantIDs[matchParticipant.participant.id.value] = matchParticipant.rank
        }
        return MatchSerialized(
            id.value,
            name,
            round.id.value,
            competitorAmount,
            room.id.value,
            time.toMillisecondOfDay(),
            getNextMatchIDs(),
            participantIDs
        )
    }
}

@Serializable
data class MatchVerbose(
    val id: Int,
    val name: String,
    val ranks: List<Int>,
    val round: RoundResponse,
    val competitorAmount: Int,
    val room: RoomResponse,
    val time: Int,
    // Key: Rank, Value: Match
    val nextMatches: Map<Int, Int>,
    // Key: Participant, Value: Rank
    val participants: Map<Int, Int>
)

@Serializable
data class MatchSerialized @OptIn(ExperimentalSerializationApi::class) constructor(
    @EncodeDefault
    val id: Int = -1,
    val name: String,
    val round: Int,
    val competitorAmount: Int,
    val room: Int,
    val time: Int,
    // Key: Rank, Value: Match
    val nextMatches: Map<Int, Int>,
    // Key: Participant, Value: Rank
    val participants: Map<Int, Int>
)