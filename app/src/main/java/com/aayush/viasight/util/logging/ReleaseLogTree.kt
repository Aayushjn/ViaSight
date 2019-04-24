package com.aayush.viasight.util.logging

import timber.log.Timber

class ReleaseLogTree: Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {}
}