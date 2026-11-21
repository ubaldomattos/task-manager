# Manual de Instalação

Este manual explica como instalar, configurar, compilar e executar o Task Manager em Windows, Linux ou macOS, sem conhecimento prévio do projeto.

## 1. Pré-requisitos

| Item | Versão | Observação |
|---|---|---|
| Java (JDK) | 21 ou superior | Obrigatório. |
| Navegador | Versão atual de Chrome, Edge, Firefox ou Safari | Para usar a aplicação. |
| Git | Qualquer versão recente | Opcional; também é possível baixar o código em ZIP. |
| Internet | Na primeira compilação | O Maven baixa as dependências (cerca de 60 MB). |

Não é preciso instalar Maven nem banco de dados. O projeto inclui o Maven Wrapper, que baixa o Maven automaticamente, e o banco SQLite é um arquivo criado pela própria aplicação.

## 2. Instalação do Java

Primeiro, verifique se o Java já está instalado. Abra um terminal (no Windows, o **Prompt de Comando** ou o **PowerShell**) e execute:

```bash
java -version
```

Se aparecer `version "21..."` ou um número maior, pule para a seção 3. Se aparecer um número menor ou "comando não encontrado", instale o JDK 21 conforme o seu sistema.

### Windows

1. Acesse <https://adoptium.net/temurin/releases/?version=21>, escolha **Windows**, **x64**, **JDK** e baixe o instalador `.msi`.
2. Execute o instalador. Na tela de recursos, marque **Set JAVA_HOME variable** e **Add to PATH**.
3. Feche e abra o terminal e confira com `java -version`.

Alternativa pelo terminal: `winget install EclipseAdoptium.Temurin.21.JDK`

### Linux (Debian/Ubuntu)

```bash
sudo apt update
sudo apt install openjdk-21-jdk
java -version
```

### macOS

