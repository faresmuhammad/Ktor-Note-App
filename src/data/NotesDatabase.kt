package com.fares.train.data

import com.fares.train.data.collections.Note
import com.fares.train.data.collections.User
import com.fares.train.security.checkHashForPassword
import org.litote.kmongo.contains
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.setValue
import java.net.URI

private val client = KMongo.createClient().coroutine
private val database = client.getDatabase("NotesDatabase")
private val users = database.getCollection<User>()
private val notes = database.getCollection<Note>()


suspend fun registerUser(user: User): Boolean {
    return users.insertOne(user).wasAcknowledged()
}

suspend fun isUserExists(email: String): Boolean {
    return users.findOne(User::email eq email) != null
}

suspend fun checkPasswordForEmail(email: String, password: String): Boolean {
    val actualPassword = users.findOne(User::email eq email)?.password ?: return false
    return checkHashForPassword(password, actualPassword)
}


suspend fun updateLoginRequestCount(email: String): Boolean {
    val user = users.findOne(User::email eq email) ?: return false
    val loginCount = user.loginCount

    return users.updateOneById(user.id, setValue(User::loginCount, loginCount + 1)).wasAcknowledged()
}

suspend fun getAllNotesForUser(email: String): List<Note> {
    return notes.find(Note::owners contains email).toList()
}

suspend fun saveNote(note: Note): Boolean {
    val isNoteExists = notes.findOneById(note.id) != null
    return if (isNoteExists) {
        notes.updateOneById(note.id, note).wasAcknowledged()
    } else {
        notes.insertOne(note).wasAcknowledged()
    }
}

suspend fun deleteNoteForUser(email: String, noteId: String): Boolean {
    val note = notes.findOne(Note::id eq noteId, Note::owners contains email)
    note?.let {
        return if (it.owners.size > 1) {
            val newOwners = it.owners - email
            val updatedResult = notes.updateOne(Note::id eq noteId, setValue(Note::owners, newOwners))
            updatedResult.wasAcknowledged()
        } else {
            notes.deleteOneById(noteId).wasAcknowledged()
        }
    } ?: return false
}

suspend fun isOwnerOfNote(email: String, noteId: String): Boolean {
    val note = notes.findOneById(noteId) ?: return false
    return email in note.owners
}

suspend fun addOwnerToNote(noteId: String, email: String): Boolean {
    val owners = notes.findOneById(noteId)?.owners ?: return false
    return notes.updateOneById(noteId, setValue(Note::owners, owners + email)).wasAcknowledged()
}

suspend fun addPictureToNote(noteId: String, picture: String): Boolean {
    val pictures = notes.findOneById(noteId)?.pictures ?: return false
    return notes.updateOneById(noteId, setValue(Note::pictures, pictures + picture)).wasAcknowledged()
}

suspend fun getNoteById(noteId: String): Note? = notes.findOneById(noteId)