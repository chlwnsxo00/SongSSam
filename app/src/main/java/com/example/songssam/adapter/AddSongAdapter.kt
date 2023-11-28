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

interface AddSongClick {
    fun isUpLoaded(songId: Long)
    fun isCompleted(title: String, artist: String,cover:String, songId: Long, instUrl: String)
    fun isNull(songId: Long)
    fun isProcessing()
    fun playOriginUrl(originUrl:String)
    fun stopMediaPlayer()
}

class AddSongAdapter(
    private var itemlist: MutableList<chartjsonItems>,
    private val addSongClick: AddSongClick
) :
    RecyclerView.Adapter<AddSongAdapter.TaskViewHolder>() {

    private var mediaPlayer: MediaPlayer? = null

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): TaskViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.songs, viewGroup, false)
        return TaskViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val item = itemlist[position]

        holder.artist.text = item.artist
        holder.title.text = item.title
        Glide.with(holder.itemView).load(item.coverImage).into(holder.coverImage)
        when (item.status) {
            "UPLOADED" -> {
                holder.touchImage.setImageResource(R.drawable.split)
            }

            "COMPLETE" -> {
                holder.touchImage.setImageResource(R.drawable.mic)
            }

            "NONE" -> {
                holder.touchImage.setImageResource(R.drawable.note_add)
            }

            "PROCESS" -> {
                holder.touchImage.setImageResource(R.drawable.loading)
            }
        }
        holder.touch.setOnClickListener {
            when (item.status) {
                "UPLOADED" -> {
                    addSongClick.isUpLoaded(item.songID)
                }

                "COMPLETE" -> {
                    addSongClick.isCompleted(item.title,item.artist,item.coverImage,item.songID, item.instUrl!!)
                }

                "NONE" -> {
                    addSongClick.isNull(item.songID)
                }

                "PROCESS" -> {
                    addSongClick.isProcessing()
                }
            }
        }
        holder.touch.setOnLongClickListener {
            if (item.status != "NONE") {
                Log.d("long", "playOrigin")
                addSongClick.playOriginUrl(item.originUrl!!)
            }
            true
        }
        holder.touch.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP && mediaPlayer?.isPlaying == true) {
                addSongClick.stopMediaPlayer()
            }
            false
        }
    }

    override fun getItemCount() = itemlist.size

    class TaskViewHolder(todoTaskView: View) : RecyclerView.ViewHolder(todoTaskView) {
        val title: TextView = todoTaskView.findViewById(R.id.title)
        val artist: TextView = todoTaskView.findViewById(R.id.artist)
        val coverImage: ImageView = todoTaskView.findViewById(R.id.cover)
        val touchImage: ImageView = todoTaskView.findViewById(R.id.add_button)
        val touch: ConstraintLayout = todoTaskView.findViewById(R.id.touch)
    }
}