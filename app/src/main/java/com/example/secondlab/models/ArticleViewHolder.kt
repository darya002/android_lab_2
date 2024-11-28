package com.example.secondlab.models

import androidx.recyclerview.widget.RecyclerView
import com.example.secondlab.databinding.CardBinding


class ArticleViewHolder(
    private val cardBinding: CardBinding,
    private val clickListener: ArticleClickListener
): RecyclerView.ViewHolder(cardBinding.root) {
    fun bindArticle(article:Article){
        cardBinding.textName.text = article.title
        cardBinding.textAuthor.text= article.author
        cardBinding.textDate.text = article.date
        cardBinding.textDescription.text = article.content

        cardBinding.cardView.setOnClickListener{
            clickListener.onClick(article)
        }
    }
}