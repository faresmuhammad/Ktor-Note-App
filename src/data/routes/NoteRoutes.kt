package com.fares.train.data.routes

import com.fares.train.data.*
import com.fares.train.data.collections.Note
import com.fares.train.data.requests.AddOwnerRequest
import com.fares.train.data.requests.AddPictureRequest
import com.fares.train.data.requests.DeleteRequest
import com.fares.train.data.requests.GetPictureRequest
import com.fares.train.data.responses.PictureResponse
import com.fares.train.data.responses.SimpleResponse
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.ContentTransformationException
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.noteRoutes() {
    route("/getNotes") {
        authenticate {
            get {
                val email = call.principal<UserIdPrincipal>()!!.name

                val notes = getAllNotesForUser(email)
                call.respond(OK, notes)
            }
        }

    }
    route("/addNote") {
        authenticate {
            post {
                val request = try {
                    call.receive<Note>()
                } catch (e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@post
                }
                if (saveNote(request)) {
                    call.respond(OK)
                } else {
                    call.respond(Conflict)
                }
            }

        }
    }

    route("/addOwnerToNote") {
        authenticate {
            post {
                val request = try {
                    call.receive<AddOwnerRequest>()
                } catch (e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@post
                }
                if (!isUserExists(request.owner)) {
                    call.respond(OK, SimpleResponse(false, "This user does not exist"))
                    return@post
                }
                if (isOwnerOfNote(request.owner, request.noteId)) {
                    call.respond(
                        OK,
                        SimpleResponse(false, "This user is already an owner of this note")
                    )
                    return@post
                }
                if (addOwnerToNote(request.noteId, request.owner)) {
                    call.respond(
                        OK,
                        SimpleResponse(true, "${request.owner} can now see this note")
                    )
                } else {
                    call.respond(Conflict)
                }
            }
        }
    }

    route("/addPictureToNote") {
        authenticate {
            post {
                val request = try {
                    call.receive<AddPictureRequest>()
                } catch (e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@post
                }
                if (addPictureToNote(request.noteId, request.pictures)) {
                    call.respond(OK, SimpleResponse(true, "Picture Add to note successfully"))
                } else {
                    call.respond(BadRequest, SimpleResponse(false, "Can't add picture to note"))
                }
            }
        }
    }

    route("/getNotePicture/{id}") {
        authenticate {
            get {
                /*val request = try {
                    call.receive<GetPictureRequest>()
                } catch (e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@get
                }*/
                val noteId = call.parameters["id"]
                val note = getNoteById(noteId!!)
                if (note != null) {
                    call.respond(OK, PictureResponse(true, note.pictures))
                } else {
                    call.respond(BadRequest, PictureResponse(false, null))
                }
            }
        }
    }

    route("/deleteNote") {
        authenticate {
            post {
                val email = call.principal<UserIdPrincipal>()!!.name
                val request = try {
                    call.receive<DeleteRequest>()
                } catch (e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@post
                }
                if (deleteNoteForUser(email, request.id)) {
                    call.respond(OK)
                } else {
                    call.respond(Conflict)
                }
            }
        }
    }
}