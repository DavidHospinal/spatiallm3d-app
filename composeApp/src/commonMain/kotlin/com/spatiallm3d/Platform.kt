package com.spatiallm3d

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform