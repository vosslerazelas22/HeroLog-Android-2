# Addendum Spec 001: FocusOrb 4 Conceitos + Ajustes Gerais — Pendências para Paridade Completa

Este addendum documenta as pendências identificadas após a implementação inicial do commit `4e35f2f` para atingir a paridade 1:1 com o React (fonte de verdade).

---

## 1. FocusOrb.kt — Imports Faltando para Animação de Pulse (Conceito D)

**Arquivo:** `app/src/main/java/com/iurispraecepta/herolog/ui/focus/FocusOrb.kt`

**Problema:** O Conceito D (Alchemist Flask) usa `rememberInfiniteTransition` + `infiniteRepeatable` para animação de pulse no container (4s, reverse), mas os imports necessários não estão todos presentes.

**Imports Faltando:**
```kotlin
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
```

---

## 2. GeneralSettingsModal.kt — Ícone de Fechar (Lucide não Existe)

**Arquivo:** `app/src/main/java/com/iurispraecepta/herolog/ui/components/GeneralSettingsModal.kt`

**Problema:** Linha ~55 usa `com.composables.icons.lucide.R` para ícone de fechar (`R.drawable.lucide_ic_x`), mas este recurso não existe no projeto Android atual.

**Recomendação:** Opção A (Material Icon) — `Icon(Icons.Default.Close, ...)` — consistente com resto do app que já usa Material 3.

---

## 3. MainActivity.kt — GeneralSettingsModal Não Adicionado ao Box

**Arquivo:** `app/src/main/java/com/iurispraecepta/herolog/MainActivity.kt`

**Problema:** O estado `isGeneralSettingsOpen` foi criado mas o `GeneralSettingsModal` **não foi renderizado** no conteúdo do `Scaffold`.

**Local:** Após `SaveImportResultDialog` (aprox. linha 469), antes do `LevelUpOverlay`.

---

## 4. FocusModeScreen.kt — Parâmetro orbConcept Ausente

**Arquivo:** `app/src/main/java/com/iurispraecepta/herolog/ui/focus/FocusModeScreen.kt`

**Problema:** A função não aceita parâmetro `orbConcept`, mas o `MainActivity.kt` já passa na chamada.

**Correção:**
1. Adicionar import: `import com.iurispraecepta.herolog.model.OrbConcept`
2. Adicionar parâmetro: `orbConcept: OrbConcept = OrbConcept.D,` antes de `modifier`
3. Passar para `FocusOrb`: `orbConcept = orbConcept,`

---

## Resumo de Ações (4 arquivos)

| Arquivo | Ação |
|---------|------|
| `FocusOrb.kt` | +3 imports (`rememberInfiniteTransition`, `infiniteRepeatable`, `RepeatMode`) |
| `GeneralSettingsModal.kt` | Trocar Lucide X por `Icons.Default.Close` |
| `MainActivity.kt` | Inserir `GeneralSettingsModal` no Box |
| `FocusModeScreen.kt` | +parâmetro `orbConcept` + import + passar para `FocusOrb` |

---

## Checklist Pós-Correção

- [ ] Build `./gradlew assembleDebug` sucesso
- [ ] Testes `./gradlew testDebugUnitTest` passam (XML bruto conferido)
- [ ] Validação visual manual: 4 conceitos × 6 modos × 2 viewports
- [ ] `PARIDADE.md` atualizado
- [ ] `DEV_LOG_ANDROID.md` registrado