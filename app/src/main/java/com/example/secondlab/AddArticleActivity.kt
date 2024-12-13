package com.example.secondlab

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.ComponentActivity
import com.example.secondlab.databinding.EditArticleBinding
import com.example.secondlab.models.ARTICLE_ID_EXTRA
import com.example.secondlab.models.Article
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.regex.Pattern

class AddArticleActivity : ComponentActivity() {

    private lateinit var binding: EditArticleBinding
    private var articleId: Int = -1
    private var articles: MutableList<Article> = mutableListOf()
    private var originalArticle: Article? = null // Оригинальные данные статьи

    // Регулярное выражение для проверки формата даты (ДД/ММ/ГГГГ)
    private val datePattern = Pattern.compile("^(0[1-9]|[12][0-9]|3[01]).(0[1-9]|1[0-2]).\\d{4}$")

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
            article?.let {
                originalArticle = it.copy() // Сохраняем оригинальные данные
                fillFields(it)
            }
        }

        // Слушатель на кнопку "Done"
        binding.button.setOnClickListener {
            if (articleId == -1) {
                // Добавляем новую статью
                if (validateFields() && validateDate()) {
                    addNewArticle()
                    showSnackbar("Статья добавлена!", null)
                }
            } else {
                // Обновляем существующую статью
                if (validateFields() && validateDate()) {
                    updateArticle()
                    showSnackbar("Статья обновлена!", ::rollbackChanges)
                }
            }
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
    }

    private fun rollbackChanges() {
        // Откатываем изменения, если статья была изменена
        if (articleId != -1 && originalArticle != null) {
            val index = articles.indexOfFirst { it.id == articleId }
            if (index != -1) {
                articles[index] = originalArticle!! // Восстанавливаем оригинальную статью
                saveArticles(articles)
            }
        }
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

    private fun generateUniqueId(): Int {
        return (System.currentTimeMillis() % Int.MAX_VALUE).toInt() // Пример уникального ID
    }

    private fun showSnackbar(message: String, undoAction: (() -> Unit)?) {
        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        if (undoAction != null) {
            snackbar.setAction("Отменить") {
                undoAction() // Выполняем действие отмены
            }
        }
        snackbar.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar, event: Int) {
                // Возвращаемся в MainActivity после закрытия Snackbar
                setResult(RESULT_OK)
                val intent = Intent(this@AddArticleActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
        snackbar.show()
    }

    // Функция для проверки всех полей на заполненность
    private fun validateFields(): Boolean {
        var isValid = true

        // Проверяем поле для названия
        if (TextUtils.isEmpty(binding.name.text)) {
            binding.name.error = "Поле не может быть пустым"
            isValid = false
        } else {
            binding.name.error = null
        }

        // Проверяем поле для автора
        if (TextUtils.isEmpty(binding.Author.text)) {
            binding.Author.error = "Поле не может быть пустым"
            isValid = false
        } else {
            binding.Author.error = null
        }

        // Проверяем поле для описания
        if (TextUtils.isEmpty(binding.textDescription.text)) {
            binding.textDescription.error = "Поле не может быть пустым"
            isValid = false
        } else {
            binding.textDescription.error = null
        }

        return isValid
    }

    private fun validateDate(): Boolean {
        val dateText = binding.Date.text.toString()
        return if (dateText.isNotEmpty() && !datePattern.matcher(dateText).matches()) {
            // Если дата не соответствует формату
            binding.Date.error = "Неверный формат даты. Используйте ДД.ММ.ГГГГ"
            false
        } else {
            // Если дата правильная
            binding.Date.error = null
            true
        }
    }
}
