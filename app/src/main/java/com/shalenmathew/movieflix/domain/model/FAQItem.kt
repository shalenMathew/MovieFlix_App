package com.shalenmathew.movieflix.domain.model

data class FAQItem(
    val question: String,
    val answer: String,
    var isExpanded: Boolean = false
)
