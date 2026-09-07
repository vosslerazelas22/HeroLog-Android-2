package com.iurispraecepta.herolog.data.export

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

/**
 * Wrapper fino sobre `ClipboardManager` pra troca de saves via clipboard.
 *
 * Mantido como object (não no ViewModel) porque é pura manipulação de
 * serviço do sistema — não tem estado próprio, não tem side-effect no
 * `CharacterState`. O ViewModel só chama e segue a vida.
 */
object SaveClipboard {

    private const val LABEL = "HeroLog Save"

    /**
     * Copia [text] pro clipboard do sistema com label "HeroLog Save".
     * Em API 33+ (Android 13+), o sistema mostra um chip de confirmação
     * automaticamente — não precisamos de Toast.
     */
    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(LABEL, text))
    }

    /**
     * Lê o conteúdo atual do clipboard se ele tiver a label esperada.
     * Retorna `null` se vazio, se a label for outra, ou se a API < 33
     * (nesta versão o getter do clipboard exige `ACCESS_COOLDOWN`, então
     * evitamos tentar).
     */
    fun readFromClipboard(context: Context): String? {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            return null
        }
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val description = clipboard.primaryClipDescription?.label?.toString() ?: return null
        if (description != LABEL) return null
        val clip = clipboard.primaryClip ?: return null
        if (clip.itemCount == 0) return null
        return clip.getItemAt(0).coerceToText(context).toString()
    }
}
