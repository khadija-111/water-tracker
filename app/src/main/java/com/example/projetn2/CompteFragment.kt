package com.example.projetn2

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent

import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class CompteFragment : Fragment() {


    private val prefs: SharedPreferences by lazy {
        requireContext().getSharedPreferences("WATER_DATA", Context.MODE_PRIVATE)
    }

    // Data
    private var notificationsEnabled = true

    private var userName = "Khadija"
    private var dailyGoal = 2.0f
    private var streakDays = 5
    private var totalConsumed = 24.5f
    private var memberSince = "Nov 2024"

    // Views

    private lateinit var tvAvatar: TextView
    private lateinit var tvName: TextView
    private lateinit var tvGoal: TextView
    private lateinit var tvStreak: TextView
    private lateinit var tvTotalConsumed: TextView
    private lateinit var tvMemberSince: TextView
    private lateinit var btnEditProfile: CardView
    private lateinit var btnNotification: CardView
    private lateinit var tvNotificationStatus: TextView

    private lateinit var btnSettings: CardView
    private lateinit var btnAbout: CardView
    private lateinit var btnLogout: CardView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_compte, container, false)

        initViews(view)
        loadData()
        setupListeners()
        updateUI()

        return view
    }

    private fun initViews(view: View) {
        tvAvatar = view.findViewById(R.id.tvAvatar)
        tvName = view.findViewById(R.id.tvName)
        tvGoal = view.findViewById(R.id.tvGoal)
        tvStreak = view.findViewById(R.id.tvStreak)
        tvTotalConsumed = view.findViewById(R.id.tvTotalConsumed)
        tvMemberSince = view.findViewById(R.id.tvMemberSince)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        btnSettings = view.findViewById(R.id.btnSettings)
        btnAbout = view.findViewById(R.id.btnAbout)
        btnLogout = view.findViewById(R.id.btnLogout)
        btnNotification = view.findViewById(R.id.btnNotification)
        tvNotificationStatus = view.findViewById(R.id.tvNotificationStatus)
    }

    private fun setupListeners() {
        btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        btnSettings.setOnClickListener {
            showNotificationSettingsDialog()
        }

        btnAbout.setOnClickListener {
            showAboutDialog()
        }

        btnLogout.setOnClickListener {
            // تحديث حالة الإشعارات قبل تسجيل الخروج
            tvNotificationStatus.text = if (notificationsEnabled) "🔔 Activées" else "🔕 Désactivées"
            showLogoutDialog()
        }

        btnNotification.setOnClickListener {
            showNotificationSettingsDialog()
        }
    }

    private fun loadData() {
        // SharedPreferences ديال التسجيل
        val authPrefs = requireContext().getSharedPreferences("USER_AUTH", Context.MODE_PRIVATE)
        userName = authPrefs.getString("current_user_name", userName) ?: userName

        // باقي القيم من WATER_DATA
        notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
        dailyGoal = prefs.getFloat("daily_goal", 2.0f)
        streakDays = prefs.getInt("streak_days", 5)
        totalConsumed = prefs.getFloat("total_consumed", 24.5f)
        memberSince = prefs.getString("member_since", getCurrentMonthYear()) ?: getCurrentMonthYear()

        if (!prefs.contains("member_since")) {
            prefs.edit().putString("member_since", memberSince).apply()
        }
    }


    private fun getCurrentMonthYear(): String {
        val dateFormat = SimpleDateFormat("MMM yyyy", Locale.FRENCH)
        return dateFormat.format(Date())
    }

    private fun updateUI() {
        // Avatar (première lettre du nom)
        tvAvatar.text = userName.firstOrNull()?.uppercase() ?: "U"

        // Nom d'utilisateur
        tvName.text = userName

        // Objectif quotidien
        tvGoal.text = "%.1fL".format(dailyGoal)

        // Série de jours
        tvStreak.text = "$streakDays jours"

        // Total consommé
        tvTotalConsumed.text = "%.1fL".format(totalConsumed)

        // Membre depuis
        tvMemberSince.text = memberSince

        // Statut notifications
        tvNotificationStatus.text = if (notificationsEnabled) "🔔 Activées" else "🔕 Désactivées"
    }


    private fun showEditProfileDialog() {
        val dialogView = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 30)
        }

        val inputName = EditText(requireContext()).apply {
            hint = "Votre nom"
            setText(userName)
            setSingleLine()
        }

        val inputGoal = EditText(requireContext()).apply {
            hint = "Objectif quotidien (L)"
            setText(dailyGoal.toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        dialogView.addView(inputName)

        // Ajouter un espace
        dialogView.addView(View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                20
            )
        })

        dialogView.addView(inputGoal)

        AlertDialog.Builder(requireContext())
            .setTitle("✏️ Modifier le profil")
            .setView(dialogView)
            .setPositiveButton("Enregistrer") { _, _ ->
                val newName = inputName.text.toString().trim()
                val newGoal = inputGoal.text.toString().toFloatOrNull()

                if (newName.isNotEmpty()) {
                    userName = newName
                    prefs.edit().putString("user_name", userName).apply()
                }

                if (newGoal != null && newGoal > 0) {
                    dailyGoal = newGoal
                    prefs.edit().putFloat("daily_goal", dailyGoal).apply()
                }

                updateUI()
                showSnackbar("✓ Profil mis à jour", "#4CAF50")
            }
            .setNegativeButton("Annuler", null)
            .show()


    }
    private fun showNotificationSettingsDialog() {
        val options = arrayOf("Activer", "Désactiver")
        val currentSelection = if (notificationsEnabled) 0 else 1

        AlertDialog.Builder(requireContext())
            .setTitle("🔔 Notifications")
            .setSingleChoiceItems(options, currentSelection) { _, which ->
                notificationsEnabled = (which == 0)
            }
            .setPositiveButton("Enregistrer") { _, _ ->
                prefs.edit().putBoolean("notifications_enabled", notificationsEnabled).apply()
                tvNotificationStatus.text = if (notificationsEnabled) "🔔 Activées" else "🔕 Désactivées"
                showSnackbar("✓ Paramètres de notification mis à jour", "#4CAF50")
            }
            .setNegativeButton("Annuler", null)
            .show()
    }



    private fun showAboutDialog() {
        val message = """
            💧 WaterTracker v1.0.0
            
            Application de suivi de consommation d'eau quotidienne.
            
            Fonctionnalités:
            • Suivi quotidien et hebdomadaire
            • Objectifs personnalisables
            • Graphiques statistiques
            • Historique détaillé
            
            Développé avec ❤️
            © 2024
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("ℹ️ À propos")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("🚪 Déconnexion")
            .setMessage("Êtes-vous sûr de vouloir vous déconnecter ?")
            .setPositiveButton("Oui") { _, _ ->
                // Effacer l'état de connexion
                val authPrefs = requireContext().getSharedPreferences("USER_AUTH", Context.MODE_PRIVATE)
                authPrefs.edit().putBoolean("is_logged_in", false).apply()

                // Feedback
                showSnackbar("✓ Déconnecté avec succès", "#FF5722")

                // Retourner à l'écran de login
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("Non", null)
            .show()
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
        updateUI()
    }
}
