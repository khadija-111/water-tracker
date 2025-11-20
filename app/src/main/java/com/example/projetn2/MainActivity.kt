package com.example.projetn2

import android.animation.ArgbEvaluator
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestNotificationPermission()
        createAuthChannel()
        startAuthWorkManager()
        scheduleNotifications()

        initViews()
        setupViewPager()
        setupTabAnimations()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }
    }

    private fun createAuthChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "auth_channel",
                "Rappel Hydratation",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications toutes les 15 min pour boire de l'eau"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun startAuthWorkManager() {
        val workRequest = PeriodicWorkRequestBuilder<AuthWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "auth_reminder",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    private fun scheduleNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val interval = 15 * 60 * 1000L // 15 minutes
        val triggerAt = System.currentTimeMillis() + interval

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            interval,
            pendingIntent
        )
    }

    private fun initViews() {
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
    }

    private fun setupViewPager() {
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 3

        val tabData = arrayOf(
            TabData("📊", "Dashboard"),
            TabData("🎯", "Objectifs"),
            TabData("💡", "Conseils"),

            TabData("👤", "Compte")
        )

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val data = tabData[position]
            tab.text = "${data.emoji} ${data.title}"
            tab.view.setOnClickListener {
                animateTabClick(it)
                viewPager.currentItem = position
            }
        }.attach()

        tabLayout.selectTab(tabLayout.getTabAt(0))
    }

    private fun setupTabAnimations() {
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, pixels: Int) {
                super.onPageScrolled(position, positionOffset, pixels)
                if (position < tabLayout.tabCount - 1) {
                    val startColor = getColorForPosition(position)
                    val endColor = getColorForPosition(position + 1)
                    val evaluator = ArgbEvaluator()
                    val color = evaluator.evaluate(positionOffset, startColor, endColor) as Int
                    viewPager.setBackgroundColor(
                        Color.argb(20, Color.red(color), Color.green(color), Color.blue(color))
                    )
                }
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                animateTabSelection(position)
            }
        })
    }

    private fun animateTabClick(view: View) {
        view.animate().scaleX(0.92f).scaleY(0.92f).setDuration(100).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
        }.start()
    }

    private fun animateTabSelection(position: Int) {
        tabLayout.getTabAt(position)?.view?.let { view ->
            view.animate().scaleX(1.05f).scaleY(1.05f).setDuration(200).withEndAction {
                for (i in 0 until tabLayout.tabCount) {
                    if (i != position) {
                        tabLayout.getTabAt(i)?.view?.animate()?.scaleX(1f)?.scaleY(1f)?.setDuration(200)?.start()
                    }
                }
            }.start()
        }
    }

    private fun getColorForPosition(position: Int): Int = when (position) {
        0 -> Color.parseColor("#1E88E5")
        1 -> Color.parseColor("#4ECDC4")
        2 -> Color.parseColor("#FFE66D")
        3 -> Color.parseColor("#FF6B6B")
        else -> Color.parseColor("#1E88E5")
    }

    data class TabData(val emoji: String, val title: String)
}