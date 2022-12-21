package com.fileupload.plugins

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

fun Application.configureRouting() {

    routing {

        static("/uploads") {
            staticRootFolder = File("uploads")
            files(".")
        }

        post("/v1/upload") {

            val multipartData = call.receiveMultipart()

            var fileDescription = ""
            var originalFileName = ""
            var newFileName = ""
            var uploadSuccess = false

            multipartData.forEachPart { part ->
                when(part) {
                    is PartData.FormItem -> {
                        fileDescription = part.value
                    }
                    is PartData.FileItem -> {

                        originalFileName = (part.originalFileName as String)

                        if (originalFileName.isNotBlank()) {

                            val extension = originalFileName.split(".")[1]

                            newFileName = System.currentTimeMillis().toString()+"."+extension

                            val fileBytes = part.streamProvider().readBytes()

                            val file = File("uploads/$newFileName")

                            uploadSuccess = createFileUpload(file = file, fileBytes = fileBytes)
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }

            val httStatus = if (uploadSuccess) {
                HttpStatusCode.Created
            } else {
                HttpStatusCode.BadRequest
            }

            call.respondText(text = "File upload success", status = httStatus)

        }
    }
}

private fun printSystem(message: String) {
    System.err.println(message)
}

suspend fun createFileUpload(file: File, fileBytes: ByteArray): Boolean {

    var uploaded = false

    if (!file.parentFile.exists()) {
        withContext(Dispatchers.IO) {
            if (file.parentFile.mkdir()) {
                file.writeBytes(fileBytes)
                uploaded = true
            }
       }

    } else {
        file.writeBytes(fileBytes)
        uploaded = true
    }

    return uploaded
}
