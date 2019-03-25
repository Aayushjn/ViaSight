package com.aayush.viasight.util

import timber.log.Timber

class NoLogTree: Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {}
}