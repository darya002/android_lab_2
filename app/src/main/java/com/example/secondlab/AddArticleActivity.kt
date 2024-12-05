package com.example.secondlab

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.secondlab.databinding.EditArticleBinding
import com.example.secondlab.models.ARTICLE_ID_EXTRA
import com.example.secondlab.models.Article
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AddArticleActivity : ComponentActivity() {

    private lateinit var binding: EditArticleBinding
    private var articleId: Int = -1
    private var articles: MutableList<Article> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Загружаем статьи из SharedPreferences
        articles = loadArticles()

        // Проверяем, передан ли ARTICLE_ID_EXTRA
        articleId = intent.getIntExtra(ARTICLE_ID_EXTRA, -1)

        if (articleId != -1) {
            // Если ID статьи передан, это режим редактирования
            val article = articles.find { it.id == articleId }
            article?.let { fillFields(it) }
        }

        // Слушатель на кнопку "Done"
        // В AddArticleActivity
        binding.button.setOnClickListener {
            if (articleId == -1) {
                // Добавляем новую статью
                addNewArticle()
            } else {
                // Обновляем существующую статью
                updateArticle()
            }

            // Возвращаемся в MainActivity
            setResult(Activity.RESULT_OK)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)  // Переходим в MainActivity
            finish()  // Закрываем текущую активность
        }


    }

    private fun fillFields(article: Article) {
        binding.name.requestFocus()
        binding.name.setText(article.title)
        binding.Author.setText(article.author)
        binding.Date.setText(article.date)
        binding.textDescription.setText(article.content)
    }

    private fun addNewArticle() {
        val newArticle = Article(
            id = generateUniqueId(), // Генерируем уникальный ID
            title = binding.name.text.toString(),
            author = binding.Author.text.toString(),
            date = binding.Date.text.toString(),
            content = binding.textDescription.text.toString()
        )

        articles.add(newArticle)
        saveArticles(articles)
    }
    private fun generateUniqueId(): Int {
        return (System.currentTimeMillis() % Int.MAX_VALUE).toInt() // Пример уникального ID
    }

    private fun updateArticle() {
        // Находим статью по ID
        val article = articles.find { it.id == articleId }
        article?.apply {
            // Обновляем поля
            title = binding.name.text.toString()
            author = binding.Author.text.toString()
            date = binding.Date.text.toString()
            content = binding.textDescription.text.toString()
        }

        // Сохраняем обновленные данные
        saveArticles(articles)

        // Возвращаем результат в MainActivity или DetailActivity
        setResult(Activity.RESULT_OK)
        finish()  // Закрываем AddArticleActivity и возвращаемся
    }


    private fun saveArticles(articles: MutableList<Article>) {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(articles)
        editor.putString(ARTICLES_KEY, json)
        editor.apply()
    }

    private fun loadArticles(): MutableList<Article> {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
        val json = sharedPreferences.getString(ARTICLES_KEY, null)
        val gson = Gson()
        val type = object : TypeToken<MutableList<Article>>() {}.type
        return if (json != null) gson.fromJson(json, type) else mutableListOf()
    }
}
