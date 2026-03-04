# Controle de Estoque Android

Aplicativo Android para controle de estoque de produtos, com entrada, saída, controle de validade e notificações automáticas.

---

## 📋 Descrição

O **Controle de Estoque** é um aplicativo Android nativo desenvolvido em **Kotlin** com **Jetpack Compose**, seguindo a arquitetura **MVVM**. Ele permite:

- Cadastrar e gerenciar produtos com data de validade
- Registrar entradas e saídas de estoque
- Visualizar histórico completo de movimentações
- Receber **notificações automáticas** sobre produtos vencidos ou próximos do vencimento
- Filtrar e buscar produtos por nome, categoria, validade ou estoque baixo

---

## 🚀 Como Rodar o Projeto no Android Studio

### Pré-requisitos
- **Android Studio** Hedgehog (2023.1.1) ou superior
- **JDK 11** ou superior
- **Android SDK** com API 26+ instalado

### Passos

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/RomeroGalindo/controle-estoque-android.git
   cd controle-estoque-android
   ```

2. **Abra no Android Studio:**
   - Abra o Android Studio
   - Clique em `File > Open`
   - Selecione a pasta `controle-estoque-android`
   - Aguarde o Gradle sincronizar

3. **Execute o app:**
   - Conecte um dispositivo Android (API 26+) ou crie um emulador
   - Clique em `Run > Run 'app'` ou pressione `Shift+F10`

---

## 🔑 Permissões Necessárias

| Permissão | Uso |
|-----------|-----|
| `POST_NOTIFICATIONS` | Enviar notificações de validade (Android 13+) |
| `RECEIVE_BOOT_COMPLETED` | Reagendar o WorkManager após reinicialização |

O app solicita a permissão de notificação automaticamente ao iniciar no Android 13+.

---

## 🏗️ Arquitetura e Tecnologias

| Componente | Tecnologia |
|-----------|-----------|
| Linguagem | Kotlin |
| Arquitetura | MVVM |
| UI | Jetpack Compose + Material 3 |
| Banco de dados | Room |
| Injeção de dependência | Hilt |
| Background tasks | WorkManager |
| Navegação | Navigation Compose |
| Preferências | DataStore Preferences |
| Programação assíncrona | Coroutines + Flow |

---

## 📁 Estrutura do Projeto

```
app/src/main/java/com/example/controleestoque/
├── ControleEstoqueApplication.kt   # Application class + agendamento WorkManager
├── MainActivity.kt                  # Activity principal
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt           # Banco de dados Room
│   │   ├── dao/
│   │   │   ├── ProdutoDao.kt        # DAO para produtos
│   │   │   └── MovimentacaoDao.kt   # DAO para movimentações
│   │   └── entity/
│   │       ├── Produto.kt           # Entidade produto
│   │       └── Movimentacao.kt      # Entidade movimentação + enum TipoMovimentacao
│   └── repository/
│       ├── ProdutoRepository.kt     # Repositório de produtos
│       └── MovimentacaoRepository.kt # Repositório de movimentações (lógica entrada/saída)
├── di/
│   └── DatabaseModule.kt            # Módulo Hilt para injeção de dependência
├── ui/
│   ├── components/
│   │   └── ProdutoCard.kt           # Componente card de produto com indicadores visuais
│   ├── navigation/
│   │   ├── Screen.kt                # Definição de rotas de navegação
│   │   └── NavGraph.kt              # Grafo de navegação Compose
│   ├── screens/
│   │   ├── home/
│   │   │   └── HomeScreen.kt        # Lista de produtos com busca e filtros
│   │   ├── produto/
│   │   │   └── ProdutoFormScreen.kt # Cadastro/edição de produto
│   │   ├── movimentacao/
│   │   │   └── MovimentacaoScreen.kt # Registro de movimentações + histórico
│   │   ├── relatorios/
│   │   │   └── RelatoriosScreen.kt  # Visão geral e relatórios
│   │   └── configuracoes/
│   │       └── ConfiguracoesScreen.kt # Configurações (dias aviso, estoque mínimo)
│   └── theme/
│       ├── Color.kt                 # Paleta de cores (incluindo status de validade)
│       ├── Theme.kt                 # Tema Material 3 com suporte a dark mode
│       └── Type.kt                  # Tipografia
├── utils/
│   ├── DateUtils.kt                 # Utilitários de data e verificação de validade
│   └── ConfiguracoesManager.kt      # Gerenciador de preferências (DataStore)
├── viewmodel/
│   ├── ProdutoViewModel.kt          # ViewModel da tela de produtos
│   ├── MovimentacaoViewModel.kt     # ViewModel das movimentações
│   └── ConfiguracoesViewModel.kt    # ViewModel de configurações
└── worker/
    └── ExpirationCheckWorker.kt     # WorkManager: verificação diária de validades
