package com.example.songssam.Fragment

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.songssam.API.SongSSamAPI.ChartJsonItem
import com.example.songssam.API.SongSSamAPI.Voice
import com.example.songssam.API.SongSSamAPI.chartjsonItems
import com.example.songssam.API.SongSSamAPI.songssamAPI
import com.example.songssam.Activitys.MainActivity
import com.example.songssam.R
import com.example.songssam.adapter.GenerateAIAdapter
import com.example.songssam.adapter.generateInterface
import com.example.songssam.databinding.FragmentHomeBinding
import `in`.srain.cube.views.ptr.PtrDefaultHandler
import `in`.srain.cube.views.ptr.PtrFrameLayout
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment() ,generateInterface{
    lateinit var binding: FragmentHomeBinding
    private var itemList = mutableListOf<chartjsonItems>()
    private var generatedItemList = mutableListOf<chartjsonItems>()
    private var generatedItemUrlPair = mutableListOf<Pair<Long, String>>()
    private var sampleVoiceList = mutableListOf<Voice>()
    private lateinit var songAdapter: GenerateAIAdapter
    private var voiceId: Long = 3
    private var mediaPlayer: MediaPlayer? = null

    private val mainActivity: MainActivity by lazy {
        context as MainActivity
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater)
        getCompletedList()
        initFloatingButton()
        initRefreshLayout()
        return binding.root
    }

    private fun getGeneratedSongList(ptrId: Long) {
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
        val call = apiService.getGeneratedSongList(ptrId)
        call.enqueue(object : Callback<List<ChartJsonItem>> {
            override fun onResponse(
                call: Call<List<ChartJsonItem>>,
                response: Response<List<ChartJsonItem>>
            ) {
                if (response.isSuccessful.not()) {
                    Toast.makeText(
                        mainActivity,
                        "서버가 닫혀있습니다!",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.d("generatedItemList", "연결 실패")
                    return
                }
                Log.d("generatedItemList", "로그인 연결 성공")
                try {
                    generatedItemList.clear()
                    generatedItemUrlPair.clear()
                    response.body()?.all {
                        generatedItemList.add(it.song)
                        generatedItemUrlPair.add(Pair(it.song.songID, it.generatedUrl))
                        true
                    }
                    Log.d("done","done")
                    Thread(Runnable {
                        mainActivity.runOnUiThread {
                            initRecyclerView()
                        }
                    }).start()
                } catch (e: Exception) {
                }
            }

            override fun onFailure(call: Call<List<ChartJsonItem>>, t: Throwable) {
                Log.d("generatedItemList", t.stackTraceToString())
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
                    Log.d("itemList", "연결 실패")
                    return
                }
                Log.d("itemList", "로그인 연결 성공")
                try {
                    itemList = response.body()?.toMutableList() ?: mutableListOf()
                    getSampleVoice()
                    Log.d("itemList", itemList.toString())
                } catch (e: Exception) {
                }
            }

            override fun onFailure(call: Call<List<chartjsonItems>>, t: Throwable) {
                Log.d("itemList", t.stackTraceToString())
                Toast.makeText(
                    mainActivity,
                    "네트워크 오류와 같은 이유로 오류 발생!",
                    Toast.LENGTH_LONG
                ).show()
                // 네트워크 오류 등 호출 실패 시 처리
            }
        })
    }

    private fun getRefreshedCompletedList() {
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
                    Log.d("itemList", "연결 실패")
                    return
                }
                Log.d("itemList", "로그인 연결 성공")
                try {
                    itemList = response.body()?.toMutableList() ?: mutableListOf()
                    Log.d("itemList", itemList.toString())
                    getGeneratedSongList(voiceId)
                } catch (e: Exception) {
                }
            }

            override fun onFailure(call: Call<List<chartjsonItems>>, t: Throwable) {
                Log.d("itemList", t.stackTraceToString())
                Toast.makeText(
                    mainActivity,
                    "네트워크 오류와 같은 이유로 오류 발생!",
                    Toast.LENGTH_LONG
                ).show()
                // 네트워크 오류 등 호출 실패 시 처리
            }
        })
    }
    private fun getSampleVoice() {
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
        val call = apiService.getSampleVoiceList()
        call.enqueue(object : Callback<List<Voice>> {
            override fun onResponse(
                call: Call<List<Voice>>,
                response: Response<List<Voice>>
            ) {
                if (response.isSuccessful.not()) {
                    Toast.makeText(
                        mainActivity,
                        "서버가 닫혀있습니다!",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.d("voice", "연결 실패")
                    return
                }
                Log.d("voice", "로그인 연결 성공")
                try {
                    sampleVoiceList = response.body()?.toMutableList() ?: mutableListOf()
                    Log.d("voice", sampleVoiceList.toString())
                    initSpinner()
                } catch (e: Exception) {
                    Log.d("voice", e.stackTraceToString())
                }
            }

            override fun onFailure(call: Call<List<Voice>>, t: Throwable) {
                Log.d("voice", t.stackTraceToString())
                Toast.makeText(
                    mainActivity,
                    "네트워크 오류와 같은 이유로 오류 발생!",
                    Toast.LENGTH_LONG
                ).show()
                // 네트워크 오류 등 호출 실패 시 처리
            }
        })
    }

    private fun initRecyclerView() {
        // generatedItemList에 포함된 아이템을 앞으로 이동시키기
        val itemsInGeneratedList = itemList.filter { it in generatedItemList }
        val itemsNotInGeneratedList = itemList.filterNot { it in generatedItemList }

        // 앞으로 이동시킨 아이템과 그렇지 않은 아이템을 합쳐서 새로운 리스트 생성
        val reorderedItemList = mutableListOf<chartjsonItems>().apply {
            addAll(itemsInGeneratedList)
            addAll(itemsNotInGeneratedList)
        }

        binding.rv.layoutManager =
            LinearLayoutManager(binding.root.context, LinearLayoutManager.VERTICAL, false)

        songAdapter = GenerateAIAdapter(
            reorderedItemList,
            generatedItemList,
            generatedItemUrlPair,
            voiceId,
            this
        )
        binding.rv.adapter = songAdapter
    }

    private fun initSpinner() {
        val spinner = binding.voiceSpinner
        val dataArray = mutableListOf<String>()
        Log.d("spinner", sampleVoiceList.toString())
        sampleVoiceList.all {
            dataArray.add(it.name)
        }
        Log.d("spinner", dataArray.toString())
        val adapter: ArrayAdapter<String> = ArrayAdapter(
            mainActivity,
            soup.neumorphism.R.layout.support_simple_spinner_dropdown_item, dataArray
        )

        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val num = sampleVoiceList.find {
                    it.name == dataArray[position]
                }!!.id
                voiceId = num
                getRefreshedCompletedList()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                voiceId = sampleVoiceList.first().id
                getGeneratedSongList(voiceId)
            }
        }
    }



    override fun successRequest() {
        view?.post {
            Toast.makeText(
                requireContext(),
                "AI커버 생성 요청 전송 완료!⭕",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    override fun failRequest(){
        view?.post {
            Toast.makeText(
                requireContext(),
                "AI커버 생성 요청 전송 실패!❌",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun playGeneratedUrl(generatedUrl: String) {
        try {
            stopMediaPlayer()
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer().apply {
                    val url = "https://songssam.site:8443/song/download?url=" + generatedUrl
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
                    stopMediaPlayer()
                } else {
                    mediaPlayer?.start()
                }
            }
        } catch (e: Exception) {
            Log.e("MediaPlayer", "Error playing audio: ${e.message}")
        }
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
    override fun onPause() {
        super.onPause()
        stopMediaPlayer()
    }


    private fun initFloatingButton() {
        binding.fab.setOnClickListener {
            binding.nsv.smoothScrollTo(0, 0)
        }
    }

    private fun initRefreshLayout() {
        binding.refreshLayout.setPtrHandler(object : PtrDefaultHandler() {
            override fun onRefreshBegin(frame: PtrFrameLayout) {
                getRefreshedCompletedList()
                // 예를 들어, 데이터를 다시 로드하거나 업데이트할 수 있습니다.
                binding.refreshLayout.postDelayed({
                    // 리프레시 완료 후 호출
                    binding.refreshLayout.refreshComplete()
                }, 1000) // 예시: 2초 후 리프레시 완료
            }
        })
    }
}