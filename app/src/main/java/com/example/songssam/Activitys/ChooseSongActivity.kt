package com.example.songssam.Activitys

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.songssam.API.SongSSamAPI.items
import com.example.songssam.API.SongSSamAPI.songssamAPI
import com.example.songssam.R
import com.example.songssam.adapter.ItemAdapter
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class ChooseSongActivity : AppCompatActivity(), ItemAdapter.SelectionChangeListener {


    private val btn: AppCompatButton by lazy {
        findViewById(R.id.btn)
    }
    private var itemList = mutableListOf<items>()
    private lateinit var selectedList: List<Long>
    private val recyclerView: RecyclerView by lazy {
        findViewById(R.id.rv)
    }
    private lateinit var adapter: ItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_song)
        crawlingTop100(savedInstanceState)
        initBTN()
        searchSong()
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar) //액티비티의 앱바(App Bar)로 지정
        val actionBar: ActionBar? = supportActionBar //앱바 제어를 위해 툴바 액세스
        actionBar!!.setDisplayHomeAsUpEnabled(true) // 앱바에 뒤로가기 버튼 만들기
        actionBar?.setHomeAsUpIndicator(R.drawable.arrow_back) // 뒤로가기 버튼 색상 설정
    }

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.getItemId()) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun initBTN() {
        btn.setOnClickListener {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://songssam.site:8443")
                .addConverterFactory(GsonConverterFactory.create())
                .client(
                    OkHttpClient.Builder()
                        .readTimeout(
                            30,
                            TimeUnit.SECONDS
                        )
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .build()
                )
                .build()
            val apiService = retrofit.create(songssamAPI::class.java)
            val accessToken = "Bearer " + GlobalApplication.prefs.getString("accessToken", "")

            val call = apiService.updateFavoriteSong(accessToken, selectedList)
            call.enqueue(object : Callback<Void> { // Use Callback<Void> as the callback type
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    // Handle failure here
                    Log.d("updateFavoriteSong", t.stackTraceToString())
                    Toast.makeText(
                        this@ChooseSongActivity,
                        "네트워크 오류와 같은 이유로 오류 발생!",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        val intent = Intent(this@ChooseSongActivity, MainActivity::class.java)
                        GlobalApplication.prefs.setString("chooseSong", "done")
                        startActivity(intent)
                    } else {
                        // Handle non-successful response here
                        Toast.makeText(this@ChooseSongActivity, "서버가 닫혀있습니다!", Toast.LENGTH_LONG)
                            .show()
                        Log.d("updateFavoriteSong", "연결 실패")
                    }
                }
            })
        }
    }

    private fun initRecyclerView(itemList: MutableList<items>) {
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        // recyclerview adapter 초기화
        adapter = ItemAdapter(itemList, this) // Register the activity as the listener
        recyclerView.adapter = adapter
    }


    private fun crawlingTop100(savedInstanceState: Bundle?) {
        Thread(Runnable {
            val doc =
                Jsoup.connect("https://www.melon.com/chart/index.htm").userAgent("Chrome").get()
            var elements: Elements = doc.select(".lst50")
            // mobile-padding 클래스의 board-list의 id를 가진 것들을 elements 객체에 저장
            /*
            크롤링 하는 법 : class 는 .(class) 로 찾고 id 는 #(id) 로 검색
             */
            for (elements in elements) {  //elements의 개수만큼 반복
                val songID = elements.attr("data-song-no").toLong()
                val coverImage = elements.select(".image_typeAll img").attr("src")
                val title = removeBracket(elements.select(".wrap_song_info .rank01 span a").text())
                val artist = elements.select(".wrap_song_info .rank02 span").text()
                itemList.add(
                    items(
                        songID, coverImage, title, artist, false
                    )
                )     //위에서 크롤링 한 내용들을 itemlist에 추가
            }
            elements = doc.select(".lst100")
            for (elements in elements) {  //elements의 개수만큼 반복
                val songID = elements.attr("data-song-no").toLong()
                val coverImage = elements.select(".image_typeAll img").attr("src")
                val title = removeBracket(elements.select(".wrap_song_info .rank01 span a").text())
                val artist = elements.select(".wrap_song_info .rank02 span").text()
                itemList.add(
                    items(
                        songID, coverImage, title, artist, false
                    )
                )     //위에서 크롤링 한 내용들을 itemlist에 추가
            }
            runOnUiThread {
                initRecyclerView(itemList)
            }
        }).start()
    }

    override fun onSelectionChanged(selectedItems: List<Long>) {
        // Update the TextView in your activity with the current selected items count
        val textView: TextView = findViewById(R.id.textView1)
        textView.text = "선호하는 곡 10곡 선택  ( ${selectedItems.size} / 10 )"
        selectedList = selectedItems

        // Enable or disable the button based on the number of selected items
        btn.isClickable = selectedItems.size == 10
    }

    private fun searchSong() {
        val searchView = findViewById<SearchView>(R.id.search)
        val searchAutoComplete: SearchView.SearchAutoComplete =
            searchView.findViewById(androidx.appcompat.R.id.search_src_text)

        searchAutoComplete.setTextColor(Color.BLACK)
        searchAutoComplete.setHintTextColor(Color.BLACK)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    val retrofit = Retrofit.Builder()
                        .baseUrl("https://songssam.site:8443")
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(
                            OkHttpClient.Builder()
                                .readTimeout(
                                    30,
                                    TimeUnit.SECONDS
                                ) // Adjust the timeout as needed
                                .connectTimeout(30, TimeUnit.SECONDS)
                                .build()
                        )
                        .build()
                    val apiService = retrofit.create(songssamAPI::class.java)
                    val call = apiService.search(query!!, 0)
                    call.enqueue(object : Callback<List<items>> {
                        override fun onResponse(
                            call: Call<List<items>>,
                            response: Response<List<items>>
                        ) {
                            if (response.isSuccessful.not()) {
                                Toast.makeText(
                                    this@ChooseSongActivity,
                                    "서버가 닫혀있습니다!",
                                    Toast.LENGTH_LONG
                                ).show()
                                Log.d("login", "연결 실패")
                                return
                            }
                            Log.d("login", "로그인 연결 성공")
                            try {
                                itemList = response.body()?.toMutableList() ?: mutableListOf()
                                Thread(Runnable {
                                    runOnUiThread {
                                        initRecyclerView(itemList)
                                    }
                                }).start()
                            } catch (e: Exception) {
                            }
                        }

                        override fun onFailure(call: Call<List<items>>, t: Throwable) {
                            Log.d("retrofit", t.stackTraceToString())
                            Toast.makeText(
                                this@ChooseSongActivity,
                                "네트워크 오류와 같은 이유로 오류 발생!",
                                Toast.LENGTH_LONG
                            ).show()
                            // 네트워크 오류 등 호출 실패 시 처리
                        }
                    })
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }


    private fun removeBracket(text: String): String {
        if (text.indexOf("(") !== -1) {
            return text.substring(0, text.indexOf("("))
        }
        return text
    }
}
