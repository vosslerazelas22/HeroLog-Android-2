---
name: exploracao-visual-tela
description: Use quando Bruno pedir para trabalhar o visual de uma tela do HeroLog Android ("vamos trabalhar na tela X", "me entregue 3 opções visuais"). Gera 3 opções de UI em Compose com seletor temporário no aparelho, espera a escolha e só então executa a limpeza. Só a camada de UI muda.
---

# Exploração visual de tela (HeroLog Android)

Fluxo em duas fases. A fase 2 (limpeza) só começa quando Bruno disser qual opção escolheu.

## Regra inegociável: só a camada de UI muda

- Não altere ViewModel, estado, repositórios, modelos, lógica, navegação nem textos de comportamento.
- As 3 opções consomem exatamente o mesmo estado e os mesmos callbacks da tela original.
- **A condição que decide se um elemento aparece é comportamento.** Ela fica idêntica à da original. Mudar quando algo é exibido ou ocultado não é decisão de design.
- Se achar que precisa mexer em algo fora da UI, PARE e pergunte.
- Não crie tokens globais. Se precisar de algo novo, sugira no final.
- Não escolha a opção vencedora por Bruno.

## Base visual (não reinventar)

- Tema e tokens: `ui/theme/` (Color, Theme, Type, RitualCardTokens) e `ui/focus/ModeTheme.kt`.
- Respeite a tematização por modo (Padrão / Masmorra / Terra Selvagem) e a bottom nav neutra.
- Insets: use o mesmo mecanismo da original (`LocalBottomBarInset`, em `ui/navigation/BottomBarInset.kt`). Nunca troque por margem fixa.
- As opções variam **layout, hierarquia, densidade e estrutura dos itens** (lista, cards, timeline etc.). Não variam identidade visual (paleta, tipografia, estilo de ícone), textos já aprovados nem funcionalidades.

## Fase 0: preparação (antes de escrever qualquer UI)

1. Leia `PARIDADE.md` e diga em uma frase qual comportamento da tela precisa ser preservado.
2. Liste **todos os elementos condicionais da original** com a condição exata e `arquivo:linha` (vazio geral, toggles, filtros, badges, blocos opcionais, expansões etc.).
3. Identifique **placeholders de feature não implementada** (ex.: texto de crônica IA com `aiChronicle`). Não decida sozinho: sinalize para Bruno e pergunte se mantém visível, ou oculta deixando um slot comentado no lugar.
4. Liste os estados da tela: com dados, vazio (em cada aba ou modo), carregamento, erro, variações por modo de tema.

## Fase 1: entrega das 3 opções

1. Crie `NomeDaTelaV1`, `V2` e `V3` como Composables independentes, em arquivos separados, com abordagens **estruturalmente distintas** (não variações cosméticas da mesma).
2. Crie um seletor temporário ORIG / V1 / V2 / V3 que roda no aparelho, em arquivo próprio (`...Debug.kt`), com fiação mínima e fácil de remover. A tela original fica **intocada**.
3. Adicione `@Preview` por versão, cobrindo: com dados, vazio, com e sem campos opcionais, com badge, e os estados de feature oculta.
4. Teste cada opção com dados de borda e relate o que verificou:
   - badge presente, título longo e texto que pode espremer outros elementos (use `weight`/`maxLines`/ellipsis; título nunca quebra letra por letra);
   - 20+ entradas: o último item não pode ficar sob a bottom nav;
   - estado vazio em cada aba ou modo;
   - os 3 modos de tema;
   - telas 375x667 e 390x844.
5. Legibilidade: sem texto abaixo de tamanho legível no celular; áreas de toque de pelo menos 48dp.
6. Coerência: não repita a mesma informação em dois níveis (ex.: data no cabeçalho do grupo e na entrada) e mantenha o mesmo critério entre abas ou modos da mesma tela.
7. Rode `assembleDebug` e confirme que compila.
8. Entregue o relatório:
   - 2-3 linhas por opção (abordagem + trade-off principal);
   - tabela das condições da fase 0: original x cada opção;
   - divergências classificadas em **aceita** (autorizada por Bruno), **inerente ao design** (explique) ou **corrigida**;
   - resultado do build.
9. PARE. Espere a validação.

## Fase 2: limpeza (só após a escolha)

1. Antes de substituir, compare as condições da opção escolhida com a original e reporte qualquer divergência.
2. O conteúdo da escolhida passa a ser a tela oficial, **mantendo o nome e a assinatura pública da original** (a navegação não muda). Mantenha os Previews (com e sem campos opcionais) e os slots comentados.
3. Apague as opções descartadas, a escolhida (após mover o conteúdo) e o arquivo de seletor. Desfaça a fiação do seletor e remova imports, recursos e tokens que ficaram sem uso.
4. Verificação:
   - `grep -rn "NomeDaTelaV\|NomeDaTelaDebug" app/src` deve retornar vazio;
   - `./gradlew assembleDebug` e `testDebugUnitTest`;
   - `git diff --stat`: só arquivos de UI da tela; liste-os e sinalize qualquer arquivo alheio ao bloco (não o inclua).
5. Registro:
   - `DEV_LOG_ANDROID.md`: opção escolhida, abordagem, motivo e o que foi descartado;
   - `PARIDADE.md`: divergências intencionais (ex.: elemento oculto) com o motivo e onde está o slot comentado.
6. Lembre que a validação visual em aparelho é de Bruno. Build e testes unitários só provam que a lógica não quebrou.

## Armadilhas já observadas (rodada piloto: Crônicas Diárias, 15/10/2026)

- Título quebrado letra por letra: badge e bloco lateral espremendo o título numa `Row`.
- Fallback condicionado a outra coisa (`notes.isBlank()`), uma mudança de lógica disfarçada de design.
- 12dp fixos no fim da lista em vez de `LocalBottomBarInset`: o último item ficaria sob a bottom nav.
- Placeholder de feature inexistente repetido em cada item da lista.
- Dois níveis mostrando a mesma data, e aba com critério de data diferente da outra.
- Texto de stats pequeno e monoespaçado demais para o celular.
