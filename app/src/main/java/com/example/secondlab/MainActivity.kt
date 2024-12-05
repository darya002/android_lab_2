package com.example.secondlab

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import com.example.secondlab.databinding.ActivityMainBinding
import com.example.secondlab.models.ARTICLE_ID_EXTRA
import com.example.secondlab.models.Article
import com.example.secondlab.models.ArticleAdapter
import com.example.secondlab.models.ArticleClickListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


const val SHARED_PREFS_NAME = "articles_prefs"
const val ARTICLES_KEY = "articles_key"

private fun saveArticles(context: Context, articles: List<Article>) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    val gson = Gson()
    val json = gson.toJson(articles)

    // Логируем перед сохранением
    Log.d("MainActivity", "Saving articles: ${articles.size}")
    editor.putString(ARTICLES_KEY, json)
    editor.apply()
}

// Функция для загрузки статей
fun loadArticles(context: Context): MutableList<Article> {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    val json = sharedPreferences.getString(ARTICLES_KEY, null)
    val gson = Gson()
    val type = object : TypeToken<MutableList<Article>>() {}.type

    if (json != null) {
        val loadedArticles = gson.fromJson<MutableList<Article>>(json, type)
        Log.d("MainActivity", "Loaded articles: ${loadedArticles.size}")
        return loadedArticles
    } else {
        Log.d("MainActivity", "No articles found in SharedPreferences")
        return mutableListOf()
    }
}


class MainActivity : ComponentActivity(), ArticleClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var articles: MutableList<Article>
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var addEditArticleLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Загружаем статьи из SharedPreferences
        articles = loadArticles(this)
        Log.d("MainActivity", "Articles loaded: ${articles.size}")

        // Настроим RecyclerView и адаптер
        articleAdapter = ArticleAdapter(articles, this)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(applicationContext, 3)
            adapter = articleAdapter
        }

        // Регистрируем ланучер для AddArticleActivity
        addEditArticleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d("MainActivity", "Result code: ${result.resultCode}")  // Добавил логирование результата
            if (result.resultCode == RESULT_OK) {
                // После редактирования или добавления обновляем статьи
                articles = loadArticles(this)  // Загружаем обновленные данные
                Log.d("MainActivity", "Articles updated: ${articles.size}")

                // Обновляем адаптер с новыми данными
                articleAdapter.updateArticles(articles)  // Обновляем данные в адаптере
            }
        }


        // Настроим FAB для добавления новых статей
        val fabAddArticle: FloatingActionButton = findViewById(R.id.fab_add_article)
        fabAddArticle.setOnClickListener {
            val intent = Intent(this, AddArticleActivity::class.java)
            addEditArticleLauncher.launch(intent) // Запуск AddArticleActivity
        }
    }

    override fun onClick(article: Article) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra(ARTICLE_ID_EXTRA, article.id)
        startActivity(intent)
    }

    override fun onStop() {
        super.onStop()
        // Сохраняем статьи перед выходом из активности
        saveArticles(this, articles)
        Log.d("MainActivity", "Articles saved: ${articles.size}")
    }
}
