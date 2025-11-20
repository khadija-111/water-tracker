package com.example.projetn2

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.content.Context
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.projetn2.data.AppDatabase
import com.example.projetn2.data.WaterEntry
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private var dailyGoal = 2f
    private val weekConsumption = MutableList(7) { 0f }

    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    private lateinit var tvConsumption: TextView
    private lateinit var tvMotivation: TextView
    private lateinit var tvAverage: TextView
    private lateinit var tvMaxDay: TextView
    private lateinit var tvGoal: TextView
    private lateinit var tvDate: TextView
    private lateinit var progressBar: View
    private lateinit var cardMain: CardView
    private lateinit var cardStats: CardView
    private lateinit var cardBarChart: CardView

    private lateinit var db: AppDatabase

    private companion object {
        val DAYS = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")

        const val COLOR_PRIMARY = "#3B82F6"
        const val COLOR_SUCCESS = "#10B981"
        const val COLOR_WARNING = "#F59E0B"
        const val COLOR_DANGER = "#EF4444"
        const val COLOR_PURPLE = "#8B5CF6"
        const val COLOR_BACKGROUND = "#F0F4F8"
        const val COLOR_CARD = "#FFFFFF"
        const val COLOR_TEXT_PRIMARY = "#1A2332"
        const val COLOR_TEXT_SECONDARY = "#64748B"
        const val COLOR_TEXT_TERTIARY = "#94A3B8"
        const val COLOR_PROGRESS_BG = "#F1F5F9"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        initViews(view)
        db = AppDatabase.getDatabase(requireContext())
        updateDate()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners(view)
        loadData()
        animateCardsEntrance()
    }

    private fun initViews(view: View) {
        pieChart = view.findViewById(R.id.pieChart)
        barChart = view.findViewById(R.id.barChart)
        tvConsumption = view.findViewById(R.id.tvConsumption)
        tvMotivation = view.findViewById(R.id.tvMotivation)
        tvAverage = view.findViewById(R.id.tvAverage)
        tvMaxDay = view.findViewById(R.id.tvMaxDay)
        tvGoal = view.findViewById(R.id.tvGoal)
        tvDate = view.findViewById(R.id.tvDate)
        progressBar = view.findViewById(R.id.progressBar)
        cardMain = view.findViewById(R.id.cardMain)
        cardStats = view.findViewById(R.id.cardStats)
        cardBarChart = view.findViewById(R.id.cardBarChart)
    }

    private fun setupListeners(view: View) {
        view.findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            showModernAmountDialog()
        }
    }

    private fun updateDate() {
        val dateFormat = SimpleDateFormat("EEEE d MMMM", Locale.FRENCH)
        val currentDate = dateFormat.format(Date())
        tvDate.text = currentDate.replaceFirstChar { it.uppercase() }
    }

    private fun getTodayIndex(): Int {
        val calendar = Calendar.getInstance()
        return (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
    }

    private fun getCurrentUserEmail(): String {
        return requireContext().getSharedPreferences("USER_AUTH", Context.MODE_PRIVATE)
            .getString("current_user_email", "") ?: ""
    }

    private fun addWater(amount: Float, displayAmount: String) {
        val todayIndex = getTodayIndex()
        val email = getCurrentUserEmail()

        weekConsumption[todayIndex] += amount

        lifecycleScope.launch {
            // ✅ CORRECTION : Utiliser la même logique que ObjectifsFragment
            // Au lieu de remplacer, on devrait ajouter à la consommation existante
            val currentTotal = withContext(Dispatchers.IO) {
                db.waterDao().getTotalForToday(email, todayIndex) ?: 0f
            }

            val newTotal = currentTotal + amount

            withContext(Dispatchers.IO) {
                db.waterDao().insert(
                    WaterEntry(userEmail = email, dayIndex = todayIndex, amount = newTotal)
                )
            }

            // Recharger les données
            loadData()

            view?.let {
                Snackbar.make(it, "✨ $displayAmount ajouté avec succès!", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.parseColor(COLOR_SUCCESS))
                    .setTextColor(Color.WHITE)
                    .show()
            }
        }
    }

    private fun showModernAmountDialog() {
        val context = requireContext()
        val input = EditText(context).apply {
            hint = "Ex: 250, 500, 750..."
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setPadding(60, 50, 60, 50)
            textSize = 18f
            setTextColor(Color.parseColor(COLOR_TEXT_PRIMARY))
            setHintTextColor(Color.parseColor(COLOR_TEXT_TERTIARY))
            background = null
        }

        val dialog = AlertDialog.Builder(context)
            .setTitle("💧 Ajouter de l'eau")
            .setMessage("Entrez la quantité en millilitres")
            .setView(input)
            .setPositiveButton("Ajouter") { _, _ ->
                val amount = input.text.toString().toFloatOrNull()
                if (amount != null && amount > 0 && amount <= 5000) {
                    addWater(amount / 1000f, "${amount.toInt()}ml")
                } else {
                    view?.let {
                        Snackbar.make(it, "⚠️ Quantité invalide (1-5000ml)", Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(Color.parseColor(COLOR_DANGER))
                            .setTextColor(Color.WHITE)
                            .show()
                    }
                }
            }
            .setNegativeButton("Annuler", null)
            .create()

        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.parseColor(COLOR_PRIMARY))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.parseColor(COLOR_TEXT_SECONDARY))
    }

    private fun loadData() {
        val email = getCurrentUserEmail()

        android.util.Log.d("DashboardFragment", "=== LOADING DATA ===")
        android.util.Log.d("DashboardFragment", "Email: $email")

        lifecycleScope.launch {
            // ✅ ÉTAPE 1 : Charger l'objectif depuis la base de données
            val latestGoal = withContext(Dispatchers.IO) {
                db.goalDao().getLatestGoal(email)
            }

            dailyGoal = latestGoal?.dailyGoal ?: 2.0f
            android.util.Log.d("DashboardFragment", "Daily Goal: $dailyGoal L")

            // ✅ ÉTAPE 2 : Charger UNIQUEMENT la consommation d'aujourd'hui
            val todayIndex = getTodayIndex()
            android.util.Log.d("DashboardFragment", "Today Index: $todayIndex")

            // ✅ CORRECTION CRITIQUE : Utiliser getTotalForToday comme ObjectifsFragment
            val todayConsumption = withContext(Dispatchers.IO) {
                db.waterDao().getTotalForToday(email, todayIndex) ?: 0f
            }

            // Réinitialiser toutes les consommations
            weekConsumption.fill(0f)

            // Charger toutes les consommations de la semaine pour le graphique
            val allEntries = withContext(Dispatchers.IO) {
                db.waterDao().getAllByUser(email)
            }

            allEntries.forEach { entry ->
                weekConsumption[entry.dayIndex] = entry.amount
                android.util.Log.d("DashboardFragment", "Day ${entry.dayIndex}: ${entry.amount}L")
            }

            // S'assurer que la consommation d'aujourd'hui est correcte
            weekConsumption[todayIndex] = todayConsumption

            android.util.Log.d("DashboardFragment", "Today Consumption: ${weekConsumption[todayIndex]}L / ${dailyGoal}L")
            android.util.Log.d("DashboardFragment", "Percentage: ${(weekConsumption[todayIndex] / dailyGoal * 100).toInt()}%")

            // ✅ ÉTAPE 3 : Mettre à jour l'interface
            updateUI()
        }
    }

    private fun updateUI() {
        updateTexts()
        updateProgressBar()
        setupPieChart()
        setupBarChart()
    }

    private fun updateTexts() {
        val todayIndex = getTodayIndex()
        val dailyConsumption = weekConsumption[todayIndex]

        val (motivationEmoji, motivationText) = when {
            dailyConsumption >= dailyGoal -> "🎉" to "Objectif atteint! Bravo!"
            dailyConsumption >= dailyGoal * 0.75f -> "💪" to "Presque là! Continue!"
            dailyConsumption >= dailyGoal * 0.5f -> "👍" to "Bon progrès!"
            dailyConsumption >= dailyGoal * 0.25f -> "🚀" to "Continue comme ça!"
            dailyConsumption > 0f -> "💧" to "C'est un début!"
            else -> "🌟" to "Commence ta journée!"
        }

        tvConsumption.text = "%.2f L / %.1f L".format(dailyConsumption, dailyGoal)
        tvMotivation.text = "$motivationEmoji $motivationText"

        val validDays = weekConsumption.filter { it > 0f }
        val average = if (validDays.isNotEmpty()) validDays.average().toFloat() else 0f
        tvAverage.text = "%.2f L".format(average)

        val maxValue = weekConsumption.maxOrNull() ?: 0f
        val maxIndex = if (maxValue > 0f) weekConsumption.indexOf(maxValue) else todayIndex
        tvMaxDay.text = DAYS[maxIndex]

        val goalPercent = ((dailyConsumption / dailyGoal) * 100).coerceAtMost(100f)
        tvGoal.text = "%.0f%% de l'objectif".format(goalPercent)
    }

    private fun updateProgressBar() {
        val todayIndex = getTodayIndex()
        val dailyConsumption = weekConsumption[todayIndex]
        val goalPercent = ((dailyConsumption / dailyGoal) * 100).coerceIn(0f, 100f)

        val params = progressBar.layoutParams
        val targetWidth = (progressBar.parent as View).width * (goalPercent / 100f)

        ValueAnimator.ofInt(params.width, targetWidth.toInt()).apply {
            duration = 800
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                params.width = animation.animatedValue as Int
                progressBar.layoutParams = params
            }
            start()
        }

        val color = when {
            goalPercent >= 100f -> COLOR_SUCCESS
            goalPercent >= 75f -> COLOR_PRIMARY
            goalPercent >= 50f -> COLOR_WARNING
            else -> COLOR_DANGER
        }
        progressBar.setBackgroundColor(Color.parseColor(color))
    }

    private fun setupPieChart() {
        val dailyConsumption = weekConsumption[getTodayIndex()]
        val consumed = dailyConsumption.coerceAtMost(dailyGoal)
        val remaining = (dailyGoal - consumed).coerceAtLeast(0f)

        val entries = mutableListOf<PieEntry>()
        if (consumed > 0) entries.add(PieEntry(consumed, "Consommé"))
        if (remaining > 0) entries.add(PieEntry(remaining, "Restant"))

        val colors = when {
            consumed >= dailyGoal -> listOf(Color.parseColor(COLOR_SUCCESS), Color.parseColor(COLOR_PROGRESS_BG))
            consumed >= dailyGoal * 0.75f -> listOf(Color.parseColor(COLOR_PRIMARY), Color.parseColor(COLOR_PROGRESS_BG))
            consumed >= dailyGoal * 0.5f -> listOf(Color.parseColor(COLOR_WARNING), Color.parseColor(COLOR_PROGRESS_BG))
            else -> listOf(Color.parseColor(COLOR_DANGER), Color.parseColor(COLOR_PROGRESS_BG))
        }

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            sliceSpace = 4f
            selectionShift = 8f
            valueTextSize = 15f
            valueTextColor = Color.WHITE
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float) = "%.1fL".format(value)
            }
        }

        pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 70f
            transparentCircleRadius = 75f
            setDrawCenterText(true)

            val percentage = ((consumed / dailyGoal) * 100).coerceAtMost(100f)
            val emoji = when {
                percentage >= 100f -> "🏆"
                percentage >= 75f -> "⭐"
                percentage >= 50f -> "💧"
                else -> "🎯"
            }
            centerText = "$emoji\n%.0f%%".format(percentage)
            setCenterTextSize(26f)
            setCenterTextColor(Color.parseColor(COLOR_TEXT_PRIMARY))
            setCenterTextTypeface(android.graphics.Typeface.DEFAULT_BOLD)

            setDrawEntryLabels(false)
            isRotationEnabled = false
            setTouchEnabled(true)
            animateY(1200, Easing.EaseInOutCubic)
            invalidate()
        }
    }

    private fun setupBarChart() {
        val todayIndex = getTodayIndex()
        val entries = weekConsumption.mapIndexed { index, value ->
            BarEntry(index.toFloat(), value)
        }

        val colors = entries.mapIndexed { index, _ ->
            when {
                index == todayIndex && weekConsumption[index] >= dailyGoal -> Color.parseColor(COLOR_SUCCESS)
                index == todayIndex && weekConsumption[index] >= dailyGoal * 0.75f -> Color.parseColor(COLOR_PRIMARY)
                index == todayIndex && weekConsumption[index] >= dailyGoal * 0.5f -> Color.parseColor(COLOR_WARNING)
                index == todayIndex -> Color.parseColor(COLOR_DANGER)
                weekConsumption[index] >= dailyGoal -> Color.parseColor(COLOR_SUCCESS)
                weekConsumption[index] >= dailyGoal * 0.75f -> Color.parseColor(COLOR_PRIMARY)
                weekConsumption[index] >= dailyGoal * 0.5f -> Color.parseColor(COLOR_WARNING)
                weekConsumption[index] > 0f -> Color.parseColor(COLOR_TEXT_TERTIARY)
                else -> Color.parseColor(COLOR_PROGRESS_BG)
            }
        }

        val dataSet = BarDataSet(entries, "").apply {
            this.colors = colors
            valueTextSize = 12f
            valueTextColor = Color.parseColor(COLOR_TEXT_PRIMARY)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float) = if (value > 0) "%.1f".format(value) else ""
            }
        }

        barChart.apply {
            data = BarData(dataSet).apply { barWidth = 0.8f }
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(DAYS)
                granularity = 1f
                setDrawGridLines(false)
                setDrawAxisLine(false)
                textSize = 12f
                textColor = Color.parseColor(COLOR_TEXT_SECONDARY)
                yOffset = 8f
            }

            axisLeft.apply {
                axisMinimum = 0f
                granularity = 0.5f
                setDrawGridLines(true)
                gridColor = Color.parseColor(COLOR_PROGRESS_BG)
                gridLineWidth = 1.5f
                textColor = Color.parseColor(COLOR_TEXT_TERTIARY)
                textSize = 11f
                setDrawAxisLine(false)

                removeAllLimitLines()
                addLimitLine(com.github.mikephil.charting.components.LimitLine(dailyGoal, "").apply {
                    lineWidth = 2f
                    lineColor = Color.parseColor(COLOR_SUCCESS)
                    enableDashedLine(10f, 5f, 0f)
                    labelPosition = com.github.mikephil.charting.components.LimitLine.LimitLabelPosition.RIGHT_TOP
                    textSize = 0f
                })
                setDrawLimitLinesBehindData(true)
            }

            axisRight.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = false
            setScaleEnabled(false)
            setPinchZoom(false)
            extraTopOffset = 15f
            extraBottomOffset = 10f
            animateY(1400, Easing.EaseInOutCubic)
            invalidate()
        }
    }

    private fun animateCardsEntrance() {
        val cards = listOf(cardMain, cardStats, cardBarChart)
        cards.forEachIndexed { index, card ->
            card.alpha = 0f
            card.translationY = 50f
            card.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay((index * 100).toLong())
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
        updateDate()
    }
}