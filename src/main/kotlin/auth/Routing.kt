package auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureAuthRouting() {
    routing {
        post("/api/login") {
            // Receive call information
            val loginInformation = call.receive<LoginInformation>()

            // Creates Session, if accounts exists and password matches
            // Automatically responds with session
            createSession(call, loginInformation)
        }

        authenticate("auth-session") {
            get("/api/checklogin") {
                call.respond(HttpStatusCode.OK, "logged_in")
            }
        }
    }
}