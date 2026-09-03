# Agora

Agenda semanal local para Android, pensada para a tela de bloqueio do Samsung
Galaxy S23 via **Good Lock + LockStar**. A versão **0.2.0** substitui os dados de
demonstração por uma agenda completa que funciona offline.

## O que funciona

- cadastro, edição e exclusão de compromissos e tarefas;
- horários opcionais, observações e quatro categorias coloridas;
- repetição diária ou semanal em dias selecionados, com data final opcional;
- conclusão de tarefas por ocorrência (concluir hoje não conclui a próxima);
- semana navegável no app, com indicadores reais por dia;
- banco local Room, sem conta e sem servidor;
- widget **Agenda completa** com semana, até quatro compromissos e três tarefas;
- widget **Resumo compacto** com o próximo compromisso e duas tarefas;
- tarefas marcáveis diretamente pelo widget;
- atualização imediata ao editar e atualização de segurança a cada 15 minutos;
- atualização ao mudar data, fuso ou idioma;
- testes unitários do motor de recorrência;
- APK compilado automaticamente pelo GitHub Actions.

## Instalar o APK de teste

1. Abra a aba **Actions** do repositório.
2. Entre na execução verde mais recente de **Build Agora APK**.
3. Baixe o artefato `agora-debug-apk` e extraia o ZIP.
4. Como a assinatura de teste mudou nesta versão, desinstale a versão 0.1 antes
   da instalação da 0.2. Como o APK é de debug, uma compilação futura também
   pode exigir reinstalação; a assinatura definitiva será configurada no release.
5. Instale `app-debug.apk`, abra o Agora e cadastre seus itens.

Nenhuma chave privada é versionada. O GitHub Actions cria uma assinatura de
debug temporária, que nunca deve ser usada para uma publicação oficial.

## Colocar na tela de bloqueio

1. No app, toque em **Widgets** e escolha o tamanho desejado.
2. Teste primeiro na tela inicial e ajuste o tamanho.
3. Abra **Good Lock → LockStar** e ative a edição da tela de bloqueio.
4. Toque na área de widgets, selecione **Agora** e posicione-o abaixo do relógio.
5. Se a versão antiga permanecer em cache, remova o widget e adicione-o novamente.

Dependendo da One UI, abrir a tela de edição a partir do widget pode exigir o
desbloqueio. Marcar uma tarefa é feito por um broadcast interno e atualiza ambos
os widgets.

## Compilar localmente

Requisitos: Android Studio atual, Java 17 e Android SDK 35. Abra o projeto,
aguarde o Gradle Sync e execute a configuração `app` em um dispositivo com
Android 12 ou superior.

Pelo terminal com Gradle 8.10.2:

```bash
gradle :app:testDebugUnitTest :app:assembleDebug
```

O APK fica em `app/build/outputs/apk/debug/app-debug.apk`.

## Arquitetura

```text
app/src/main/java/com/ysmmfe/agora
├── data/       Room, entidades, DAO e repositório
├── domain/     cálculo de recorrências
├── ui/         lista e formatação do app
├── widget/     widget compacto, ações e atualização periódica
├── MainActivity.kt
├── EditItemActivity.kt
└── AgoraWidgetProvider.kt
```

## Escopo desta versão

Os dados ficam somente no aparelho. Google Calendar e Google Tasks não estão
incluídos porque exigem um projeto Google Cloud, tela de consentimento OAuth e
credenciais ligadas à assinatura final do app. O modelo local já foi estruturado
para essa integração futura sem bloquear o uso atual.
