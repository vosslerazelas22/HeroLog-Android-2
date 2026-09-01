---
name: herolog-audit
description: Audita paridade funcional/visual entre o HeroLog React (fonte de verdade) e o HeroLog Android, ou audita a consistência de qualquer documento do projeto (AGENTS.md, PARIDADE.md, DEV_LOG_ANDROID.md) contra o código real. Use antes de declarar qualquer módulo como "validado" ou antes de aceitar um documento novo/editado como pronto para commit.
license: MIT
compatibility: opencode
---

# HeroLog Audit

Auditoria independente de paridade ou de consistência documental. **Não modifique nenhum
arquivo durante uma auditoria** — este skill só lê, compara e reporta.

## Quando usar

- Antes de marcar "Fidelidade Lógica: Validada" ou "Fidelidade Visual: Validada" no PARIDADE.md
- Antes de aceitar como pronto qualquer documento novo ou editado (AGENTS.md, PARIDADE.md, etc.)
- Sempre que o Bruno pedir explicitamente uma auditoria ou "cross-examination"
- Antes de fechar um Bloco que envolveu alteração de lógica de negócio

## Processo obrigatório

1. **Leia a especificação ou o alvo da auditoria** — o que deveria ser verdade.
2. **Leia o trecho real do código React** correspondente (fonte de verdade). Nunca supor
   comportamento por analogia com outro módulo já portado.
3. **Leia a implementação Android atual** correspondente.
4. **Consulte PARIDADE.md e DEV_LOG_ANDROID.md** para decisões conscientes de divergência
   já registradas — uma divergência documentada não é um bug.
5. **Não encerre a busca após encontrar a primeira implementação relevante.** Procure
   todas as referências e usos relacionados antes de concluir.
6. **Liste todos os arquivos examinados**, não só os que confirmaram a hipótese inicial.
7. **Classifique cada achado**: CRÍTICO / ALTO / MÉDIO / BAIXO.
8. **Informe explicitamente o que NÃO foi possível verificar** — não preencha lacunas
   com suposição.

## Armadilhas conhecidas a checar sempre

- **Resumo em prosa não é prova de teste.** Exigir XML bruto ou saída nominal por método
  antes de aceitar qualquer alegação de "testes passaram".
- **"O arquivo/tela/lógica não existe" precisa de grep real**, nunca aceitar de imediato —
  já ocorreu de um entry point estar escondido em componente não óbvio.
- **Dois módulos parecidos podem ter lógica diferente.** Foco ≠ Habit ≠ Daily ≠ Todo:
  cada um tem sua própria fórmula. Verificar o trecho literal, não supor por semelhança.
- **Imports silenciosos podem ligar a tipos errados** (ex: um pacote paralelo/morto com
  cópia duplicada de tipos). Confirmar que os imports apontam para `model.*` real.
- **A suíte completa de testes pode travar em ambiente com pouca memória** — isso não
  invalida a auditoria, mas deve ser registrado como validação parcial, nunca omitido.

## Formato do relatório

Para cada achado:

Problema N — <resumo curto>
Trecho/local: <arquivo:linha ou seção>
Por que é problemático: <explicação concreta, não genérica>
Severidade: CRÍTICO / ALTO / MÉDIO / BAIXO
Correção proposta: <ação específica>

Ao final, um resumo com contagem por severidade. **Não corrija nada nesta etapa** —
apenas reporte. A decisão sobre quais achados corrigir é do Bruno.