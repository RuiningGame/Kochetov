package com.kochet.devlife

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.annotation.GlideModule
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.android.synthetic.main.fragment_joke_review.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlinx.coroutines.*
import java.lang.Exception

class JokeSectionContext(val title: String, val additionalPath: String, val hasPages: Boolean = true)

@GlideModule
class JokeReviewFragment(private val jokeContext: JokeSectionContext) : Fragment() {
    private lateinit var imageView: ImageView
    private lateinit var progressView: CircularProgressIndicator
    private lateinit var authorView: TextView
    private lateinit var descView: TextView
    private lateinit var layout: LinearLayoutCompat
    private lateinit var previousJokeButton: Button
    private lateinit var nextJokeButton: Button
    private lateinit var sliderLayout: LinearLayout
    private lateinit var errorView: TextView

    private var page: Int = 0

    private var index: Int = 0

    private var jokesList: LinkedList<JokeData> = LinkedList<JokeData>()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_joke_review, container, false)
        imageView = view.findViewById(R.id.imageView)
        progressView = view.findViewById(R.id.progressIndicator)
        authorView = view.findViewById(R.id.authorView)
        descView = view.findViewById(R.id.descView)
        layout = view.findViewById(R.id.layout)
        nextJokeButton = view.findViewById(R.id.nextJokeButton)
        nextJokeButton.setOnClickListener { loadNextJoke() }
        previousJokeButton = view.findViewById(R.id.previousJokeButton)
        sliderLayout = view.findViewById(R.id.sliderLayout)
        previousJokeButton.setOnClickListener { loadPrevJoke() }

        val errorLayout = view.findViewById<LinearLayoutCompat>(R.id.emptyListView)
        errorView = view.findViewById(R.id.errorView)
        errorLayout.findViewById<Button>(R.id.retryButton)
            .setOnClickListener {
                hideErrorView()
                GlobalScope.launch(Dispatchers.IO) { sendGet() } }
        return view
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun loadNextJoke(){
        index++
        if (index == jokesList.count()){
            page++
            GlobalScope.launch(Dispatchers.IO) { sendGet() }
        } else {
            setJoke()
        }
    }

    private fun loadPrevJoke(){
        index--
        setJoke()
    }

    private fun setJoke() {
        if (jokesList.count() == 0){
            showErrorView("Не нашлось ни одной записи в выбранной категории")

        } else{
            var joke = jokesList[index]

            activity?.runOnUiThread {
                previousJokeButton.isEnabled = index != 0
                progressView.visibility = View.GONE
                sliderLayout.visibility = View.VISIBLE
                authorView.text = joke.author
                descView.text = joke.description
                val circularProgressDrawable = context?.let { CircularProgressDrawable(it) }
                if (circularProgressDrawable != null)
                {
                    circularProgressDrawable.strokeWidth = 10f
                    circularProgressDrawable.centerRadius = 50f
                    circularProgressDrawable.start()
                }
                Glide.with(this)
                    .asGif()
                    .load(joke.gifURL)
                    .placeholder(circularProgressDrawable)
                    .error(R.mipmap.ic_launcher_round)
                    .into(imageView)
            }
        }
    }

    private fun showErrorView(text: String){
        activity?.runOnUiThread {
            errorView.text = text
            progressView.visibility = View.GONE
            emptyListView.visibility = View.VISIBLE
            sliderLayout.visibility = View.GONE
            layout.visibility = View.GONE
        }
    }

    private fun hideErrorView(){
        activity?.runOnUiThread {
            emptyListView.visibility = View.GONE
            layout.visibility = View.VISIBLE
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        GlobalScope.launch(Dispatchers.IO) { sendGet() }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun sendGet()
    {
            activity?.runOnUiThread { progressView.visibility = View.VISIBLE }
            var pagePath = "";
            if (jokeContext.hasPages) {
                pagePath = "/${page}"
            }
            val url = URL("https://developerslife.ru/${jokeContext.additionalPath}${pagePath}?json=true")

        try {
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"

                println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")

                inputStream.bufferedReader().use {
                    it.lines().forEach { line ->
                        parseResponse(line)
                    }
                    setJoke()
                }
            }
        }
        catch (e: Exception) {
            print(e.message)
            if (Utilities.isOnline(context)){
                showErrorView("Произошла ошибка при загрузке данных. Свяжитесь со мной")
            } else{
                showErrorView("Произошла ошибка при загрузке данных. Проверьте подключение к сети.")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun parseResponse(response: String) {
        println(response)
        if (jokeContext.hasPages) {
            val page = Json { ignoreUnknownKeys = true }.decodeFromString<JokePage>(response)
            for (jokeData in page.result) {
                jokeData.gifURL = Utilities.replaceStartInUrl(jokeData.gifURL)
                jokesList.add(jokeData)
            }
        }
        else {
            val jokeData = Json { ignoreUnknownKeys = true }.decodeFromString<JokeData>(response)
            jokeData.gifURL = Utilities.replaceStartInUrl(jokeData.gifURL)
            jokesList.add(jokeData)
        }
    }
}