```

---

## 🗄️ Banco de Dados

### Entidade: Produto

| Campo | Tipo | Descrição |
|-------|------|-----------|
| id | Long (PK) | Identificador auto-gerado |
| nome | String | Nome do produto (obrigatório) |
| codigoBarras | String? | Código de barras (opcional) |
| categoria | String | Categoria do produto |
| quantidadeAtual | Int | Quantidade atual em estoque |
| unidade | String | Unidade de medida (ex: kg, litro) |
| dataValidade | Long | Timestamp da data de validade |
| localizacao | String | Localização física |
| observacoes | String | Observações livres |
| quantidadeMinima | Int | Alerta de estoque baixo (0 = usa global) |

### Entidade: Movimentacao

| Campo | Tipo | Descrição |
|-------|------|-----------|
| id | Long (PK) | Identificador auto-gerado |
| produtoId | Long (FK) | Referência ao produto |
| tipo | TipoMovimentacao | ENTRADA ou SAIDA |
| quantidade | Int | Quantidade movimentada |
| dataHora | Long | Timestamp da movimentação |
| observacoes | String | Observações |

---

## 🔔 Sistema de Notificações

O `ExpirationCheckWorker` é agendado via WorkManager para executar **uma vez por dia**. Ele:

1. Lê a configuração de dias antes do vencimento (padrão: 7 dias)
2. Busca produtos com validade ≤ hoje + N dias
3. Busca produtos já vencidos (validade < hoje)
4. Envia notificações locais para cada produto problemático com:
   - Nome do produto
   - Data de validade
   - Quantidade em estoque

O agendamento usa `ExistingPeriodicWorkPolicy.KEEP` para não duplicar o worker.

---

## 🎨 Indicadores Visuais

| Status | Cor | Condição |
|--------|-----|----------|
| 🔴 Vencido | Vermelho | Produto já passou da data de validade |
| 🟠 Próximo | Laranja | Dentro do período configurado |
| 🟡 Estoque Baixo | Amarelo | Quantidade ≤ mínimo configurado |
| ✅ Normal | Verde/Padrão | Sem alertas |

---

## 🧪 Testes

Os testes unitários cobrem:

- **`ExpirationCheckTest`**: Lógica de detecção de produtos vencidos e próximos do vencimento, cálculo de dias para vencer, formatação de datas
- **`MovimentacaoLogicaTest`**: Cálculo de nova quantidade após entrada/saída, validação de estoque insuficiente

Execute os testes no Android Studio com `Run > Run Tests` ou via linha de comando:
```bash
./gradlew test
```

---

## 📸 Screenshots

> _Screenshots serão adicionados após a execução do aplicativo._

| Tela Inicial | Cadastro de Produto | Movimentações |
|:---:|:---:|:---:|
| _(placeholder)_ | _(placeholder)_ | _(placeholder)_ |

| Relatórios | Configurações | |
|:---:|:---:|:---:|
| _(placeholder)_ | _(placeholder)_ | |

---

## 📄 Licença

MIT License – consulte o arquivo LICENSE para detalhes.
