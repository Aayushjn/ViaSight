package com.aayush.viasight.util.logging

import timber.log.Timber.Tree

class ReleaseLogTree: Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {}
}