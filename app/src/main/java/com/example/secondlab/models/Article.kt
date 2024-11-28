package com.example.secondlab.models

val Articles = mutableListOf<Article>()

val ARTICLE_ID_EXTRA = "article extra"

data class Article(
    val title: String,
    val content: String,
    val author: String,
    val date: String,
    val id: Int? = Articles.size
)