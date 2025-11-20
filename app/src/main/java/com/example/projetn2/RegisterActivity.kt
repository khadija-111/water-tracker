package com.example.projetn2

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private val prefs: SharedPreferences by lazy {
        getSharedPreferences("USER_AUTH", Context.MODE_PRIVATE)
    }

    private lateinit var etRegisterUsername: EditText
    private lateinit var etRegisterEmail: EditText
    private lateinit var etRegisterPassword: EditText
    private lateinit var etRegisterConfirmPassword: EditText
    private lateinit var cardBirthday: CardView
    private lateinit var tvBirthday: TextView
    private lateinit var tvPasswordStrength: TextView
    private lateinit var tvAgeValidation: TextView
    private lateinit var btnRegister: CardView
    private lateinit var tvGoToLogin: TextView

    private var selectedBirthday: Calendar? = null
    private var isOver18 = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initViews()
        setupListeners()
        setupPasswordStrengthChecker()
    }

    private fun initViews() {
        etRegisterUsername = findViewById(R.id.etRegisterUsername)
        etRegisterEmail = findViewById(R.id.etRegisterEmail)
        etRegisterPassword = findViewById(R.id.etRegisterPassword)
        etRegisterConfirmPassword = findViewById(R.id.etRegisterConfirmPassword)
        cardBirthday = findViewById(R.id.cardBirthday)
        tvBirthday = findViewById(R.id.tvBirthday)
        tvPasswordStrength = findViewById(R.id.tvPasswordStrength)
        tvAgeValidation = findViewById(R.id.tvAgeValidation)
        btnRegister = findViewById(R.id.btnRegister)
        tvGoToLogin = findViewById(R.id.tvGoToLogin)
    }

    private fun setupListeners() {
        cardBirthday.setOnClickListener { showDatePicker() }

        btnRegister.setOnClickListener { handleRegister() }

        tvGoToLogin.setOnClickListener { finish() }
    }

    private fun setupPasswordStrengthChecker() {
        etRegisterPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updatePasswordStrength(s.toString())
            }
        })
    }

    private fun updatePasswordStrength(password: String) {
        when {
            password.isEmpty() -> {
                tvPasswordStrength.text = "💡 Sécurité: Faible"
                tvPasswordStrength.setTextColor(Color.parseColor("#FF5722"))
            }
            password.length < 6 -> {
                tvPasswordStrength.text = "💡 Sécurité: Faible"
                tvPasswordStrength.setTextColor(Color.parseColor("#FF5722"))
            }
            password.length < 8 -> {
                tvPasswordStrength.text = "💪 Sécurité: Moyenne"
                tvPasswordStrength.setTextColor(Color.parseColor("#FF9800"))
            }
            password.length >= 8 && password.any { it.isDigit() } -> {
                tvPasswordStrength.text = "🔒 Sécurité: Forte"
                tvPasswordStrength.setTextColor(Color.parseColor("#4CAF50"))
            }
            else -> {
                tvPasswordStrength.text = "💪 Sécurité: Bonne"
                tvPasswordStrength.setTextColor(Color.parseColor("#8BC34A"))
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR) - 18
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // DatePickerDialog فـ Spinner mode
        val datePickerDialog = DatePickerDialog(
            this,
            android.R.style.Theme_Holo_Light_Dialog, // هذا باش يتحول لـ spinner
            { _, selectedYear, selectedMonth, selectedDay ->
                val birthday = Calendar.getInstance()
                birthday.set(selectedYear, selectedMonth, selectedDay)
                selectedBirthday = birthday
                tvBirthday.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(birthday.time)
                tvBirthday.setTextColor(Color.parseColor("#333333"))
                checkAge(birthday)
            },
            year,
            month,
            day
        )


        datePickerDialog.datePicker.minDate = Calendar.getInstance().apply { set(1900, 0, 1) }.timeInMillis

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

        datePickerDialog.show()
    }



    private fun checkAge(birthday: Calendar) {
        val today = Calendar.getInstance()
        var age = today.get(Calendar.YEAR) - birthday.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < birthday.get(Calendar.DAY_OF_YEAR)) age--
        isOver18 = age >= 18
        if (isOver18) {
            tvAgeValidation.text = "✓ Âge valide ($age ans)"
            tvAgeValidation.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            tvAgeValidation.text = "⚠️ Vous devez avoir 18 ans ou plus (actuellement $age ans)"
            tvAgeValidation.setTextColor(Color.parseColor("#FF5722"))
        }
        tvAgeValidation.visibility = View.VISIBLE
    }

    private fun handleRegister() {
        val username = etRegisterUsername.text.toString().trim()
        val email = etRegisterEmail.text.toString().trim()
        val password = etRegisterPassword.text.toString().trim()
        val confirmPassword = etRegisterConfirmPassword.text.toString().trim()

        if (!validateRegistration(username, email, password, confirmPassword)) return

        if (prefs.contains("user_${email}_password")) {
            showSnackbar("❌ Cet email est déjà utilisé", "#FF5722")
            return
        }

        prefs.edit().apply {
            putString("user_${email}_username", username)
            putString("user_${email}_password", password)
            putString("user_${email}_birthday", tvBirthday.text.toString())
            apply()
        }

        // Connexion automatique après inscription
        prefs.edit().apply {
            putBoolean("is_logged_in", true)
            putString("current_user_email", email)
            putString("current_user_name", username)
            apply()
        }

        showSnackbar("✓ Compte créé avec succès! Connectez-vous maintenant", "#4CAF50")

        btnRegister.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 1500)
    }

    private fun validateRegistration(username: String, email: String, password: String, confirmPassword: String): Boolean {
        if (username.isEmpty()) { showSnackbar("❌ Veuillez entrer un nom d'utilisateur", "#FF5722"); etRegisterUsername.requestFocus(); return false }
        if (username.length < 3) { showSnackbar("❌ Nom d'utilisateur doit contenir au moins 3 caractères", "#FF5722"); etRegisterUsername.requestFocus(); return false }
        if (email.isEmpty()) { showSnackbar("❌ Veuillez entrer votre email", "#FF5722"); etRegisterEmail.requestFocus(); return false }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { showSnackbar("❌ Format d'email invalide", "#FF5722"); etRegisterEmail.requestFocus(); return false }
        if (password.isEmpty()) { showSnackbar("❌ Veuillez entrer un mot de passe", "#FF5722"); etRegisterPassword.requestFocus(); return false }
        if (password.length < 6) { showSnackbar("❌ Mot de passe doit contenir au moins 6 caractères", "#FF5722"); etRegisterPassword.requestFocus(); return false }
        if (confirmPassword.isEmpty()) { showSnackbar("❌ Veuillez confirmer votre mot de passe", "#FF5722"); etRegisterConfirmPassword.requestFocus(); return false }
        if (password != confirmPassword) { showSnackbar("❌ Les mots de passe ne correspondent pas", "#FF5722"); etRegisterConfirmPassword.requestFocus(); return false }
        if (selectedBirthday == null) { showSnackbar("❌ Veuillez sélectionner votre date de naissance", "#FF5722"); return false }
        if (!isOver18) { showSnackbar("❌ Vous devez avoir 18 ans ou plus pour créer un compte", "#FF5722"); return false }
        return true
    }

    private fun showSnackbar(message: String, colorHex: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(Color.parseColor(colorHex))
            .setTextColor(Color.WHITE)
            .show()
    }
}
