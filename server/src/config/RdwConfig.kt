package config

import clients.RdwClient
import io.ktor.server.application.*

fun Application.configureRdw(): RdwClient {
    val appToken = environment.config.property("rdw.appToken").getString()
    return RdwClient(appToken)
}