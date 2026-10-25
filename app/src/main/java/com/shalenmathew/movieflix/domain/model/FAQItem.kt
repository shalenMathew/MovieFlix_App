package com.shalenmathew.movieflix.domain.model

data class FAQItem(
    val question: String,
    val answer: String,
    var isExpanded: Boolean = false
)

sealed class FAQRow {
    data class Header(val title: String) : FAQRow()
    data class Item(val item: FAQItem) : FAQRow()
}
