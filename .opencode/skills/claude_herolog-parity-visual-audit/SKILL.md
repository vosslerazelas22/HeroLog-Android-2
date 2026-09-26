---
name: claude_herolog-parity-visual-audit
description: Audita paridade visual entre uma tela do HeroLog React (fonte da verdade) e sua implementação Android (Kotlin/Compose), comparando valores literais de código dos dois lados, elemento a elemento, com gate obrigatório de citação dos dois trechos por linha. Use antes de declarar qualquer fidelidade visual como validada.
---

# Skill: Auditoria de Paridade Visual (React → Android Compose)

## Propósito
Auditar paridade visual entre uma tela do HeroLog-React-ref (fonte da verdade) e sua
implementação em HeroLog-Android-2 (Kotlin/Jetpack Compose). O objetivo NÃO é dizer
"parece igual" — é extrair valores literais dos dois lados, colocá-los lado a lado, e
declarar divergência ou match para cada elemento, um por um.

## Regra de ouro (gate obrigatório)
Nenhum veredito de "OK" ou "confere" é válido sem uma citação literal do trecho de
código dos DOIS lados (React e Kotlin) na mesma linha da tabela. Se você não tem os
dois trechos citados, a linha está incompleta — não conte como auditada.

Proibido:
- "Os estilos parecem consistentes"
- "O texto está igual"
- "Cores parecem próximas"

Obrigatório: extrair o valor exato via grep/leitura direta do arquivo, para cada
elemento, e citar o trecho.

## Protocolo de execução

1. Identifique a tela/componente alvo e localize o arquivo React correspondente
   (ex. `src/components/HeroLogModal.tsx`) e o arquivo Kotlin correspondente
   (ex. `HeroLogModal.kt`).
2. Faça um inventário de TODOS os elementos visuais da tela antes de comparar
   qualquer coisa: textos, ícones, botões, cards, imagens, transições. Não pule
   elementos "óbvios" — a maioria dos bugs reportados vieram de elementos
   considerados triviais (case de texto, ícone de nav bar).
3. Para cada elemento, preencha uma linha em UMA das tabelas abaixo (categoria
   correspondente), com os trechos literais dos dois lados.
4. Ao final, gere um resumo separado listando SOMENTE as divergências
   encontradas, com número de linha/arquivo dos dois lados, pronto para virar
   item de spec (Dx) ou requisito funcional (FR-xxx), seguindo o padrão já usado
   no projeto (specs numerados, diagnostic items).
5. Nenhuma auditoria é considerada completa sem grep de TODAS as branches/estados
   do componente (ex. estado vazio, erro, loading, variações condicionais) — não
   apenas o caminho feliz renderizado por padrão.

## Categorias e o que checar

### 1. Texto
| Elemento | Texto literal React (citado) | Texto literal Android (citado) | Case bate? | Match? |
|---|---|---|---|---|
Checar: texto exato, **case exato** (UPPERCASE vs Title Case vs sentence case —
causa raiz de bug já visto), truncamento/ellipsis (`line-clamp`, `maxLines`),
pluralização, texto condicional.

### 2. Tipografia
| Elemento | Font family/weight/size React | Font family/weight/size Android | Match? |
|---|---|---|---|
Checar: família da fonte, peso, tamanho, letter-spacing, line-height. Fontes
divergentes já foram um erro recorrente — não assumir que a fonte padrão do tema
Android é a mesma do CSS.

### 3. Cor
| Elemento | Hex/token React (citado) | Hex/token Android (citado) | Match? |
|---|---|---|---|
Checar: cor de texto, cor de fundo, cor de borda, opacidade/alpha exata. Valor
hex exato — não "parece cinza".

### 4. Ícones
| Elemento | Nome exato do ícone + lib React (citado) | Nome exato + lib Android (citado) | Tamanho/stroke bate? | Match? |
|---|---|---|---|---|
Lembrar: Android usa Lucide Icons — confirmar que o nome do ícone escolhido é o
equivalente exato do usado no `lucide-react`, não uma aproximação visual.

### 5. Sombra/Elevação
| Elemento | box-shadow React (blur/spread/offset/cor) | elevation Android | Match visual? |
|---|---|---|---|
`box-shadow` do CSS não mapeia 1:1 para `elevation` do Compose — comparar
resultado visual renderizado, não só presença de sombra.

### 6. Cantos e bordas
| Elemento | border-radius React (por canto) | corner radius Android (por canto) | Largura/cor de borda bate? | Match? |
|---|---|---|---|---|
Checar cantos assimétricos — erro comum é aplicar radius uniforme quando o
React tem cantos diferentes.

### 7. Estados de interação
| Elemento | Estados no React (hover/focus/disabled/error) | Equivalente Android (pressed/ripple/disabled/error) | Match? |
|---|---|---|---|
`:hover` não existe em mobile — deve virar estado `pressed`/ripple, não ser
simplesmente ignorado. Confirmar que `disabled`, `focus` e `error` também têm
equivalente implementado, não só o estado padrão.

### 8. Gradientes
| Elemento | Direção + color stops React | Direção + color stops Android | Match? |
|---|---|---|---|

### 9. Imagens
| Elemento | object-fit React | ContentScale Android | Match? |
|---|---|---|---|

### 10. Scroll
| Elemento | Comportamento React (overscroll/snap/scroll-padding) | Comportamento Android | Match? |
|---|---|---|---|

### 11. Camadas (z-index)
| Elemento | Ordem/z-index React | Ordem de composição Android | Match? |
|---|---|---|---|

### 12. Animação/Movimento
| Elemento | Propriedades animadas React (todas, não só uma) | Propriedades animadas Android | Match? |
|---|---|---|---|
| — | Duração exata (ms) React | Duração exata Android | Match? |
| — | Easing/curva React (`ease-in-out`, cubic-bezier etc.) | Easing/curva Android (`tween`, `spring`, `FastOutSlowInEasing` etc.) | Curva equivalente ou aproximação grosseira? |
| — | Delay/stagger entre elementos (se lista) | Delay/stagger Android | Match? |

Checar especificamente:
- Se o React anima múltiplas propriedades juntas (ex. opacity + scale +
  translateY), confirmar que TODAS foram portadas — não só opacity.
- Se o movimento no React tem comportamento de "bounce"/física, o equivalente
  correto no Compose é `spring()`, não `tween()`. Marcar como divergência se o
  agente usou tween para simplificar.
- Duração e curva devem ser comparadas valor a valor, não "tem uma transição".

## Saída final esperada
1. As tabelas preenchidas (todas as categorias aplicáveis à tela).
2. Um resumo de divergências no formato Dx (diagnostic item), pronto para virar
   spec, seguindo o padrão já usado no projeto HeroLog.
3. Nenhum item marcado como "Match" sem os dois trechos citados na tabela.
