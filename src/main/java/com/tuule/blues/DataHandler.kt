package com.tuule.blues

import java.io.InputStream
import java.io.OutputStream

/**
 * Created by tuule on 19.06.17.
 */
interface DataHandler {
    val inputStream: InputStream
    val outputStream: OutputStream
    fun parseData(): String
}