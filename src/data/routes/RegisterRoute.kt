package com.fares.train.data.routes

import com.fares.train.data.collections.User
import com.fares.train.data.isUserExists
import com.fares.train.data.registerUser
import com.fares.train.data.requests.AccountRequest
import com.fares.train.data.responses.SimpleResponse
import com.fares.train.security.getHashWithSalt
import io.ktor.application.call
import io.ktor.features.ContentTransformationException
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*

fun Route.registerRoute() {
    route("/register") {
        post {
            val request = try {
                call.receive<AccountRequest>()
            } catch (e: ContentTransformationException) {
                call.respond(BadRequest)
                return@post
            }
            val isUserExist = isUserExists(request.email)
            if (!isUserExist) {
                val validUser = User(
                    request.email.trim(),
                    getHashWithSalt(request.password.trim())
                )
                if (registerUser(validUser)) {
                    call.respond(OK, SimpleResponse(true, "Successfully User created"))
                } else {
                    call.respond(OK, SimpleResponse(false, "Unknown Error occurred"))
                }
            } else {
                call.respond(OK, SimpleResponse(false, "A User with that email already exists"))
            }
        }
    }
}