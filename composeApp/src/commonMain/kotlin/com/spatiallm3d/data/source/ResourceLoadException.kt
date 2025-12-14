package com.spatiallm3d.data.source

/**
 * Exception thrown when resource loading fails.
 */
expect class ResourceLoadException : Exception {
    constructor(message: String)
    constructor(message: String, cause: Throwable?)
}
