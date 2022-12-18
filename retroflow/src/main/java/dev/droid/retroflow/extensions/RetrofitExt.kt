package dev.droid.retroflow.extensions

import okhttp3.ResponseBody

fun ResponseBody.stringCopy(): String {
    val peekSource = source().peek()
    val charset = contentType()?.charset(Charsets.UTF_8) ?: Charsets.UTF_8
    return peekSource.readString(charset)
}