package com.nytimes.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import com.nytimes.R
import com.nytimes.entities.pojo.Post
import android.content.Intent
import android.net.Uri
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.nytimes.entities.Constants

class DetailActivity : AppCompatActivity() {
    companion object {
        const val extraPost = "extraPost"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val id = findViewById<AppCompatTextView>(R.id.id)
        val title = findViewById<AppCompatTextView>(R.id.title)
        val description = findViewById<AppCompatTextView>(R.id.description)
        val openUrl = findViewById<View>(R.id.openUrl)
        val image = findViewById<AppCompatImageView>(R.id.image)

        val post = intent.getParcelableExtra<Post>(extraPost)

        id.text = post.id.toString()
        title.text = post.title
        description.text = post.abstract

        openUrl.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(post.url)
            startActivity(intent)
        }

        val media = post.media.firstOrNull { it.type == "image" }
        val metadata = media?.metadata?.firstOrNull { it.format == "superJumbo" }

        var uri: Uri? = null
        metadata?.url?.let {
            uri = Uri.parse(it)
                .buildUpon()
                .appendQueryParameter(Constants.apiKeyParameter, Constants.apiKey)
                .build()
        }

        Glide.with(this)
            .load(uri)
            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
            .into(image)
    }
}