package com.example.projetn2

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.projetn2.data.AppDatabase
import com.example.projetn2.data.GoalEntry
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ObjectifsFragment : Fragment() {

    private lateinit var db: AppDatabase

    private var dailyGoal = 2.0f
    private var currentConsumption = 0f
    private val history = mutableListOf<String>()

    private lateinit var etGoal: EditText
    private lateinit var btnSetGoal: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var progressOverlay: View
    private lateinit var tvProgress: TextView
    private lateinit var tvGoalDisplay: TextView
    private lateinit var tvCurrentConsumption: TextView
    private lateinit var tvRemainingConsumption: TextView
    private lateinit var tvHistoryCount: TextView
    private lateinit var emptyState: LinearLayout
    private lateinit var lvHistory: ListView
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_objectifs, container, false)

        db = AppDatabase.getDatabase(requireContext())

        initViews(view)
        setupAdapter()
        setupListeners()
        loadData()

        return view
    }

    private fun initViews(view: View) {
        progressBar = view.findViewById(R.id.progressBar)
        progressOverlay = view.findViewById(R.id.progressOverlay)
        tvProgress = view.findViewById(R.id.tvProgress)
        etGoal = view.findViewById(R.id.etGoal)
        btnSetGoal = view.findViewById(R.id.btnSetGoal)
        tvGoalDisplay = view.findViewById(R.id.tvGoalDisplay)
        tvCurrentConsumption = view.findViewById(R.id.tvCurrentConsumption)
        tvRemainingConsumption = view.findViewById(R.id.tvRemainingConsumption)
        tvHistoryCount = view.findViewById(R.id.tvHistoryCount)
        emptyState = view.findViewById(R.id.emptyState)
        lvHistory = view.findViewById(R.id.lvHistory)
    }

    private fun setupAdapter() {
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, history)
        lvHistory.adapter = adapter
        lvHistory.isNestedScrollingEnabled = true
    }

    private fun setupListeners() {
        btnSetGoal.setOnClickListener { handleSetGoal() }
    }

    private fun getCurrentUserEmail(): String {
        return requireContext().getSharedPreferences("USER_AUTH", Context.MODE_PRIVATE)
            .getString("current_user_email", "") ?: ""
    }

    private fun handleSetGoal() {
        val input = etGoal.text.toString().trim()
        if (input.isEmpty()) {
            showSnackbar("❌ Veuillez entrer un objectif", "#FF5722")
            return
        }

        val newGoal = input.toFloatOrNull()
        if (newGoal == null || newGoal <= 0) {
            showSnackbar("❌ Objectif invalide", "#FF5722")
            return
        }

        dailyGoal = newGoal
        val email = getCurrentUserEmail()

        lifecycleScope.launch(Dispatchers.IO) {
            db.goalDao().insertGoal(GoalEntry(userEmail = email, dailyGoal = newGoal))

            withContext(Dispatchers.Main) {
                loadData()
                updateStats()
                updateProgress()
                addToHistory(newGoal)
                etGoal.text.clear()
                showSnackbar("✓ Objectif défini: ${newGoal}L", "#4CAF50")
            }
        }
    }
    private fun loadWeeklyHistory() {
        val email = getCurrentUserEmail()
        lifecycleScope.launch {
            val weekEntries = withContext(Dispatchers.IO) {
                db.waterDao().getLast7Days(email)
            }
            history.clear()
            weekEntries.forEach { entry ->
                val percent = ((entry.amount / dailyGoal) * 100).coerceIn(0f, 100f).toInt()
                val status = when {
                    percent >= 100 -> "✓ Atteint"
                    percent >= 75 -> "⚡ Presque"
                    percent >= 50 -> "📈 En cours"
                    else -> "❌ Non atteint"
                }
                history.add("Jour ${entry.dayIndex} → ${entry.amount}L → $status ($percent%)")
            }
            adapter.notifyDataSetChanged()
            updateHistoryVisibility()
        }
    }

    private fun addToHistory(goal: Float) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        val percent = ((currentConsumption / dailyGoal) * 100).coerceIn(0f, 100f).toInt()

        val status = when {
            percent >= 100 -> "✓ Atteint"
            percent >= 75 -> "⚡ Presque"
            percent >= 50 -> "📈 En cours"
            else -> "❌ Non atteint"
        }

        val entry = "$currentDate\nObjectif: ${goal}L → $status ($percent%)"
        history.add(0, entry)
        if (history.size > 20) history.removeAt(history.size - 1)
        adapter.notifyDataSetChanged()
    }

    private fun loadData() {
        val email = getCurrentUserEmail()
        lifecycleScope.launch {
            val latestGoal = withContext(Dispatchers.IO) { db.goalDao().getLatestGoal(email) }
            dailyGoal = latestGoal?.dailyGoal ?: 2.0f
            loadWeeklyHistory()
            val todayIndex = getTodayIndex()
            val totalConsumptionToday = withContext(Dispatchers.IO) {
                db.waterDao().getTotalForToday(email, todayIndex)
            }
            currentConsumption = totalConsumptionToday ?: 0f

            updateUI()
        }
    }

    private fun getTodayIndex(): Int {
        val calendar = Calendar.getInstance()
        return (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
    }

    private fun updateUI() {
        val percent = ((currentConsumption / dailyGoal) * 100).coerceIn(0f, 100f)
        progressBar.progress = percent.toInt()
        tvProgress.text = "${percent.toInt()}% atteint"
        updateStats()
        updateHistoryVisibility()
        updateProgress()
    }

    private fun updateProgress() {
        val percent = ((currentConsumption / dailyGoal) * 100).coerceIn(0f, 100f)

        animateProgress(percent.toInt())

        val emoji = when {
            percent >= 100 -> "🏆"
            percent >= 75 -> "💪"
            percent >= 50 -> "👍"
            percent >= 25 -> "📈"
            else -> "⏳"
        }
        tvProgress.text = "$emoji ${percent.toInt()}% atteint"

        val color = when {
            percent >= 100 -> "#4CAF50"
            percent >= 75 -> "#8BC34A"
            percent >= 50 -> "#FFC107"
            percent >= 25 -> "#FF9800"
            else -> "#FF5722"
        }
        tvProgress.setTextColor(Color.parseColor(color))
    }

    private fun animateProgress(targetPercent: Int) {
        val currentProgress = progressBar.progress
        val animator = ObjectAnimator.ofInt(progressBar, "progress", currentProgress, targetPercent)
        animator.duration = 1000
        animator.interpolator = DecelerateInterpolator()
        animator.start()

        progressOverlay.post {
            val parentView = progressOverlay.parent as? View
            if (parentView != null && parentView.width > 0) {
                val layoutParams = progressOverlay.layoutParams
                val currentWidth = layoutParams.width
                val targetWidth = (parentView.width * targetPercent / 100).coerceAtLeast(1)
                val widthAnimator = ValueAnimator.ofInt(currentWidth, targetWidth)
                widthAnimator.duration = 1000
                widthAnimator.interpolator = DecelerateInterpolator()
                widthAnimator.addUpdateListener { animation ->
                    layoutParams.width = animation.animatedValue as Int
                    progressOverlay.layoutParams = layoutParams
                }
                widthAnimator.start()
            }
        }
    }

    private fun updateStats() {
        tvGoalDisplay.text = "Objectif: %.1fL".format(dailyGoal)
        tvCurrentConsumption.text = "%.2fL".format(currentConsumption)
        val remaining = (dailyGoal - currentConsumption).coerceAtLeast(0f)
        tvRemainingConsumption.text = "Reste: %.2fL".format(remaining)
        tvHistoryCount.text = history.size.toString()
    }

    private fun updateHistoryVisibility() {
        if (history.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            lvHistory.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            lvHistory.visibility = View.VISIBLE
        }
    }

    private fun showSnackbar(message: String, colorHex: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(Color.parseColor(colorHex))
                .setTextColor(Color.WHITE)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
        loadWeeklyHistory()
    }
}
