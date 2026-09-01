---
name: herolog-parity
description: Compara um módulo ou feature específica entre o HeroLog React (fonte de verdade) e o HeroLog Android para determinar fidelidade lógica e visual. Use ao portar uma feature nova, ao investigar uma regressão suspeita, ou ao decidir se uma divergência encontrada é bug ou decisão consciente já registrada.
license: MIT
compatibility: opencode
---

# HeroLog Parity

Comparação estruturada de paridade entre o React (fonte de verdade) e o Android. Este
skill não decide sozinho se algo deve ser corrigido — ele produz a comparação que
permite ao Bruno decidir.

## Princípio central

O código real do app React (branch main) é a fonte de verdade. O Android é uma cópia
fiel — qualquer divergência não documentada é um bug, não uma melhoria.

**Nunca assumir equivalência entre módulos parecidos.** Foco ≠ Habit ≠ Daily ≠ Todo —
cada um tem sua própria fórmula e seu próprio comportamento, mesmo quando a UI parece
similar. Cada comparação exige verificação literal da fonte, nunca por analogia com
outro módulo já portado.

## Processo obrigatório

1. **Obter o trecho real do código React** correspondente à feature — não supor
   comportamento pela documentação ou pela versão Android existente.
2. **Localizar a implementação Android atual** correspondente.
3. **Comparar modelo de dados**: tipos, campos, valores default, nullability.
4. **Comparar side effects**: o que muda de estado, e em que momento exatamente.
5. **Comparar persistência**: o que é salvo, quando, e como é serializado.
6. **Comparar UI**: fidelidade visual exige print real ou comparação via Roborazzi —
   nunca presumir fidelidade visual sem evidência.
7. **Comparar edge cases**: valores nulos, listas vazias, limites (primeiro/último
   item, zero, negativo).
8. **Verificar com testes**: build + suíte relevante, resultado nominal por método.
9. **Documentar toda divergência encontrada** — classificar como intencional (já
   registrada em PARIDADE.md, com motivo) ou como bug real.

## Armadilhas específicas de paridade a checar sempre

- **Três formatos de data coexistem** no CharacterState: `toDateString()` (inglês),
  `toLocaleDateString('pt-BR')`, `toLocaleString('pt-BR')` — cada um com uso
  específico. Não tratar como equivalentes ao portar módulos com datas.
- **Imports podem apontar silenciosamente para tipos mortos/duplicados** em vez do
  `model.*` real, por resolução de pacote incorreta. Confirmar sempre o import
  exato, não só o nome do tipo.
- **Catálogos e dados literais extensos** (títulos, quests, conquistas) devem ser
  transcritos do código-fonte real, nunca inventados — verificar campo a campo
  contra a fonte após qualquer escrita ou edição.
- **Resultados não determinísticos** (ex: loot via `Random.Default`) devem ser
  persistidos já calculados, nunca recalculados numa recuperação futura — evita
  reroll ao fechar/reabrir o app.
- **`@SerialName`** garante paridade byte a byte do JSON entre React e Android —
  nunca remover ou alterar sem verificar o impacto na compatibilidade de dados
  já persistidos.

## Formato do relatório

Módulo/feature avaliado: <nome>

Fidelidade lógica: Fiel / Divergente / Não verificável — com evidência (arquivo:linha,
trecho React comparado)

Fidelidade visual: Validada (print real) / Não validada / Não aplicável

Divergências encontradas: para cada uma —
descrição, se é intencional (já registrada em PARIDADE.md, citar onde) ou bug real,
e ação proposta.

Atualização sugerida para o PARIDADE.md: linha/seção e novo conteúdo, se aplicável.

Não corrigir nada nesta etapa — apenas reportar. A decisão de corrigir é do Bruno.