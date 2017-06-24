package com.tuule.blues

import java.io.InputStream
import java.io.OutputStream

/**
 * Created by tuule on 20.06.17.
 */
interface BluesConnectionType {
    fun createHandler(inputStream: InputStream, outputStream: OutputStream): DataHandler
    fun renewHandler(inputStream: InputStream, outputStream: OutputStream) {
        handler = createHandler(inputStream, outputStream)
    }

    var handler: DataHandler
}