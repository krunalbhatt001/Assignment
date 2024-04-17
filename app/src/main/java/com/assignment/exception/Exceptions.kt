package com.assignment.exception

import java.io.IOException

/** handling HTTP errors*/
class HttpErrorException(message: String) : IOException(message)

/** handling Forbidden errors*/
class ForbiddenException(message: String) : IOException(message)

/** handling unreachable server errors*/
class UnreachableServerException(message: String) : IOException(message)

/** handling JSON parsing errors*/
class JsonParsingException(message: String) : Exception(message)

/**  handling bitmap decode error*/
class BitmapDecodeException(message: String) : Exception(message)

/** handling url belongs to image or not*/
class UrlNotImageException(message: String) : Exception(message)

class NoInternetException(message: String) : Exception(message)