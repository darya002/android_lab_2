package com.example.secondlab.models

val Articles = mutableListOf<Article>()

val ARTICLE_ID_EXTRA = "articleExtra"

var currentArticleId = 0

data class Article(
    var title: String,
    var content: String,
    var author: String,
    var date: String,
    val id: Int
)