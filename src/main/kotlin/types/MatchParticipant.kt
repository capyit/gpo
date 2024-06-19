package types

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import tables.MatchParticipants

class MatchParticipant(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<MatchParticipant>(MatchParticipants)

    var match by Match referencedOn MatchParticipants.match
    var participant by Participant referencedOn MatchParticipants.participant
    var rank by MatchParticipants.rank

    fun updateRank(newRank: Int) {

        // If the Round is not ongoing, then don't update the rank
        if(!match.round.running){return}

        val nextMatch = match.getNextMatches()[rank]
        if (nextMatch != null) {
            if (rank != 0) {
                val matchparticipant = MatchParticipant.find {
                    (MatchParticipants.match eq nextMatch.id.value) and (MatchParticipants.participant eq this@MatchParticipant.participant.id.value)
                }.firstOrNull()
                if (matchparticipant != null)  {
                    matchparticipant.updateRank(-1)
                    matchparticipant.delete()
                }
            }
        }
        if(newRank>-1) {
            this.newRank(newRank)
        }
    }

    fun newRank(newRank: Int) {
        rank = newRank
        val nextMatch = match.getNextMatches()[rank]
        if (nextMatch != null) {
            MatchParticipant.new {
                this.match = nextMatch
                this.participant = this@MatchParticipant.participant
            }
        }
    }
}
