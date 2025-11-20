package com.example.projetn2

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.snackbar.Snackbar
import android.view.View

class LoginActivity : AppCompatActivity() {
    private val prefs: SharedPreferences by lazy {
        getSharedPreferences("USER_AUTH", Context.MODE_PRIVATE)
    }

    // ✅ Variables pour le triple tap amélioré
    private var tapCount = 0
    private var lastTapTime = 0L
    private val TAP_TIMEOUT = 500L // 500ms entre chaque tap

    private lateinit var etLoginEmail: EditText
    private lateinit var etLoginPassword: EditText
    private lateinit var btnLogin: CardView
    private lateinit var tvGoToRegister: TextView
    private lateinit var tvForgotPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        val hasAnyUser = prefs.getAll().any { it.key.startsWith("user_") }

        if (!hasAnyUser) {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
            return
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initViews()
        setupListeners()
        setupTripleTap()
    }

    private fun initViews() {
        etLoginEmail = findViewById(R.id.etLoginEmail)
        etLoginPassword = findViewById(R.id.etLoginPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvGoToRegister = findViewById(R.id.tvGoToRegister)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener { handleLogin() }
        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        tvForgotPassword.setOnClickListener {
            showSnackbar("💡 Contactez le support pour réinitialiser", "#0288D1")
        }
    }

    // ✅ Configuration du triple tap sur toute l'activité
    private fun setupTripleTap() {
        val rootView = findViewById<View>(android.R.id.content)

        rootView.setOnClickListener {
            val currentTime = System.currentTimeMillis()

            // Si le délai entre deux taps est trop long, on réinitialise
            if (currentTime - lastTapTime > TAP_TIMEOUT) {
                tapCount = 1
            } else {
                tapCount++
            }

            lastTapTime = currentTime

            // Si on a 3 taps rapides
            if (tapCount >= 3) {
                autoFillLogin()
                tapCount = 0 // Réinitialiser après l'auto-fill
            }
        }
    }

    // ✅ Auto-remplissage avec le dernier utilisateur connecté
    private fun autoFillLogin() {
        // Option 1 : Utiliser le dernier utilisateur connecté
        val lastEmail = prefs.getString("current_user_email", null)

        if (lastEmail != null) {
            val password = prefs.getString("user_${lastEmail}_password", null)
            if (password != null) {
                etLoginEmail.setText(lastEmail)
                etLoginPassword.setText(password)
                showSnackbar("🎯 Auto-remplissage activé!", "#4CAF50")
                return
            }
        }

        // Option 2 : Utiliser le premier utilisateur trouvé
        val allUsers = prefs.all.filter { it.key.startsWith("user_") && it.key.endsWith("_password") }
        if (allUsers.isNotEmpty()) {
            val firstUserKey = allUsers.keys.first()
            val email = firstUserKey.removePrefix("user_").removeSuffix("_password")
            val password = allUsers[firstUserKey] as? String

            if (password != null) {
                etLoginEmail.setText(email)
                etLoginPassword.setText(password)
                showSnackbar("🎯 Auto-remplissage activé!", "#4CAF50")
                return
            }
        }

        // Option 3 : Compte de test par défaut
        etLoginEmail.setText("test@example.com")
        etLoginPassword.setText("123456")
        showSnackbar("🎯 Mode test activé!", "#4CAF50")
    }

    private fun handleLogin() {
        val email = etLoginEmail.text.toString().trim()
        val password = etLoginPassword.text.toString().trim()

        if (!validateLogin(email, password)) return

        val savedPassword = prefs.getString("user_${email}_password", null)
        val savedUsername = prefs.getString("user_${email}_username", null)

        if (savedPassword == null) {
            showSnackbar("❌ Email non trouvé. Créez un compte d'abord.", "#FF5722")
            etLoginEmail.text.clear()
            etLoginPassword.text.clear()
            return
        }

        if (savedPassword != password) {
            showSnackbar("❌ Mot de passe incorrect", "#FF5722")
            etLoginEmail.text.clear()
            etLoginPassword.text.clear()
            return
        }

        prefs.edit().apply {
            putBoolean("is_logged_in", true)
            putString("current_user_email", email)
            putString("current_user_name", savedUsername)
            apply()
        }

        showSnackbar("✓ Connexion réussie! Bienvenue $savedUsername", "#4CAF50")

        btnLogin.postDelayed({ goToMainActivity() }, 1000)
    }

    private fun validateLogin(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            showSnackbar("❌ Veuillez entrer votre email", "#FF5722")
            etLoginEmail.requestFocus()
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showSnackbar("❌ Format d'email invalide", "#FF5722")
            etLoginEmail.requestFocus()
            return false
        }
        if (password.isEmpty()) {
            showSnackbar("❌ Veuillez entrer votre mot de passe", "#FF5722")
            etLoginPassword.requestFocus()
            return false
        }
        if (password.length < 6) {
            showSnackbar("❌ Mot de passe doit contenir au moins 6 caractères", "#FF5722")
            etLoginPassword.requestFocus()
            return false
        }
        return true
    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showSnackbar(message: String, colorHex: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(Color.parseColor(colorHex))
            .setTextColor(Color.WHITE)
            .show()
    }
}