package com.iurispraecepta.herolog.ui.focus

import com.iurispraecepta.herolog.R

data class AmbientTrack(
    val id: String,
    val nome: String,
    val icone: String,
    @androidx.annotation.RawRes val rawRes: Int
)

val AMBIENT_SOUNDS: List<AmbientTrack> = listOf(
    AmbientTrack("floresta", "Floresta", "🌲", R.raw.floresta),
    AmbientTrack("chuva", "Chuva", "🌧", R.raw.chuva),
    AmbientTrack("taverna", "Taverna", "🍺", R.raw.taverna),
    AmbientTrack("biblioteca", "Biblioteca", "📚", R.raw.biblioteca),
    AmbientTrack("ruinas", "Ruínas", "🏛", R.raw.ruinas),
    AmbientTrack("montanha", "Montanha", "⛰", R.raw.montanha),
    AmbientTrack("cidade", "Cidade", "🏘", R.raw.cidade),
    AmbientTrack("templo", "Templo", "⛩", R.raw.templo)
)
