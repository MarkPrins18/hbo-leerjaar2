package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureAuthentication() {
    val config = environment.config

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "KTOR Server"

            verifier(
                JWT
                    .require(Algorithm.HMAC256(config.property("jwt.secret").getString()))
                    .withIssuer(config.property("jwt.issuer").toString())
                    .withAudience(config.property("jwt.audience").getString())
                    .build()
            )

            validate { credential ->
                JWTPrincipal(credential.payload)
            }
        }
    }
}