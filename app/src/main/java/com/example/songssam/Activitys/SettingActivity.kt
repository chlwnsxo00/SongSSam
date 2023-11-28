package com.example.songssam.Activitys

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.songssam.R

class SettingActivity : AppCompatActivity() {

    private val logoutContainer : ConstraintLayout by lazy {
        findViewById(R.id.logout_container)
    }

    private val deleteAccountContainer : ConstraintLayout by lazy {
        findViewById(R.id.delete_account_container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        initLogoutClick()
        initDeleteAccountClick()
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

    private fun initDeleteAccountClick() {
        logoutContainer.setOnClickListener {
            GlobalApplication.prefs.setString("accessToken","")
            startActivity(Intent(this@SettingActivity,LoginActivity::class.java))
        }
    }

    private fun initLogoutClick() {
        deleteAccountContainer.setOnClickListener {

        }
    }
}