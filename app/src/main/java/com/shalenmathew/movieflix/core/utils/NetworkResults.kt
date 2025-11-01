package com.shalenmathew.movieflix.core.utils


sealed class NetworkResults<T>(val data:T?=null, val message:String?=null) {

    class Success<T>(data:T?):NetworkResults<T>(data)
    class Error<T>(message: String?, data: T? = null) : NetworkResults<T>(data, message)
    class Loading<T>(data: T? = null) : NetworkResults<T>(data)

}