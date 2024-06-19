package types

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Table.Dual.uniqueIndex
import tables.Accounts
import tables.Participants

//TODO Website Platzierung nicht-öffentlich vor Beginn - Einstellung dafür
//TODO Möglichkeit festzufrieren
//TODO Einteilung während der Anmeldung
//TODO Eigenschaft Raum - Option Zufällig plus alle Räume für participant

//TODO Add Seite für Anmeldung mit Suchleiste nach Competitors
//TODO Es soll Name, Startnummer, Checkbox um Anmeldung zu bestätigen

class Account(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Account>(Accounts)

    var username by Accounts.username.uniqueIndex()
    var hash by Accounts.hash
    var admin by Accounts.admin
    var edit by Accounts.edit
    var email by Accounts.email
    val participant by Participant optionalReferrersOn Participants.account

    fun asResponse(): AccountResponse {
        return AccountResponse(id.value, username, hash, admin, edit, email)
    }

    fun canEdit(): Boolean {
        return admin || edit
    }

    fun isAdmin(): Boolean {
        return admin
    }

    fun scope(): String {
        return if (admin) {
            "ADMIN"
        } else if (edit) {
            "EDIT"
        } else {
            "VIEW"
        }
    }
}

@Serializable
data class AccountResponse(
    val id: Int,
    val username: String,
    val hash: String,
    val admin: Boolean,
    val edit: Boolean,
    val email: String
)
