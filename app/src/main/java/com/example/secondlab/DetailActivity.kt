package com.example.secondlab

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.secondlab.databinding.CardDetailsBinding
import com.example.secondlab.models.ARTICLE_ID_EXTRA
import com.example.secondlab.models.Article
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DetailActivity : ComponentActivity() {

    private lateinit var binding: CardDetailsBinding
    private var article: Article? = null
    private var articles: MutableList<Article> = mutableListOf()

    val editArticleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CardDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val articleId = intent.getIntExtra(ARTICLE_ID_EXTRA, -1)
        articles = loadArticles()
        article = articles.find { it.id == articleId }

        article?.let { updateUI(it) }

        // Обработчик для кнопки "Редактировать"
        binding.button.setOnClickListener {
            article?.let {
                val intent = Intent(this, AddArticleActivity::class.java).apply {
                    putExtra(ARTICLE_ID_EXTRA, it.id)
                }
                editArticleLauncher.launch(intent)
            }
        }

        // Обработчик для кнопки "Удалить"
        binding.delButton.setOnClickListener {
            article?.let {
                deleteArticle(it)
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


    private fun deleteArticle(article: Article) {
        // Удаляем статью из списка
        articles.remove(article)
        saveArticles(articles) // Сохраняем изменения

        // Показать Snackbar
        Snackbar.make(binding.root, "Статья удалена", Snackbar.LENGTH_LONG)
            .setAction("Отменить") {
                // Действие для отмены удаления
                articles.add(article) // Возвращаем статью
                saveArticles(articles) // Сохраняем восстановленные данные
                Snackbar.make(binding.root, "Удаление отменено", Snackbar.LENGTH_SHORT).show()
            }.addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar, event: Int) {
                    // После закрытия Snackbar переходим на MainActivity
                    if (event != DISMISS_EVENT_ACTION) { // Если пользователь не нажал "Отменить"
                        navigateToMainActivity()
                    }
                }
            }).show()
    }
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
