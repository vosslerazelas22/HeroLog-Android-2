---
name: herolog-implementation
description: Implementa uma mudança de código no HeroLog Android seguindo workflow obrigatório de requisitos, inspeção da fonte React e do PARIDADE.md, plano, implementação e diff — com build/teste explicitamente delegados ao Android Studio, nunca executados ou presumidos nesta sessão. Use ao portar uma feature do React, corrigir um bug, ou fazer qualquer alteração de código Kotlin/Compose.
license: MIT
compatibility: opencode
---

# HeroLog Implementation

## Princípio central

**Este ambiente (WSL/OpenCode) não tem toolchain Android configurado.** Gradle, JDK e
Android SDK não estão disponíveis aqui. Build e testes são responsabilidade exclusiva
do Android Studio, do lado do Windows, depois de um `git pull`. Nunca declarar uma
mudança como "testada", "buildada" ou "funcionando" a partir desta sessão — qualquer
alegação de "BUILD SUCCESSFUL" ou "testes passaram" feita aqui, sem o round-trip pelo
Android Studio, é presumidamente falsa.

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

5. **BUILD/TEST (delegado, não executado)** — ao terminar a implementação, não tentar
   compilar ou testar. Preparar a instrução exata para o round-trip: commit local,
   confirmação do hash, e o que o Android Studio deve validar (build, testes
   específicos, comportamento visual).

6. **DIFF** — `git diff` completo das mudanças, arquivo por arquivo.

7. **REPORT** — resumo objetivo no formato abaixo, com Validação marcada como
   pendente até o resultado real voltar.

## Regras obrigatórias (herdadas do AGENTS.md)

- Nunca aceitar resumo em prosa como prova de teste — só existe prova depois do
  build real no Android Studio, com resultado nominal.
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

Validação: PENDENTE — aguardando build/teste no Android Studio (commit: <hash>)

Desvios de escopo: <se houver, com justificativa>

## Quando um bloco pode ser considerado fechado

Só depois que o resultado real do build/teste no Android Studio for reportado de volta
a esta sessão. Até lá, mesmo com a implementação completa, o bloco permanece aberto —
e a entrada correspondente no DEV_LOG_ANDROID.md não deve ser lançada com "Validação:
PENDENTE" como estado final.