Com [Homebrew](https://brew.sh): `brew install --cask temurin@21`. Ou baixe o instalador `.pkg` em <https://adoptium.net>.

## 3. Obtenção do código

Com Git:

```bash
git clone https://github.com/ubaldomattos/task-manager.git
cd task-manager
```

Sem Git: na página <https://github.com/ubaldomattos/task-manager>, clique em **Code > Download ZIP**, extraia o arquivo e abra um terminal na pasta extraída.

## 4. Configuração

A aplicação funciona sem nenhuma configuração. Se quiser alterar o comportamento padrão, defina estas variáveis de ambiente antes de executar:

| Variável | Padrão | Descrição |
|---|---|---|
| `TASKMANAGER_DB_PATH` | `./data/taskmanager.db` | Caminho do arquivo SQLite. Um caminho relativo parte do diretório onde a aplicação é executada. |
| `TASKMANAGER_PORT` | `8080` | Porta HTTP. |
| `TASKMANAGER_ADDRESS` | `127.0.0.1` | Endereço de escuta. Use `0.0.0.0` para permitir acesso de outros dispositivos da rede (ver aviso abaixo). |

Exemplos:

```powershell
# Windows (PowerShell), vale para a janela atual
$env:TASKMANAGER_DB_PATH = "C:\Users\voce\Documents\tarefas.db"
$env:TASKMANAGER_PORT = "9090"
```

```cmd
:: Windows (Prompt de Comando)
set TASKMANAGER_DB_PATH=C:\Users\voce\Documents\tarefas.db
```

```bash
# Linux / macOS
export TASKMANAGER_DB_PATH="$HOME/tarefas.db"
```

As mesmas opções podem ser passadas como argumentos na execução do JAR:

```bash
java -jar target/task-manager.jar --taskmanager.database.path=/caminho/tarefas.db --server.port=9090
```

> **Aviso:** a aplicação não tem login. Com `TASKMANAGER_ADDRESS=0.0.0.0`, qualquer pessoa na mesma rede pode ver e alterar suas tarefas. Use apenas em redes confiáveis.

## 5. Compilação

Na pasta do projeto:

```bash
# Linux / macOS
./mvnw package

# Windows
mvnw.cmd package
```

O comando baixa as dependências, executa os testes automatizados e gera o arquivo `target/task-manager.jar`. Ao final deve aparecer `BUILD SUCCESS`.

Para compilar sem rodar os testes, acrescente `-DskipTests`.

No Linux ou macOS, se aparecer "Permission denied" ao executar `./mvnw`, rode `chmod +x mvnw` uma vez.

## 6. Execução

Escolha uma das formas:

**A partir do JAR (recomendado após compilar):**

```bash
java -jar target/task-manager.jar
```

**Direto pelo Maven, sem gerar o JAR:**

```bash
./mvnw spring-boot:run      # Windows: mvnw.cmd spring-boot:run
```

Aguarde a mensagem `Started TaskManagerApplication` e abra <http://localhost:8080> no navegador.

Para encerrar, pressione `Ctrl + C` no terminal.

O JAR é autossuficiente: pode ser copiado para outra pasta ou outra máquina com Java 21 e executado do mesmo modo.

## 7. Localização do banco SQLite

Por padrão o banco fica em `data/taskmanager.db`, **relativo ao diretório de onde a aplicação foi executada**. Se você executar a partir da pasta do projeto, o arquivo estará em `task-manager/data/taskmanager.db`.

O caminho completo é exibido no log de inicialização:

```
INFO ... DatabaseConfig : Banco de dados SQLite: C:\...\task-manager\data\taskmanager.db
```

Observações:

- O arquivo e a pasta são criados na primeira execução.
- **Backup:** com a aplicação parada, copie o arquivo `.db`. Para restaurar, coloque a cópia no mesmo caminho.
- **Consulta direta:** o arquivo pode ser aberto com ferramentas como [DB Browser for SQLite](https://sqlitebrowser.org).
- O arquivo não é versionado no Git (está no `.gitignore`).
- Para começar do zero, pare a aplicação e apague o arquivo. Um banco vazio será criado na próxima execução.

## 8. Solução de problemas

### `java` não é reconhecido ou a versão é menor que 21

O Java não está instalado ou não está no `PATH`. Reinstale conforme a seção 2, marcando a opção de adicionar ao `PATH`, e abra um novo terminal. Se houver mais de uma versão instalada, ajuste a variável `JAVA_HOME` para a pasta do JDK 21.

### `release version 21 not supported` ao compilar

O Maven está usando um JDK antigo. Confira com `./mvnw -v` qual Java é usado e ajuste `JAVA_HOME` para o JDK 21.

### `Port 8080 was already in use`

Outro programa já usa a porta 8080, possivelmente outra instância do Task Manager. Encerre o outro programa ou use outra porta:

```bash
java -jar target/task-manager.jar --server.port=9090
```

### Falha ao baixar dependências ou o Maven (rede corporativa com proxy)

Em redes com proxy, o Maven precisa conhecer o servidor proxy. Crie ou edite o arquivo `settings.xml` na pasta `.m2` do seu usuário (`C:\Users\<usuario>\.m2\settings.xml` ou `~/.m2/settings.xml`):

```xml
<settings>
  <proxies>
    <proxy>
      <id>proxy</id>
      <active>true</active>
      <protocol>https</protocol>
      <host>proxy.suaempresa.com</host>
      <port>8080</port>
    </proxy>
  </proxies>
</settings>
```

Se o proxy exigir usuário e senha, evite gravar a senha em texto puro: use a [criptografia de senhas do Maven](https://maven.apache.org/guides/mini/guide-encryption.html).

Se o Maven Wrapper não conseguir baixar o Maven, instale o Maven manualmente e use `mvn` no lugar de `./mvnw`.

### `SQLITE_CANTOPEN` ou "Não foi possível criar o diretório do banco de dados"

A aplicação não tem permissão para gravar no caminho configurado. Escolha um caminho dentro da sua pasta de usuário com `TASKMANAGER_DB_PATH`.

### `database is locked`

Duas instâncias da aplicação estão usando o mesmo arquivo de banco. Encerre uma delas.

### A página abre, mas aparece "Não foi possível conectar ao servidor"

A aplicação foi encerrada ou reiniciada. Verifique o terminal, execute-a novamente e recarregue a página.

### A página não abre a partir de outro computador ou do celular

Por segurança, a aplicação aceita apenas acessos da própria máquina. Veja `TASKMANAGER_ADDRESS` na seção 4. Também pode ser necessário liberar a porta no firewall.

### Acentos aparecem trocados no terminal do Windows

É apenas a exibição do terminal; os dados são gravados corretamente. Execute `chcp 65001` antes de iniciar a aplicação para usar UTF-8 no console.

### A interface parece desatualizada após uma atualização

O navegador pode estar usando arquivos em cache. Recarregue com `Ctrl + Shift + R` (ou `Cmd + Shift + R` no macOS).
