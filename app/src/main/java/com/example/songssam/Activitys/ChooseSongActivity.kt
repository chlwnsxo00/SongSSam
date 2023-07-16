package com.example.songssam.Activitys

import android.content.ContentValues.TAG
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.DocumentsContract.Document
import android.util.Log
import android.widget.Adapter
import android.widget.GridView
import androidx.appcompat.app.AppCompatActivity
import com.example.songssam.R
import com.example.songssam.data.ChooseSongGridAdapter
import com.example.songssam.data.ChooseSongGridItem
import org.jsoup.Jsoup
import org.jsoup.select.Elements


class ChooseSongActivity : AppCompatActivity() {

    private var itemList : ArrayList<ChooseSongGridItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_song)
        crawlingstart()
    }

    private fun initGridView() {
        val gridView : GridView = findViewById(R.id.gridView)
        val adapter = ChooseSongGridAdapter(this@ChooseSongActivity,itemList)
        gridView.adapter = adapter
    }

    private fun crawlingstart() {
        Thread(Runnable{
            val doc = Jsoup.connect("https://www.melon.com/chart/index.htm").userAgent("Chrome").get()
            val elements : Elements = doc.select(".lst50")
            // mobile-padding 클래스의 board-list의 id를 가진 것들을 elements 객체에 저장
            /*
            크롤링 하는 법 : class 는 .(class) 로 찾고 id 는 #(id) 로 검색
             */

            for(elements in elements){  //elements의 개수만큼 반복
                val coverImage = elements.select(".image_typeAll img").attr("src")
                val title = elements.select(".wrap_song_info .rank01 span a").text()
                val artist = limitchars(elements.select(".wrap_song_info .rank02 span").text())
                itemList.add(ChooseSongGridItem(coverImage,title,artist))     //위에서 크롤링 한 내용들을 itemlist에 추가
                Log.i(TAG,"item추가"+ title + artist + coverImage)
            }

            runOnUiThread{
                initGridView()
            }
        }).start()
    }
    private fun limitchars(text:String):String{
        if(text.length>14){
            return text.substring(0,12) + ".."
        }
        return text
    }
}
