## 📋 Pré-requisitos

É necessário ter uma versão do Gurobi autenticada com chave de acesso para rodar a aplicação. Para mais informações, siga os tutoriais disponíveis no site deles [aqui](https://www.gurobi.com/features/academic-named-user-license/) e [aqui](https://www.gurobi.com/documentation/quickstart.html).

## ▶️ Rodar a aplicação
Primeiro, rode o seguinte comando na pasta do projeto para complilar a aplicação, substituindo `<JAR_PATH>` pelo local em que se encontra o arquivo `gurobi.jar`. Se seguiu os tutoriais à risca, ele provavelmente está localizado dentro da pasta `lib` no local de instalação do Gurobi. Ele também pode ser encontrado através da variável `LD_LIBRARY_PATH` com `$LD_LIBRARY_PATH/gurobi.jar` no linux se as variáveis de ambiente foram definidas corretamente:

```bash
javac -d bin -sourcepath src -cp ".:<JAR_PATH>:" src/App.java
```

Após a compilação, rode o seguinte comando, substituindo cada argumento pelo valor que deseja passar à aplicação:

```bash
java -cp ".:<JAR_PATH>:./bin" App <GRAFO> <PRIMAL> <RELAXADO>
```

### 📝 Argumentos
- `<JAR_PATH>`: localização do arquivo `gurobi.jar`
- `<GRAFO>`: nome do arquivo `.txt` que possui a representação do grafo colorido
- `<PRIMAL>`: indica se deve utilizar a versão primal ou dual para solucionar o problema e aceita como valores válidos `primal` e `dual`
- `<RELAXADO>`: indica se deve tratar as variáveis do problema como inteiras ou contínuas e aceita como valores válidos `integer` e `relaxed`

## 📥 Entrada
Recebe um arquivo `.txt` que representa um grafo colorido. O arquivo de entrada deve seguir o seguinte padrão de 3 linhas:

- primeira linha indica as cores do grafo, representadas de 0 a N, separadas por espaço
- segunda linha indica as arestas do grafo representadas por triplas separadas por `;` na forma `(i,j,p)`, onde `i` e `j` são os nós da aresta e p é o seu peso
- terceira linha representa os nós `s` e `t` do grafo separados por espaço

## 🎯 Exemplos
Exemplos de arquivos de entrada podem ser encontrados na wiki do projeto [aqui](https://github.com/borinjrjose/TSPP/wiki/Exemplos).
