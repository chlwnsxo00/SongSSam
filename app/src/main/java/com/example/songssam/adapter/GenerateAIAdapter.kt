package com.example.songssam.adapter

import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.songssam.API.SongSSamAPI.chartjsonItems
import com.example.songssam.R
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

interface generateInterface {
    fun successRequest()
    fun failRequest()

    fun playGeneratedUrl(generatedUrl: String)
    fun stopMediaPlayer()
}

class GenerateAIAdapter(
    private var itemlist: MutableList<chartjsonItems>,
    private var generatedItemList: MutableList<chartjsonItems>,
    private var generatedItemUrlPair: MutableList<Pair<Long, String>>,
    private var voiceId: Long,
    private val generateInterface: generateInterface
) :
    RecyclerView.Adapter<GenerateAIAdapter.TaskViewHolder>() {


    private var selectedItem: chartjsonItems? = null
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.generate_cover_item, viewGroup, false)
        return TaskViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemlist.size
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val item = itemlist[position]

        holder.artist.text = item.artist
        holder.title.text = item.title
        Glide.with(holder.itemView).load(item.coverImage).into(holder.coverImage)

        if (selectedItem == null) {
            if (generatedItemList.contains(item)) {
                holder.touchImage.setImageResource(R.drawable.hear)
            }
        } else {
            if (generatedItemList.contains(item) && selectedItem != item) {
                holder.touchImage.setImageResource(R.drawable.hear)
            } else if (generatedItemList.contains(item) && selectedItem == item) {
                holder.touchImage.setImageResource(R.drawable.hearoff)
            }
        }
        holder.touch.setOnClickListener {
            if (selectedItem == null && generatedItemList.contains(item)) {
                holder.touchImage.setImageResource(R.drawable.hearoff)
                val url = generatedItemUrlPair.first { it.first == item.songID }.second
                generateInterface.playGeneratedUrl(url)
                selectedItem = item
            } else if (generatedItemList.contains(item)) {
                selectedItem = if (selectedItem != item) {
                    generateInterface.stopMediaPlayer()
                    selectedItem?.let {
                        // 기존에 선택된 아이템을 선택 해제
                        val index = itemlist.indexOf(it)
                        if (index != RecyclerView.NO_POSITION) {
                            notifyItemChanged(index)
                        }
                    }
                    holder.touchImage.setImageResource(R.drawable.hearoff)
                    val url = generatedItemUrlPair.first { it.first == item.songID }.second
                    generateInterface.playGeneratedUrl(url)
                    item
                } else {
                    holder.touchImage.setImageResource(R.drawable.hear)
                    generateInterface.stopMediaPlayer()
                    null
                }
            } else {
                sendPostRequest(makeJson(voiceId, item.songID))
            }
        }
    }
    // JSON 데이터 준비

    private fun makeJson(voiceId: Long, songId: Long): String {
        return "{\"targetVoiceId\":\"$voiceId\", \"targetSongId\":\"$songId\"}"
    }

    fun sendPostRequest(jsonData: String) {
        Thread {
            val client = OkHttpClient()

            val mediaType = "application/json; charset=UTF-8".toMediaType()
            val requestBody = jsonData.toRequestBody(mediaType)

            val request = Request.Builder()
                .url("https://songssam.site:8443/ddsp/makesong")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    // 서버 응답(responseBody) 처리
                    generateInterface.successRequest()
                } else {
                    generateInterface.failRequest()
                    // 에러 처리
                }
            }
        }.start()
    }

    class TaskViewHolder(todoTaskView: View) : RecyclerView.ViewHolder(todoTaskView) {
        val title: TextView = todoTaskView.findViewById(R.id.title)
        val artist: TextView = todoTaskView.findViewById(R.id.artist)
        val coverImage: ImageView = todoTaskView.findViewById(R.id.cover)
        val touch: ConstraintLayout = todoTaskView.findViewById(R.id.touch)
        val touchImage: ImageView = todoTaskView.findViewById(R.id.add_button)
    }
}