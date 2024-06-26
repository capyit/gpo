package auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import tables.Accounts
import types.Account
import types.Session
import java.util.*


/**
 * The Session Information is sent as
 * {
 *    id: "very long UUID"
 * }
 * With this class the JSON Object can be easily deserialized into a Java Class
 */
@Serializable
data class UserSession(val id: String) : Principal

/**
 * The Login Information is received as
 * {
 *    username: "Name"
 *    password: "pass"
 * }
 * With this class the JSON Object can be easily deserialized into a Java Class
 */
@Serializable
data class LoginInformation(val username: String = "0", val password: String = "0")

/**
 * Configures the Auth handlers
 * ------------------------------------------------------
 * This is what configures the handlers for requests
 * Only one Handler is configured, the Session Handler
 */
fun Application.configureAuth() {

    // Install the Session handler module
    install(Sessions) {
        // Default Session information
        cookie<UserSession>("gpo_internal") {
            cookie.path = "/"
            cookie.extensions["SameSite"] = "none"
            cookie.secure = true
            cookie.maxAgeInSeconds = 60000
        }
    }

    // Create Session handler using Session handler module
    authentication {
        session<UserSession>("auth-session") {
            // Check Session data
            validate { session ->
                // Check if session exists
                transaction {
                    Session.findById(UUID.fromString(session.id))
                } ?: return@validate null
                // If Session exists return it again
                session
            }
        }
    }
}

// With given LoginInformation, check username and password
fun checkLogin(loginInformation: LoginInformation): Account? {
    val username = loginInformation.username
    val password = loginInformation.password

    // Check DB for user
    val accounts = transaction {
        Account.find { Accounts.username.lowerCase() eq username.lowercase() }.toList()
    }

    // Check that the DB outputs contains sth.
    if (accounts.isEmpty()) {
        return null
    }

    // This should never happen, but just in case
    // Check if multiple users with the same name were given back
    if (accounts.count() > 1) {
        return null
    }

    // Define the current account
    val account = accounts.elementAt(0)

    // Check password
    if (!BCrypt.checkpw(password, account.hash)) {
        return null
    }

    return account
}

suspend fun createSession(call: ApplicationCall, loginInformation: LoginInformation) {

    // Check login
    val login = checkLogin(loginInformation) ?: return

    try {
        // Create new session
        val session = transaction {
            Session.new {
                account = login
            }
        }

        // Add Session information into call response
        call.sessions.set(UserSession(id = session.id.toString()))
        call.respond(HttpStatusCode.OK, "logged_in")
    } catch (e: Exception) {
        call.respond(HttpStatusCode.InternalServerError, "session_creation_error")
    }
}