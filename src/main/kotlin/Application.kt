import api.configureRoundRouting
import api.configureMatchRouting
import api.configureParticipantRouting
import api.configureRoomRouting
import auth.configureAuth
import auth.configureAuthRouting
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.postgresql.ds.PGSimpleDataSource
import org.slf4j.LoggerFactory


// Current dev version
// This is relevant when there is an update to the database scheme
// -> Every change to database scheme also requires an increase in the version number
const val VERSION = "0.0.3"

//TODO Starttime API für Browser durch Turnierleitung


// Define environment for webserver
val environment = applicationEngineEnvironment {

    // Start Logger
    // Not in use currently, should be changed
    log = LoggerFactory.getLogger("ktor.application")

    // Start connection to outside world
    connector {
        port = 8082
        host = "0.0.0.0"
    }
    // Start this app
    module(Application::module)
}

fun main() {
    // Start webserver with settings and startup sequences above
    embeddedServer(Netty, environment).start(wait = true)
}

// App starting point
fun Application.module() {

    // Configure the ability to use JSON for REST APIs
    install(ContentNegotiation) {
        json()
    }

    install(CORS){
        anyHost()
    }

    configureDB()

    /**
     * Configures the Auth handlers (See utils.Authentication)
     * ------------------------------------------------------
     * This is what configures the handlers for requests
     * Only one Handler is configured, the Session Handler
     * Also includes functions to check login information
     * and to create the sessions.
     */
    configureAuth()

    /**
     * Configures the Auth API (See api.AuthRouting)
     * ------------------------------------------------------
     * Creates two routes
     * /api/login - Send login information and get new session back
     * /api/checklogin - Check if session works
     */
    configureAuthRouting()

    // Configure the routing for the API for Matches (See api.MatchRouting)
    configureMatchRouting()
    configureRoundRouting()
    configureRoomRouting()
    configureParticipantRouting()
}

fun configureDB() {

    val url =  System.getenv("DB_URL")
    val user = System.getenv("DB_USER")
    val password = System.getenv("DB_PASSWORD")

    val source = PGSimpleDataSource()
    source.serverNames = arrayOf<String?>(
        url
    )
    source.databaseName = "gpo"
    source.user = user
    source.password = password


    val dbConfig = DatabaseConfig {
        keepLoadedReferencesOutOfTransaction = true
    }
    Database.connect(source, databaseConfig = dbConfig)
}
