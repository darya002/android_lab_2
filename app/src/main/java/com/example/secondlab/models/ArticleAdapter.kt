package com.example.secondlab.models

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.secondlab.databinding.CardBinding

class ArticleAdapter(
    private var articles: MutableList<Article>, // Изменяем список на MutableList
    private val clickListener: ArticleClickListener
) : RecyclerView.Adapter<ArticleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val from = LayoutInflater.from(parent.context)
        val binding = CardBinding.inflate(from, parent, false)
        return ArticleViewHolder(binding, clickListener)
    }

    override fun getItemCount(): Int = articles.size

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bindArticle(articles[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateArticles(newArticles: MutableList<Article>) {
        Log.d("ArticleAdapter", "Updating articles in adapter: ${newArticles.size}")
        articles.clear()
        articles.addAll(newArticles)
        notifyDataSetChanged() // Обновляем адаптер
    }

}
