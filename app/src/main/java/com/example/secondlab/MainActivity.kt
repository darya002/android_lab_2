package com.example.secondlab

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.secondlab.broadcastreceiver.ArticleCheckService
import com.example.secondlab.broadcastreceiver.NOTIFICATION_CHANNEL_ID
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

    Log.d("MainActivity", "Saving articles: ${articles.size}")
    editor.putString(ARTICLES_KEY, json)
    editor.apply()
}

fun loadArticles(context: Context): MutableList<Article> {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    val json = sharedPreferences.getString(ARTICLES_KEY, null)
    val gson = Gson()
    val type = object : TypeToken<MutableList<Article>>() {}.type

    return if (json != null) {
        val loadedArticles = gson.fromJson<MutableList<Article>>(json, type)
        Log.d("MainActivity", "Loaded articles: ${loadedArticles.size}")
        loadedArticles
    } else {
        Log.d("MainActivity", "No articles found in SharedPreferences")
        mutableListOf()
    }
}

class MainActivity : ComponentActivity(), ArticleClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var articles: MutableList<Article>
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var addEditArticleLauncher: ActivityResultLauncher<Intent>
    private lateinit var detailArticleLauncher: ActivityResultLauncher<Intent>
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Напоминания"
            val descriptionText = "Канал для напоминаний"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d("MainActivity", "Notification permission granted")
            } else {
                Log.d("MainActivity", "Notification permission denied")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        // Запрос разрешений на уведомления для Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Загружаем статьи из SharedPreferences
        articles = loadArticles(this)
        Log.d("MainActivity", "Articles loaded: ${articles.size}")

        // Настраиваем RecyclerView и адаптер
        articleAdapter = ArticleAdapter(articles, this)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(applicationContext, 3)
            adapter = articleAdapter
        }

        // Регистрируем ланучер для AddArticleActivity
        addEditArticleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                articles = loadArticles(this)
                articleAdapter.updateArticles(articles)
                Log.d("MainActivity", "Articles updated: ${articles.size}")
                updateLastArticleTimestamp()
            }
        }

        // Регистрируем ланучер для DetailActivity (перезагрузка MainActivity после возврата)
        detailArticleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            articles = loadArticles(this)
            articleAdapter.updateArticles(articles)
            Log.d("MainActivity", "Articles reloaded after DetailActivity: ${articles.size}")
        }

        // Настраиваем FAB для добавления новых статей
        val fabAddArticle: FloatingActionButton = findViewById(R.id.fab_add_article)
        fabAddArticle.setOnClickListener {
            val intent = Intent(this, AddArticleActivity::class.java)
            addEditArticleLauncher.launch(intent)
        }
    }
    override fun onStart() {
        super.onStart()
        val serviceIntent = Intent(this, ArticleCheckService::class.java)
        startService(serviceIntent)
    }
    override fun onClick(article: Article) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra(ARTICLE_ID_EXTRA, article.id)
        detailArticleLauncher.launch(intent) // Используем launcher для перезагрузки MainActivity
    }

    override fun onStop() {
        super.onStop()
        saveArticles(this, articles)
        Log.d("MainActivity", "Articles saved: ${articles.size}")
    }

    private fun updateLastArticleTimestamp() {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putLong("last_update_time", System.currentTimeMillis())
        editor.apply()
    }
}
