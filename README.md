# Minha Oficina — app Android

App nativo para gerenciar suas ferramentas: cadastro, onde cada uma fica guardada, quanto você investiu, o que está em manutenção e para quem você emprestou.

- Kotlin + Jetpack Compose (Material 3), com tema claro e escuro automáticos
- Banco de dados local **Room (SQLite)**: os dados ficam só no celular e o app funciona sem internet
- Android 8.0 ou mais novo

## Como gerar o APK

### Opção A — Sem instalar nada (GitHub compila para você)

1. Crie uma conta grátis em github.com e crie um repositório novo (pode ser privado).
2. No repositório, toque em **Add file → Upload files** e envie **todo o conteúdo** desta pasta, inclusive a pasta `.github`. É mais fácil fazer isso pelo computador, arrastando a pasta.
   Se a pasta `.github` não aparecer no repositório (alguns navegadores escondem pastas que começam com ponto), use **Add file → Create new file**, digite o nome `.github/workflows/build.yml` e cole o conteúdo desse arquivo.
3. Abra a aba **Actions**. A compilação "Gerar APK" começa sozinha e leva cerca de 5 minutos.
4. Quando terminar (ícone verde), abra a execução e baixe o arquivo **MinhaOficina-apk** em *Artifacts*.
5. Descompacte o .zip no celular e toque no `.apk` para instalar. O Android vai pedir para permitir a instalação de "fontes desconhecidas" para o app que você usou para abrir o arquivo.

### Opção B — Android Studio (no computador)

1. Instale o Android Studio (grátis): developer.android.com/studio
2. **File → Open** e escolha esta pasta. Aguarde a sincronização do Gradle.
3. Para instalar direto no celular, ative a *Depuração USB* nas Opções do desenvolvedor, conecte o cabo e clique em **Run ▶**.
4. Para gerar o arquivo: **Build → Build APK(s)**. O APK fica em `app/build/outputs/apk/`.

## Funções

- Painel de resumo: total de ferramentas, valor investido e proporção por situação
- Busca por nome, marca, local, código ou pessoa; filtros por situação e categoria
- Tela de detalhes com ações rápidas: **Emprestar**, **Registrar devolução** e **Voltou da manutenção**
- Alerta em vermelho para empréstimos com mais de 14 dias
- Código automático para cada ferramenta (FER-001, FER-002…)
- Exportar o inventário em CSV (menu ⋮), que pode ser enviado por WhatsApp ou e-mail, ou aberto no Excel

## Observações

- O APK de release é assinado com a chave de debug, só para facilitar a instalação. Para publicar na Play Store, crie uma chave própria em **Build → Generate Signed Bundle / APK**.
- Desinstalar o app apaga os dados. Use **Exportar inventário** para guardar uma cópia.
