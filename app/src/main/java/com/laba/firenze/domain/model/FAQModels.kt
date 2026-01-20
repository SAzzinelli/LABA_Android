package com.laba.firenze.domain.model

import com.google.gson.annotations.SerializedName

data class FAQItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("q")
    val q: String,
    @SerializedName("a")
    val a: String
)

data class FAQCategory(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("items")
    val items: List<FAQItem>
)

data class FAQResponse(
    @SerializedName("categories")
    val categories: List<FAQCategory>
)
