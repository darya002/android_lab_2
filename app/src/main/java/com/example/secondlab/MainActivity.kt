package com.example.secondlab

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.secondlab.databinding.ActivityMainBinding
import com.example.secondlab.models.ARTICLE_ID_EXTRA
import com.example.secondlab.models.Article
import com.example.secondlab.models.ArticleAdapter
import com.example.secondlab.models.ArticleClickListener
import com.example.secondlab.models.Articles
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : ComponentActivity(), ArticleClickListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        addArticles()

        // Настройка RecyclerView
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(applicationContext, 3)
            adapter = ArticleAdapter(Articles, this@MainActivity)
        }

        // Обработчик для FloatingActionButton
        val fabAddArticle: FloatingActionButton = findViewById(R.id.fab_add_article)
        fabAddArticle.setOnClickListener {
            //addNewArticle()
        }
    }

    override fun onClick(article: Article) {
        val intent = Intent(applicationContext, DetailActivity::class.java)
        intent.putExtra(ARTICLE_ID_EXTRA, article.id)
        startActivity(intent)
    }

    private fun addArticles() {
        val art1 = Article("Bees", "Bees are very smart. They love flowers. I don't like them.", "Butuzova Daria", "27.11.2024")
        Articles.add(art1)
        Articles.add(art1.copy())
        Articles.add(art1.copy())
    }


//    private fun addNewArticle() {
//        // Добавление новой статьи
//        val newArticle = Article("New Article", "This is a new article.", "Author Name", "28.11.2024")
//        Articles.add(newArticle)
//
//        // Уведомляем адаптер о новом элементе
//        binding.recyclerView.adapter?.notifyItemInserted(Articles.size - 1)
//    }
}

//
//@Composable
//fun PlusButton(){
//        Box(modifier = Modifier.fillMaxSize()){
//            FloatingActionButton(
//                modifier = Modifier.padding(16.dp)
//                    .align(Alignment.BottomEnd), containerColor = Color.Cyan ,
//                onClick = {}) { }
//        }
//}
