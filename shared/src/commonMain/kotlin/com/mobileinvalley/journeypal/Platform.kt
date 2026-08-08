package com.mobileinvalley.journeypal

import kotlinx.datetime.Instant

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun now(): Instant

expect fun resolveUri(uri: String?): Any?
