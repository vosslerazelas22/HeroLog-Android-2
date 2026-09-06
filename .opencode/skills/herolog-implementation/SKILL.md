---
name: herolog-implementation
description: Implementa uma mudança de código no HeroLog Android seguindo workflow obrigatório de requisitos, inspeção da fonte React e do PARIDADE.md, plano, implementação e diff — com build/teste executados localmente via ./gradlew ou delegados ao Android Studio. Use ao portar uma feature do React, corrigir um bug, ou fazer qualquer alteração de código Kotlin/Compose.
license: MIT
compatibility: opencode
---

# HeroLog Implementation

## Princípio central

**Este ambiente (WSL/OpenCode) tem toolchain Android completa.** JDK 17 (compilação) e
JDK 21 (testes Robolectric), Gradle 9.3.1 com wrapper, Android SDK 36 em `~/Android/Sdk`,
e Roborazzi 1.59.0 estão instalados e versionados no repo (`gradlew`, `gradle-wrapper.jar`,
`local.properties`, configurações em `gradle.properties` e `app/build.gradle.kts`).

Build e testes **podem ser executados diretamente aqui** via `./gradlew assembleDebug` e
`./gradlew testDebugUnitTest`. Porém, a validação visual (inspeção de UI, screenshots em
device/emulador) continua sendo responsabilidade do Android Studio — esta sessão não tem
acesso a emulador/dispositivo real.

**Regra**: não declarar "visual validado" sem inspeção humana real. Build e testes unitários
podem ser validados aqui via XML bruto.

## Workflow obrigatório

1. **REQUIREMENTS** — confirmar o requisito exato pedido, sem adicionar escopo.

2. **INSPECT**:
   - Ler PARIDADE.md e DEV_LOG_ANDROID.md para o módulo em questão (estado atual,
     decisões conscientes já registradas, riscos conhecidos).
   - Ler o trecho real do código React correspondente via `@react-source` — nunca
     supor comportamento por analogia com outro módulo já portado.
   - Ler a implementação Android atual do módulo.
   - Ao citar o comportamento React como referência no relatório final, citar o
     arquivo e a linha exata consultados — nunca descrever o comportamento React
     em prosa sem essa citação.

3. **PLAN** — escopo mínimo e auditável (um "Bloco"). Se o escopo crescer durante a
   inspeção, dividir em blocos menores em vez de expandir silenciosamente o bloco atual.

4. **IMPLEMENT** — implementar apenas o necessário. Não alterar arquitetura existente
   sem justificar explicitamente a razão.

5. **BUILD/TEST** — executar `./gradlew assembleDebug` e `./gradlew testDebugUnitTest`
   para validar build e lógica. Verificar XML bruto em
   `app/build/test-results/testDebugUnitTest/` para resultado nominal. A validação visual
   (UI em device/emulador) continua pendente até inspeção no Android Studio.

6. **DIFF** — `git diff` completo das mudanças, arquivo por arquivo.

7. **REPORT** — resumo objetivo no formato abaixo, com Validação marcada como
   pendente até o resultado real voltar.

## Regras obrigatórias (herdadas do AGENTS.md)

- Nunca aceitar resumo em prosa como prova de teste — exigir XML bruto ou saída nominal
  por método. Rodar `./gradlew testDebugUnitTest` e verificar o XML real.
- Nunca aceitar "o arquivo/tela/função não existe" sem grep real.
- Catálogos e dados literais extensos: sempre transcritos da fonte real, nunca
  inventados; verificar campo a campo contra a fonte após a escrita.
- Nunca presumir que dois módulos usam a mesma lógica por analogia — cada um exige
  verificação literal da fonte React (Foco ≠ Habit ≠ Daily ≠ Todo).
- Confirmar que os imports apontam para `model.*` real, nunca um pacote
  paralelo/morto por resolução silenciosa.
- Nunca alterar o schema do Room sem criar Migration explícita.
- Resultados não determinísticos (ex: loot via `Random.Default`) devem ser
  persistidos já calculados, nunca recalculados numa recuperação futura.
- Atenção a `@SerialName` em enums — nunca remover ou alterar sem verificar o
  impacto na compatibilidade com dados já persistidos.
- Ao commitar, use `git add <arquivo específico>` para cada arquivo alterado —
  nunca `git add -A` ou `git add .` — para evitar incluir mudanças não relacionadas
  ao escopo do bloco.

## Formato do relatório final

Escopo: <o que foi implementado, uma frase>

Arquivos criados/alterados: <lista com caminho completo>
(gerar esta lista a partir de `git diff --stat` real antes de reportar,
nunca de memória — o relatório deve refletir o diff real, mesmo que ele
inclua arquivos fora da intenção original do bloco)

Resumo: <o que mudou e por quê, incluindo qualquer decisão de divergência do React>

Validação: <resultado de build/teste — BUILD SUCCESSFUL + XML bruto nominal, ou PENDENTE>
Visual: PENDENTE — aguardando inspeção em device/emulador

Desvios de escopo: <se houver, com justificativa>

## Quando um bloco pode ser considerado fechado

- **Código + build + testes**: fechados quando build e testes unitários passam com
  resultado nominal confirmado (XML bruto).
- **Visual**: permanece pendente até inspeção humana em device/emulador real (Android Studio).
- A entrada no DEV_LOG_ANDROID.md deve refletir o estado real: "FECHADO (código + build +
  testes); visual pendente" quando aplicável.