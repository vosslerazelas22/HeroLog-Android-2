# ⚔️ HeroLog Android

> Transforme sua produtividade em uma jornada de RPG — agora em app nativo.

HeroLog Android é um app de produtividade gamificado com mecânicas de RPG, built com Kotlin e Jetpack Compose.

---

## ✨ Funcionalidades

- **Timer Pomodoro** com pausas curtas e longas configuráveis
- **Dungeon Mode** — 4 sessões consecutivas com recompensas elevadas
- **Mecânicas de RPG** — XP, HP, Gold, Loot e Equipamentos
- **Sistema de Skills e Subskills** — categorize suas sessões por área de foco
- **Anotações de sessão** — registre notas e histórico de foco
- **Habits, Dailies e Todos** — quests diárias e tarefas com mecânicas de RPG
- **Inventário e Loja** — equipamentos, títulos e itens
- **Backup/Restore** — exportação e importação de saves (arquivo e clipboard)
- **Efeitos sonoros** — SFX integrados

---

## 🛠️ Stack

| Camada | Tecnologia |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Arquitetura | MVVM (single ViewModel) |
| Persistência | Room (blob JSON) |
| Serialização | kotlinx.serialization |
| Ícones | Lucide Icons (`com.composables:icons-lucide-android`) |
| Testes | JUnit + Robolectric + Roborazzi (screenshot tests) |
| Build | Gradle Kotlin DSL (KTS) |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 |

---

## 🚀 Como rodar localmente

```bash
# Clone o repositório
git clone https://github.com/vosslerazelas22/HeroLog-Android-2.git
cd HeroLog-Android-2

# Build debug
./gradlew assembleDebug

# Rodar testes unitários
./gradlew testDebugUnitTest
```

> **Pré-requisitos**: JDK 17+ para compilação, JDK 21 para testes Robolectric, Android SDK 36.

### Build release

```bash
./gradlew assembleRelease
```

A signing config release espera as variáveis de ambiente `KEYSTORE_PATH`, `STORE_PASSWORD` e `KEY_PASSWORD`. Para debug, um keystore padrão (`debug.keystore`) já está incluído.

---

## 📁 Estrutura do projeto

```
app/src/main/java/com/iurispraecepta/herolog/
├── MainActivity.kt              # Entry point, single Activity
├── HeroLogApplication.kt        # Container manual de dependências
├── model/                       # CharacterModels.kt — todos os tipos @Serializable
├── data/
│   ├── entity/                  # Room entities
│   ├── dao/                     # Data Access Objects
│   ├── database/                # HeroLogDatabase (Room)
│   ├── repository/              # CharacterRepository, FocusSessionRepository
│   └── TitleCatalog.kt          # Catálogo de títulos
├── logic/
│   ├── SkillLogic.kt            # Lógica de skills
│   ├── InventoryLogic.kt        # Lógica de inventário
│   ├── TitleLogic.kt            # Lógica de títulos
│   ├── CombatLogic.kt           # Lógica de combate
│   ├── CharacterMappers.kt      # Mappers entre entidades e modelos
│   ├── SaveMigrationLogic.kt    # Migração de saves antigos
│   ├── quests/                  # HabitLogic, DailyLogic, TodoLogic, RolloverLogic
│   ├── focus/                   # FocusRewardsLogic, FocusApplyLogic, CognitiveDeathLogic
│   ├── achievements/            # AchievementCatalog, AchievementLogic
│   ├── character/               # LevelUpLogic
│   └── kingdom/                 # ShopLogic, HeatmapLogic
└── ui/
    ├── HeroLogViewModel.kt      # Único ViewModel
    ├── theme/                   # Color.kt, Type.kt, Theme.kt
    ├── navigation/              # HeroLogBottomNav.kt
    ├── components/              # HeroLogModal, ItemInspectModal, AppHeader
    ├── focus/                   # FocusModeScreen, FocusOrb, QuickActionsBar
    ├── character/               # CharacterScreen, LevelUpOverlay
    ├── skills/                  # SkillsScreen, SkillSelectorModal
    ├── habits/                  # HabitsScreen
    ├── dailies/                 # DailiesScreen
    ├── todos/                   # TodosScreen
    ├── inventory/               # InventoryScreen
    ├── quests/                  # QuestsScreen
    ├── history/                 # HistoryScreen
    └── kingdom/                 # ShopScreen, TitleShopScreen, HeatmapScreen
```

---

## 🧪 Testes

```bash
# Suíte completa
./gradlew testDebugUnitTest

# Por classe específica
./gradlew testDebugUnitTest --tests "com.iurispraecepta.herolog.logic.SkillLogicTest"
```

Os resultados nominais ficam em `app/build/test-results/testDebugUnitTest/`. **Sempre verificar o XML bruto** — não confiar apenas na saída "BUILD SUCCESSFUL".

### Tipos de teste

- **Unitários** (lógica pura): funções em `logic/`
- **Integração** (ViewModel + Room em memória via Robolectric)
- **Screenshot** (Roborazzi): baselines em `app/src/test/screenshots/`

---

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](./LICENSE) para mais detalhes.
