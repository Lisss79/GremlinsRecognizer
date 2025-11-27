package ru.lisss79.gremlins_recognizer

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform