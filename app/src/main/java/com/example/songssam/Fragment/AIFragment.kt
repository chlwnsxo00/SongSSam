package com.example.songssam.Fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.songssam.API.SongSSamAPI.chartjsonItems
import com.example.songssam.API.SongSSamAPI.songssamAPI
import com.example.songssam.Activitys.ChooseSongActivity
import com.example.songssam.Activitys.GlobalApplication
import com.example.songssam.Activitys.MainActivity
import com.example.songssam.Activitys.RecordingActivity
import com.example.songssam.R
import com.example.songssam.adapter.AddSongAdapter
import com.example.songssam.adapter.AddSongClick
import com.example.songssam.databinding.FragmentAiBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


/**
 * A simple [Fragment] subclass.
 */
class AIFragment : Fragment(), AddSongClick {
    lateinit var binding: FragmentAiBinding
    private lateinit var songadApter: AddSongAdapter
    private var itemList = mutableListOf<chartjsonItems>()
    private val mainActivity: MainActivity by lazy {
        context as MainActivity
    }
    private var uploadSongId: Long = 0
    private val REQUEST_CODE_PICK_FILE = 101

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAiBinding.inflate(inflater)
        checkChosen()
        searchSong()
        // Inflate the layout for this fragment
        return binding.root
    }


    private fun checkChosen() {
        if (GlobalApplication.prefs.getString("chooseSong", "").equals("")) {
            initBeforeChosen()
        } else {
            Log.d("ai", "else")
            binding.beforeChooseSong.visibility = GONE
            binding.afterChooseSong.visibility = VISIBLE
            chartjson()
            initRecyclerView()
            initSpinner()
        }
    }

    private fun initSpinner() {
        val spinner = binding.spinner
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            mainActivity,
            R.array.status,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> {
                        chartjson()
                        initRecyclerView()
                    }

                    1 -> {
                        getUploadedList()
                        initRecyclerView()
                    }

                    2 -> {
                        getCompletedList()
                        initRecyclerView()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                getUploadedList()
                initRecyclerView()
            }
        }
    }

    private fun initBeforeChosen() {
        Log.d("ai", "if")
        binding.beforeChooseSong.visibility = VISIBLE
        binding.afterChooseSong.visibility = GONE
        binding.btnChooseSong.setOnClickListener {
            val intent = Intent(activity, ChooseSongActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initRecyclerView() {
        binding.rv.layoutManager =
            LinearLayoutManager(binding.root.context, LinearLayoutManager.VERTICAL, false)
        songadApter = AddSongAdapter(itemList, this)
        binding.rv.adapter = songadApter
    }

    private fun chartjson() {
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
        val call = apiService.chartJson()
        call.enqueue(object : Callback<List<chartjsonItems>> {
            override fun onResponse(
                call: Call<List<chartjsonItems>>,
                response: Response<List<chartjsonItems>>
            ) {
                if (response.isSuccessful.not()) {
                    Toast.makeText(
                        mainActivity,
                        "서버가 닫혀있습니다!",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.d("ai", "연결 실패")
                    return
                }
                Log.d("ai", "로그인 연결 성공")
                try {
                    itemList = response.body()?.toMutableList() ?: mutableListOf()
                    Thread(Runnable {
                        mainActivity.runOnUiThread {
                            initRecyclerView()
                        }
                    }).start()
                } catch (e: Exception) {
                }
            }

            override fun onFailure(call: Call<List<chartjsonItems>>, t: Throwable) {
                Log.d("ai", t.stackTraceToString())
                Toast.makeText(
                    mainActivity,
                    "네트워크 오류와 같은 이유로 오류 발생!",
                    Toast.LENGTH_LONG
                ).show()
                // 네트워크 오류 등 호출 실패 시 처리
            }
        })
    }

    private fun getUploadedList() {
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
        val call = apiService.getUploadedList()
        call.enqueue(object : Callback<List<chartjsonItems>> {
            override fun onResponse(
                call: Call<List<chartjsonItems>>,
                response: Response<List<chartjsonItems>>
            ) {
                if (response.isSuccessful.not()) {
                    Toast.makeText(
                        mainActivity,
                        "서버가 닫혀있습니다!",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.d("ai", "연결 실패")
                    return
                }
                Log.d("ai", "로그인 연결 성공")
                try {
                    itemList = response.body()?.toMutableList() ?: mutableListOf()
                    Thread(Runnable {
                        mainActivity.runOnUiThread {
                            initRecyclerView()
                        }
                    }).start()
                } catch (e: Exception) {
                }
            }

            override fun onFailure(call: Call<List<chartjsonItems>>, t: Throwable) {
                Log.d("ai", t.stackTraceToString())
                Toast.makeText(
                    mainActivity,
                    "네트워크 오류와 같은 이유로 오류 발생!",
                    Toast.LENGTH_LONG
                ).show()
                // 네트워크 오류 등 호출 실패 시 처리
            }
        })
    }

    private fun getCompletedList() {
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
        val call = apiService.getCompletedList()
        call.enqueue(object : Callback<List<chartjsonItems>> {
            override fun onResponse(
                call: Call<List<chartjsonItems>>,
                response: Response<List<chartjsonItems>>
            ) {
                if (response.isSuccessful.not()) {
                    Toast.makeText(
                        mainActivity,
                        "서버가 닫혀있습니다!",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.d("ai", "연결 실패")
                    return
                }
                Log.d("ai", "로그인 연결 성공")
                try {
                    itemList = response.body()?.toMutableList() ?: mutableListOf()
                    Thread(Runnable {
                        mainActivity.runOnUiThread {
                            initRecyclerView()
                        }
                    }).start()
                } catch (e: Exception) {
                }
            }

            override fun onFailure(call: Call<List<chartjsonItems>>, t: Throwable) {
                Log.d("ai", t.stackTraceToString())
                Toast.makeText(
                    mainActivity,
                    "네트워크 오류와 같은 이유로 오류 발생!",
                    Toast.LENGTH_LONG
                ).show()
                // 네트워크 오류 등 호출 실패 시 처리
            }
        })
    }

    private fun searchSong() {
        val searchView = binding.search
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
                    val call = apiService.recordableSearch(query!!, 0)
                    call.enqueue(object : Callback<List<chartjsonItems>> {
                        override fun onResponse(
                            call: Call<List<chartjsonItems>>,
                            response: Response<List<chartjsonItems>>
                        ) {
                            if (response.isSuccessful.not()) {
                                Toast.makeText(
                                    mainActivity,
                                    "서버가 닫혀있습니다!",
                                    Toast.LENGTH_LONG
                                ).show()
                                Log.d("record", "연결 실패")
                                return
                            }
                            Log.d("record", "로그인 연결 성공")
                            try {
                                itemList = response.body()?.toMutableList() ?: mutableListOf()
                                Thread(Runnable {
                                    mainActivity.runOnUiThread {
                                        initRecyclerView()
                                    }
                                }).start()
                            } catch (e: Exception) {
                            }
                        }

                        override fun onFailure(call: Call<List<chartjsonItems>>, t: Throwable) {
                            Log.d("record", t.stackTraceToString())
                            Toast.makeText(
                                mainActivity,
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

    override fun isNull(songId: Long) {
        selectMp3(songId)
    }

    override fun isProcessing() {
        Toast.makeText(mainActivity, "현재 전처리 중에 있습니다!\n (평균 소요 시간: 3분)", Toast.LENGTH_SHORT).show()
    }

    override fun isUpLoaded(songId: Long) {
        processingSong(songId)
    }

    private fun processingSong(songId: Long) {
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
        val call = apiService.processingSong(songId)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(
                call: Call<Void>,
                response: Response<Void>
            ) {
                if (response.isSuccessful.not()) {
                    Toast.makeText(
                        mainActivity,
                        "서버가 닫혀있습니다!",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("ai", "연결 실패 - Response code: ${response.code()}")
                }
                Log.d("ai", "로그인 연결 성공")
                Toast.makeText(
                    mainActivity,
                    "전처리 요청 성공!",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("ai", "Call failed: ${t.message}")
                Toast.makeText(
                    mainActivity,
                    "전처리 요청 실패!",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    override fun isCompleted(
        title: String,
        artist: String,
        cover: String,
        songId: Long,
        instUrl: String
    ) {
        val intent = Intent(activity, RecordingActivity::class.java)
        intent.putExtra("title", title)
        intent.putExtra("artist", artist)
        intent.putExtra("cover", cover)
        intent.putExtra("songId", songId)
        intent.putExtra("instUrl", instUrl)
        startActivity(intent)
    }

    private fun selectMp3(songId: Long) {
        uploadSongId = songId
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "audio/mpeg"
        }
        if (intent.resolveActivity(mainActivity.packageManager) != null) {
            startActivityForResult(intent, REQUEST_CODE_PICK_FILE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            Log.d("mp3", uri.toString())
            if (uri != null) {
                val contentResolver = mainActivity.applicationContext.contentResolver
                val inputStream = contentResolver.openInputStream(uri)
                val songIdRequestBody =
                    RequestBody.create(
                        "text/plain".toMediaTypeOrNull(),
                        uploadSongId.toString()
                    ) // Convert songId to RequestBody
                val fileRequestBody =
                    inputStream?.readBytes()?.toRequestBody("audio/mpeg".toMediaTypeOrNull())
                val filePart = fileRequestBody?.let {
                    MultipartBody.Part.createFormData("file", "$uploadSongId.mp3", it)
                }

                val retrofit = Retrofit.Builder()
                    .baseUrl("https://songssam.site:8443")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(
                        OkHttpClient.Builder()
                            .readTimeout(30, TimeUnit.SECONDS)
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .build()
                    )
                    .build()
                val apiService = retrofit.create(songssamAPI::class.java)
                Log.e("mp3", "songId = $uploadSongId")
                val call = apiService.uploadSongToRecord(songIdRequestBody, filePart)
                call.enqueue(object : Callback<Void> {
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e("mp3", "Call failed: ${t.message}")
                        Toast.makeText(
                            mainActivity,
                            "업로드 실패!",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                mainActivity,
                                "업로드 성공!",
                                Toast.LENGTH_LONG
                            ).show()
                            Log.d("mp3", "연결 성공")
                        } else {
                            Toast.makeText(
                                mainActivity,
                                "서버가 닫혀있습니다!",
                                Toast.LENGTH_LONG
                            ).show()
                            Log.e("mp3", "연결 실패 - Response code: ${response.code()}")
                        }
                    }
                })
            }
        } else {
            Log.d(
                "mp3",
                "if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == Activity.RESULT_OK) 실패"
            )
        }
    }


    override fun playOriginUrl(originUrl: String) {
        try {
            stopMediaPlayer()

            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer().apply {
                    val url = "https://songssam.site:8443/song/download?url=" + originUrl
                    setDataSource(url)
                    setOnPreparedListener {
                        it.start()
                    }
                    setOnErrorListener { _, _, _ ->
                        false
                    }
                    prepareAsync()
                }
            } else {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.pause()
                } else {
                    mediaPlayer?.start()
                }
            }
        } catch (e: Exception) {
            Log.e("MediaPlayer", "Error playing audio: ${e.message}")
        }
    }


    override fun onPause() {
        super.onPause()
        stopMediaPlayer()
    }

    override fun stopMediaPlayer() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
    }
}