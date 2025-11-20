

package com.example.projetn2

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager

class SplashOnboardingActivity : AppCompatActivity() {

    private lateinit var splashLayout: LinearLayout
    private lateinit var onboardingLayout: LinearLayout
    private lateinit var tvLogo: TextView
    private lateinit var tvAppName: TextView
    private lateinit var viewPager: ViewPager
    private lateinit var btnStart: Button
    private lateinit var dotsLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_onboarding)

        initViews()
        animateSplash()

        btnStart.setOnClickListener {
            animateButtonClick(it)
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, LoginActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }, 200)
        }
    }

    private fun initViews() {
        splashLayout = findViewById(R.id.splashLayout)
        onboardingLayout = findViewById(R.id.onboardingLayout)
        tvLogo = findViewById(R.id.tvLogo)
        tvAppName = findViewById(R.id.tvAppName)
        viewPager = findViewById(R.id.viewPager)
        btnStart = findViewById(R.id.btnStart)
        dotsLayout = findViewById(R.id.dotsLayout)
    }

    private fun animateSplash() {
        // Animation du logo avec bounce
        val logoScale = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(tvLogo, "scaleX", 0f, 1.2f, 1f).apply {
                    duration = 1000
                    interpolator = OvershootInterpolator(1.5f)
                },
                ObjectAnimator.ofFloat(tvLogo, "scaleY", 0f, 1.2f, 1f).apply {
                    duration = 1000
                    interpolator = OvershootInterpolator(1.5f)
                },
                ObjectAnimator.ofFloat(tvLogo, "rotation", 0f, 360f).apply {
                    duration = 1000
                }
            )
        }

        // Animation du nom avec slide + fade
        val nameAnim = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(tvAppName, "alpha", 0f, 1f).apply {
                    duration = 800
                    startDelay = 500
                },
                ObjectAnimator.ofFloat(tvAppName, "translationY", 100f, 0f).apply {
                    duration = 800
                    startDelay = 500
                    interpolator = AccelerateDecelerateInterpolator()
                }
            )
        }

        logoScale.start()
        nameAnim.start()

        // Transition avec fade
        Handler(Looper.getMainLooper()).postDelayed({
            ObjectAnimator.ofFloat(splashLayout, "alpha", 1f, 0f).apply {
                duration = 600
                start()
            }

            Handler(Looper.getMainLooper()).postDelayed({
                splashLayout.visibility = View.GONE
                onboardingLayout.visibility = View.VISIBLE
                onboardingLayout.alpha = 0f
                ObjectAnimator.ofFloat(onboardingLayout, "alpha", 0f, 1f).apply {
                    duration = 600
                    start()
                }
                setupViewPager()
            }, 600)
        }, 2800)
    }

    private fun setupViewPager() {
        val pages = listOf(
            OnboardingPage("Bienvenue", "Suivez votre hydratation", "💧", "#1E88E5"),
            OnboardingPage("Objectifs", "Atteignez vos objectifs", "🎯", "#4ECDC4"),
            OnboardingPage("Statistiques", "Suivez vos progrès", "📊", "#FF6B6B")
        )

        viewPager.adapter = OnboardingPagerAdapter(pages)
        setupDots(pages.size)

        btnStart.visibility = View.GONE
        btnStart.alpha = 0f

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                updateDotsOnScroll(position, positionOffset)
            }

            override fun onPageSelected(position: Int) {
                updateDots(position)

                if (position == pages.size - 1) {
                    btnStart.visibility = View.VISIBLE
                    ObjectAnimator.ofFloat(btnStart, "alpha", 0f, 1f).apply {
                        duration = 500
                        start()
                    }
                    ObjectAnimator.ofFloat(btnStart, "translationY", 50f, 0f).apply {
                        duration = 500
                        interpolator = OvershootInterpolator()
                        start()
                    }
                } else {
                    ObjectAnimator.ofFloat(btnStart, "alpha", 1f, 0f).apply {
                        duration = 300
                        start()
                    }
                    Handler(Looper.getMainLooper()).postDelayed({
                        btnStart.visibility = View.GONE
                    }, 300)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        viewPager.post {
            updateDots(0)
        }
    }

    private fun setupDots(count: Int) {
        dotsLayout.removeAllViews()

        for (i in 0 until count) {
            val dot = TextView(this).apply {
                text = "●"
                textSize = 14f
                setTextColor(Color.WHITE)
                alpha = 0.3f

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(12, 0, 12, 0)
                }
                layoutParams = params
            }
            dotsLayout.addView(dot)
        }
    }

    private fun updateDots(position: Int) {
        for (i in 0 until dotsLayout.childCount) {
            val dot = dotsLayout.getChildAt(i) as TextView

            if (i == position) {
                ObjectAnimator.ofFloat(dot, "alpha", dot.alpha, 1f).apply {
                    duration = 300
                    start()
                }
                ObjectAnimator.ofFloat(dot, "scaleX", dot.scaleX, 1.8f).apply {
                    duration = 300
                    start()
                }
                ObjectAnimator.ofFloat(dot, "scaleY", dot.scaleY, 1.8f).apply {
                    duration = 300
                    start()
                }
            } else {
                ObjectAnimator.ofFloat(dot, "alpha", dot.alpha, 0.3f).apply {
                    duration = 300
                    start()
                }
                ObjectAnimator.ofFloat(dot, "scaleX", dot.scaleX, 1f).apply {
                    duration = 300
                    start()
                }
                ObjectAnimator.ofFloat(dot, "scaleY", dot.scaleY, 1f).apply {
                    duration = 300
                    start()
                }
            }
        }
    }

    private fun updateDotsOnScroll(position: Int, positionOffset: Float) {
        if (position < dotsLayout.childCount - 1) {
            val currentDot = dotsLayout.getChildAt(position) as TextView
            val nextDot = dotsLayout.getChildAt(position + 1) as TextView

            currentDot.alpha = 1f - (positionOffset * 0.7f)
            nextDot.alpha = 0.3f + (positionOffset * 0.7f)

            val currentScale = 1.8f - (positionOffset * 0.8f)
            val nextScale = 1f + (positionOffset * 0.8f)

            currentDot.scaleX = currentScale
            currentDot.scaleY = currentScale
            nextDot.scaleX = nextScale
            nextDot.scaleY = nextScale
        }
    }

    private fun animateButtonClick(view: View) {
        val scaleDown = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.92f),
                ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.92f)
            )
            duration = 100
        }

        val scaleUp = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 0.92f, 1f),
                ObjectAnimator.ofFloat(view, "scaleY", 0.92f, 1f)
            )
            duration = 100
        }

        scaleDown.start()
        Handler(Looper.getMainLooper()).postDelayed({
            scaleUp.start()
        }, 100)
    }

    data class OnboardingPage(
        val title: String,
        val description: String,
        val logo: String,
        val color: String
    )

    inner class OnboardingPagerAdapter(private val pages: List<OnboardingPage>) : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val inflater = LayoutInflater.from(container.context)
            val layout = inflater.inflate(R.layout.onboarding_page, container, false)

            val tvLogo = layout.findViewById<TextView>(R.id.tvLogoPage)
            val tvTitle = layout.findViewById<TextView>(R.id.tvTitlePage)
            val tvDesc = layout.findViewById<TextView>(R.id.tvDescPage)

            val page = pages[position]
            tvLogo.text = page.logo
            tvTitle.text = page.title
            tvDesc.text = page.description

            // Animation d'entrée
            layout.alpha = 0f
            layout.translationY = 80f

            layout.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(700)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setStartDelay(150)
                .start()

            // Animation du logo
            tvLogo.apply {
                scaleX = 0f
                scaleY = 0f
                rotation = -180f
            }

            tvLogo.animate()
                .scaleX(1f)
                .scaleY(1f)
                .rotation(0f)
                .setDuration(900)
                .setInterpolator(OvershootInterpolator(1.2f))
                .setStartDelay(300)
                .start()

            container.addView(layout)
            return layout
        }

        override fun getCount(): Int = pages.size
        override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj
        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            container.removeView(obj as View)
        }
    }
}