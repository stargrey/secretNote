package com.github.stargrey.secret.ui.fragment

interface SearchOrSoftDataImpl {
    var searchString: String
    var softOrder: String

    abstract fun refreshData()
}