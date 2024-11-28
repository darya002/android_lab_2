package com.example.secondlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.secondlab.databinding.CardDetailsBinding
import com.example.secondlab.models.ARTICLE_ID_EXTRA
import com.example.secondlab.models.Article
import com.example.secondlab.models.Articles

class DetailActivity : ComponentActivity()
{
    private lateinit var binding : CardDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CardDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root )
        val articleId = intent.getIntExtra(ARTICLE_ID_EXTRA, -1)
        val article = articleFromId(articleId)
        if(article!=null){
            binding.textName.text = article.title
            binding.textAuthor.text= article.author
            binding.textDate.text = article.date
            binding.textFullDescription.text = article.content
        }
    }

    private fun articleFromId(articleId: Int): Article? {
        for (article in Articles) {
            if(article.id == articleId)
                return article
        }
        return null
    }
}