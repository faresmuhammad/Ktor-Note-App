package com.fares.train.data.routes

import com.fares.train.data.checkPasswordForEmail
import com.fares.train.data.requests.AccountRequest
import com.fares.train.data.responses.SimpleResponse
import com.fares.train.data.updateLoginRequestCount
import io.ktor.application.*
import io.ktor.features.ContentTransformationException
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.loginRoute() {
    route("/login") {
        post {
            val request = try {
                call.receive<AccountRequest>()
            } catch (e: ContentTransformationException) {
                call.respond(BadRequest)
                return@post
            }
            val isPasswordCorrect = checkPasswordForEmail(request.email, request.password)
            if (isPasswordCorrect) {
                if (updateLoginRequestCount(request.email)) {
                    call.respond(
                        OK,
                        SimpleResponse(true, "You now logged in")
                    )
                }

            } else {
                call.respond(OK, SimpleResponse(false, "Your Email or Password is incorrect"))
            }
        }
    }
}