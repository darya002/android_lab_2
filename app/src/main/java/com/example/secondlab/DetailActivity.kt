package com.example.secondlab

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.secondlab.databinding.CardDetailsBinding
import com.example.secondlab.models.ARTICLE_ID_EXTRA
import com.example.secondlab.models.Article
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DetailActivity : ComponentActivity() {

    private lateinit var binding: CardDetailsBinding
    private var article: Article? = null // Храним текущую статью
    private var articles: MutableList<Article> = mutableListOf()

    // Лаунчер для AddArticleActivity
    // В DetailActivity, после редактирования или добавления статьи
    val editArticleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // Перезагружаем MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()  // Закрываем DetailActivity, чтобы вернуться в MainActivity
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CardDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Получаем ID статьи из Intent
        val articleId = intent.getIntExtra(ARTICLE_ID_EXTRA, -1)

        // Загружаем список статей из SharedPreferences
        articles = loadArticles()

        // Ищем статью по ID
        article = articles.find { it.id == articleId }

        // Если статья найдена, отображаем её
        article?.let { updateUI(it) }
        // В DetailActivity
        binding.button.setOnClickListener {
            if (article != null) {
                Log.d("DetailActivity", "Article to edit: ${article?.id}")
                val intent = Intent(this, AddArticleActivity::class.java).apply {
                    putExtra(ARTICLE_ID_EXTRA, article?.id)
                }
                editArticleLauncher.launch(intent)
            } else {
                Log.d("DetailActivity", "Article is null!")
            }
        }
    }

    private fun updateUI(article: Article) {
        binding.textName.text = article.title
        binding.textAuthor.text = article.author
        binding.textDate.text = article.date
        binding.textFullDescription.text = article.content
    }

    private fun loadArticles(): MutableList<Article> {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
        val json = sharedPreferences.getString(ARTICLES_KEY, null)
        val gson = Gson()
        val type = object : TypeToken<MutableList<Article>>() {}.type
        return if (json != null) gson.fromJson(json, type) else mutableListOf()
    }

    private fun saveArticles(articles: MutableList<Article>) {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(articles)
        editor.putString(ARTICLES_KEY, json)
        editor.apply()
    }
}
