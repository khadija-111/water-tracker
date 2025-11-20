package com.example.projetn2

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

class ConseilsFragment : Fragment() {

    // Data - Conseils avec emojis
    private val conseilsList = listOf(
        "🍽️" to "Buvez un verre d'eau avant chaque repas",
        "💧" to "Gardez une bouteille d'eau avec vous",
        "🏃" to "Buvez 2 verres d'eau après le sport",
        "🚫" to "Remplacez les boissons sucrées par de l'eau",
        "⏰" to "Réglez des rappels pour boire régulièrement",
        "🌡️" to "Buvez plus par temps chaud",
        "🥤" to "Utilisez une paille pour boire plus facilement",
        "🍋" to "Ajoutez du citron pour plus de goût"
    )

    // Articles avec titre + lien
    private val articlesList = listOf(
        "Healthline - Quantité d'eau recommandée" to "https://www.healthline.com/nutrition/how-much-water-should-you-drink-per-day",
        "Medical News Today - Bienfaits de l'eau" to "https://www.medicalnewstoday.com/articles/290814",
        "WHO - Hydratation et santé" to "https://www.who.int/news-room/fact-sheets/detail/drinking-water",
        "Mayo Clinic - Guide d'hydratation" to "https://www.mayoclinic.org/healthy-lifestyle/nutrition-and-healthy-eating/in-depth/water/art-20044256"
    )

    // Views
    private lateinit var conseilsContainer: LinearLayout
    private lateinit var articlesContainer: LinearLayout
    private lateinit var btnShare: Button
    private lateinit var tvConseilsCount: TextView
    private lateinit var tvArticlesCount: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_conseils, container, false)

        initViews(view)
        setupConseils()
        setupArticles()
        setupListeners()

        return view
    }

    private fun initViews(view: View) {
        conseilsContainer = view.findViewById(R.id.conseilsContainer)
        articlesContainer = view.findViewById(R.id.articlesContainer)
        btnShare = view.findViewById(R.id.btnShare)
        tvConseilsCount = view.findViewById(R.id.tvConseilsCount)
        tvArticlesCount = view.findViewById(R.id.tvArticlesCount)

        // Mettre à jour les compteurs
        tvConseilsCount.text = conseilsList.size.toString()
        tvArticlesCount.text = articlesList.size.toString()
    }

    private fun setupConseils() {
        conseilsList.forEachIndexed { index, (emoji, conseil) ->
            val conseilCard = createConseilCard(emoji, conseil, index)
            conseilsContainer.addView(conseilCard)
        }
    }

    private fun createConseilCard(emoji: String, conseil: String, index: Int): View {
        val cardView = CardView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                if (index > 0) topMargin = 12
            }
            radius = 12f
            cardElevation = 4f
            setCardBackgroundColor(Color.parseColor("#E0F7FA"))
        }

        val linearLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        // Emoji
        val emojiText = TextView(requireContext()).apply {
            text = emoji
            textSize = 28f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = 16
            }
        }

        // Conseil text
        val conseilText = TextView(requireContext()).apply {
            text = conseil
            textSize = 15f
            setTextColor(Color.parseColor("#006064"))
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        // Numéro
        val numberBadge = CardView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            radius = 12f
            cardElevation = 2f
            setCardBackgroundColor(Color.parseColor("#00BCD4"))
        }

        val numberText = TextView(requireContext()).apply {
            text = (index + 1).toString()
            textSize = 12f
            setTextColor(Color.WHITE)
            setPadding(12, 6, 12, 6)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        numberBadge.addView(numberText)
        linearLayout.addView(emojiText)
        linearLayout.addView(conseilText)
        linearLayout.addView(numberBadge)
        cardView.addView(linearLayout)

        return cardView
    }

    private fun setupArticles() {
        articlesList.forEachIndexed { index, (title, link) ->
            val articleCard = createArticleCard(title, link, index)
            articlesContainer.addView(articleCard)
        }
    }

    private fun createArticleCard(title: String, link: String, index: Int): View {
        val cardView = CardView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                if (index > 0) topMargin = 12
            }
            radius = 12f
            cardElevation = 4f
            setCardBackgroundColor(Color.parseColor("#FFF3E0"))
            isClickable = true
            isFocusable = true
            foreground = ContextCompat.getDrawable(requireContext(),
                android.R.drawable.list_selector_background)
        }

        val linearLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        // Icon
        val iconText = TextView(requireContext()).apply {
            text = "🔗"
            textSize = 24f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = 16
            }
        }

        // Content
        val contentLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val titleText = TextView(requireContext()).apply {
            text = title
            textSize = 15f
            setTextColor(Color.parseColor("#E65100"))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val linkText = TextView(requireContext()).apply {
            text = "Cliquez pour lire →"
            textSize = 12f
            setTextColor(Color.parseColor("#FF6F00"))
            setPadding(0, 4, 0, 0)
        }

        contentLayout.addView(titleText)
        contentLayout.addView(linkText)

        linearLayout.addView(iconText)
        linearLayout.addView(contentLayout)
        cardView.addView(linearLayout)

        // Click listener
        cardView.setOnClickListener {
            openLink(link, title)
        }

        return cardView
    }

    private fun setupListeners() {
        btnShare.setOnClickListener {
            shareConseils()
        }
    }

    private fun openLink(url: String, title: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)

            view?.let {
                Snackbar.make(it, "🌐 Ouverture: $title", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.parseColor("#00BCD4"))
                    .setTextColor(Color.WHITE)
                    .show()
            }
        } catch (e: Exception) {
            view?.let {
                Snackbar.make(it, "❌ Erreur d'ouverture du lien", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.parseColor("#FF5722"))
                    .setTextColor(Color.WHITE)
                    .show()
            }
        }
    }

    private fun shareConseils() {
        // Créer le texte à partager
        val shareText = buildString {
            append("💧 Conseils d'hydratation 💧\n\n")

            conseilsList.forEachIndexed { index, (emoji, conseil) ->
                append("${index + 1}. $emoji $conseil\n")
            }

            append("\n📚 Ressources:\n")
            articlesList.forEach { (title, link) ->
                append("• $title\n  $link\n")
            }

            append("\n✨ Partagé depuis Water Tracker App")
        }

        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "💧 Conseils d'hydratation")
                putExtra(Intent.EXTRA_TEXT, shareText)
            }

            startActivity(Intent.createChooser(shareIntent, "Partager via"))

            view?.let {
                Snackbar.make(it, "✓ Partage en cours...", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.parseColor("#4CAF50"))
                    .setTextColor(Color.WHITE)
                    .show()
            }
        } catch (e: Exception) {
            view?.let {
                Snackbar.make(it, "❌ Erreur de partage", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.parseColor("#FF5722"))
                    .setTextColor(Color.WHITE)
                    .show()
            }
        }
    }
}