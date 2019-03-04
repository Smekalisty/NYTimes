package com.nytimes.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.nytimes.R
import com.nytimes.contracts.API
import com.nytimes.entities.Constants
import com.nytimes.entities.pojo.Post
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.net.UnknownHostException

class MainActivity : AppCompatActivity() {
    private val disposables = CompositeDisposable()

    private lateinit var refresh: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView

    private var adapter: Adapter? = null

    private val keyDataSource = "keyDataSource"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        refresh = findViewById(R.id.refresh)
        recyclerView = findViewById(R.id.recyclerView)

        refresh.isRefreshing = true
        refresh.setOnRefreshListener {
            requestDataSource()
        }

        adapter = Adapter().apply {
            val disposable = clickSubject.subscribe(this@MainActivity::onSelected, this@MainActivity::onError)
            disposables.add(disposable)
        }

        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(this)

        if (savedInstanceState == null) {
            requestDataSource()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putParcelableArrayList(keyDataSource, adapter?.dataSource)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        val dataSource =  savedInstanceState?.getParcelableArrayList<Post>(keyDataSource)
        if (dataSource == null) {
            requestDataSource()
        } else {
            onSuccess(dataSource)
        }

        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }

    private fun requestDataSource() {
        val gson = GsonBuilder()
            .registerTypeAdapter(ArrayList::class.java, JsonDeserializer<ArrayList<Post>> { json, type, _ ->
                val jsonElement = json?.asJsonObject?.get("results")
                Gson().fromJson<ArrayList<Post>>(jsonElement, type)
            })
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        val api = retrofit.create(API::class.java)

        val disposable = api
            .posts(7.toString(), Constants.apiKey)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(this::onSuccess, this::onError)

        disposables.add(disposable)
    }

    private fun onSuccess(posts: ArrayList<Post>) {
        refresh.isRefreshing = false
        if (posts.isEmpty()) {
            showMessage(R.string.no_posts_time_to_work)
        } else {
            if (adapter == null) {
                showMessage(R.string.an_error_has_occurred)
            } else {
                adapter!!.dataSource = posts
                adapter!!.notifyDataSetChanged()
            }
        }
    }

    private fun onError(error: Throwable) {
        refresh.isRefreshing = false

        if (error is UnknownHostException) {
            showMessage(R.string.probably_no_connection)
        } else {
            val message = error.message ?: error.toString()
            showMessage(message)
        }
    }

    private fun showMessage(@StringRes id: Int) {
        Snackbar.make(recyclerView, id, Snackbar.LENGTH_SHORT).show()
    }

    private fun showMessage(message: String) {
        Snackbar.make(recyclerView, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun onSelected(post: Post) {
        if (!refresh.isRefreshing) {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra(DetailActivity.extraPost, post)
            startActivity(intent)
        }
    }

    private class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>() {
        val clickSubject = PublishSubject.create<Post>()

        var dataSource = arrayListOf<Post>()

        override fun getItemCount() = dataSource.size

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            val layoutInflater = LayoutInflater.from(viewGroup.context)
            val view = layoutInflater.inflate(R.layout.layout_post, viewGroup, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            val post = dataSource[position]

            val defaultImage = ContextCompat.getDrawable(viewHolder.itemView.context, R.drawable.icon_default_image)

            val requestOptions = RequestOptions.circleCropTransform()
                .error(defaultImage)
                .placeholder(defaultImage)
                .diskCacheStrategy(DiskCacheStrategy.ALL)

            val media = post.media.firstOrNull { it.type == "image" }
            val metadata = media?.metadata?.firstOrNull { it.format == "square320" }

            var uri: Uri? = null
            metadata?.url?.let {
                uri = Uri.parse(it)
                    .buildUpon()
                    .appendQueryParameter(Constants.apiKeyParameter, Constants.apiKey)
                    .build()
            }

            Glide.with(viewHolder.itemView)
                .load(uri)
                .apply(requestOptions)
                .into(viewHolder.image)

            viewHolder.title.text = post.title
            viewHolder.description.text = post.abstract

            viewHolder.itemView.setOnClickListener {
                clickSubject.onNext(post)
            }
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var image: AppCompatImageView = view.findViewById(R.id.image)
            var title: AppCompatTextView = view.findViewById(R.id.title)
            var description: AppCompatTextView = view.findViewById(R.id.description)
        }
    }
}