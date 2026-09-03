# Agora

Protótipo Android de um calendário semanal com foco no dia atual, criado para
ser usado como widget na tela inicial e, em aparelhos Samsung compatíveis, na
tela de bloqueio por meio do Good Lock + LockStar.

Esta é a versão **0.1.1 — ajuste para o LockStar**. A compatibilidade com o
Galaxy S23 foi confirmada em aparelho real. Os compromissos e tarefas ainda são
demonstrativos; esta versão corrige a altura e a transparência observadas no teste.

## O que já funciona

- semana calculada automaticamente, de segunda a domingo;
- destaque automático do dia atual;
- atualização ao mudar a data, o fuso ou o idioma;
- atualização periódica pelo sistema;
- visual transparente com indicadores coloridos;
- agenda e tarefas demonstrativas;
- toque no widget para abrir o app;
- botão no app para solicitar a adição à tela inicial;
- suporte declarado às categorias `home_screen` e `keyguard`;
- redimensionamento horizontal e vertical.
- cartão visual limitado ao conteúdo, sem preencher a área vazia do LockStar;
- compilação automática também em pull requests.

## Abrir e compilar no Android Studio

1. Instale uma versão atual do Android Studio.
2. Na tela inicial, escolha **Open** e selecione esta pasta.
3. Caso o Android Studio pergunte pela versão do Gradle, escolha **8.10.2**.
4. Aguarde o Gradle Sync e a instalação do Android SDK 35.
5. Ative a depuração USB no Galaxy S23 ou crie um emulador com Android 12 ou superior.
6. Selecione o dispositivo e clique em **Run**.

Configuração usada:

- Java 17;
- Kotlin 2.0.21;
- Android Gradle Plugin 8.8.2;
- compile/target SDK 35;
- minSdk 31, equivalente ao Android 12.

## Gerar APK pelo Android Studio

No menu, use:

`Build → Build Bundle(s) / APK(s) → Build APK(s)`

O arquivo será criado em:

`app/build/outputs/apk/debug/app-debug.apk`

## Gerar APK pelo GitHub Actions

O projeto inclui o workflow `.github/workflows/build-apk.yml`, que não depende
de um Gradle instalado no repositório.

1. Crie um repositório no GitHub e envie esta pasta.
2. Abra a aba **Actions**.
3. Execute **Build Agora APK**.
4. Ao final, baixe o artefato `agora-debug-apk`.

## Testar no Galaxy S23

### Primeiro, na tela inicial

1. Instale e abra o app Agora.
2. Toque em **Adicionar widget à tela inicial**.
3. Confirme a solicitação do sistema.
4. Redimensione o widget e confira se nada foi cortado.

### Depois, na tela de bloqueio

1. Instale o **Good Lock** pela Galaxy Store.
2. Instale e abra o módulo **LockStar**.
3. Ative a edição da tela de bloqueio.
4. Entre na área de widgets e procure por **Agora**.
5. Posicione-o abaixo do relógio.
6. Aumente a altura até mostrar a agenda e as tarefas.

Dependendo da versão da One UI, tocar no widget pode solicitar o desbloqueio.
Isso é uma política do sistema e não um defeito do app.

## Checklist do teste

Anote ou tire uma captura de tela destes pontos:

- o widget aparece na lista do LockStar?
- qual é o menor tamanho legível?
- a transparência combina com o papel de parede?
- o dia atual está destacado corretamente?
- algum texto fica cortado?
- a tela bloqueada mostra todos os quatro compromissos?
- tocar no widget pede desbloqueio?
- o Always On Display também aceita o widget?

## Próxima versão

A versão 0.2 receberá:

- banco local Room;
- cadastro, edição e exclusão de compromissos;
- tarefas concluíveis;
- eventos recorrentes por dia da semana;
- categorias e cores personalizadas;
- conteúdo real no widget;
- layout responsivo para alturas menores;
- widget compacto separado.

## Estrutura

```text
app/src/main
├── java/com/ysmmfe/agora
│   ├── AgoraWidgetProvider.kt
│   └── MainActivity.kt
├── res
│   ├── drawable
│   ├── layout
│   │   ├── activity_main.xml
│   │   └── agora_widget.xml
│   ├── values
│   └── xml/agora_widget_info.xml
└── AndroidManifest.xml
```
