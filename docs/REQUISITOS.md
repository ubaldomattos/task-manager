# Documento de Requisitos

## 1. Objetivo do sistema

O Task Manager permite que uma pessoa organize suas atividades por data. A tela principal mostra um calendário mensal; ao selecionar um dia, o usuário gerencia as tarefas daquela data. O sistema deve ser simples de instalar, intuitivo, responsivo e visualmente agradável.

## 2. Escopo

### 2.1 Incluído

- Calendário mensal com navegação e indicação de dias com tarefas.
- Cadastro, listagem, edição, exclusão e marcação de conclusão de tarefas.
- Persistência local em SQLite.
- Modo claro e modo escuro.
- Interface responsiva para computador, tablet e celular.

### 2.2 Fora do escopo

- Múltiplos usuários, autenticação e controle de acesso.
- Tarefas recorrentes, lembretes, notificações e prioridades.
- Alteração da data de uma tarefa já cadastrada.
- Sincronização entre dispositivos ou acesso pela internet.
- Lixeira ou recuperação de tarefas excluídas.

## 3. Requisitos funcionais

| ID | Requisito |
|---|---|
| RF01 | O sistema deve exibir, na tela principal, um calendário com os dias do mês. |
| RF02 | O usuário deve poder navegar para o mês anterior e para o mês seguinte. |
| RF03 | O usuário deve poder voltar ao mês e ao dia atuais com um único comando ("Hoje"). |
| RF04 | O calendário deve destacar visualmente o dia atual. |
| RF05 | O calendário deve indicar os dias que possuem tarefas, diferenciando dias com pendências de dias com todas as tarefas concluídas. |
| RF06 | Ao clicar em um dia, o sistema deve exibir todas as tarefas programadas para aquela data. |
| RF07 | O usuário deve poder cadastrar uma tarefa para o dia selecionado, informando título e, opcionalmente, descrição. |
| RF08 | O usuário deve poder editar o título e a descrição de uma tarefa existente. |
| RF09 | O usuário deve poder excluir uma tarefa, após confirmar a operação. |
| RF10 | O usuário deve poder marcar uma tarefa como concluída ou pendente por meio de um checkbox. |
| RF11 | A situação de conclusão deve ser gravada no banco de dados. |
| RF12 | Tarefas concluídas devem ter aparência diferente das pendentes. |
| RF13 | O usuário deve poder alternar entre modo claro e modo escuro. |
| RF14 | A preferência de tema deve ser mantida entre acessos no mesmo navegador. |
| RF15 | O sistema deve exibir mensagens claras de confirmação e de erro para as operações. |

## 4. Requisitos não funcionais

| ID | Requisito |
|---|---|
| RNF01 | O backend deve ser escrito em Java com Spring Boot. |
| RNF02 | Os dados devem ser armazenados em SQLite, acessado por Spring Data JPA e Hibernate. |
| RNF03 | A aplicação não deve exigir a instalação ou a configuração de um SGBD externo. |
| RNF04 | O banco deve ser criado automaticamente na primeira execução. |
| RNF05 | Os dados devem permanecer armazenados entre reinicializações da aplicação. |
| RNF06 | A localização do arquivo do banco deve ser configurável. |
| RNF07 | O esquema do banco deve ser versionado, permitindo evoluções futuras sem perda de dados. |
| RNF08 | A interface deve se adaptar a telas de computador, tablet e celular (a partir de 320 px de largura). |
| RNF09 | A interface deve ter boa legibilidade e contraste mínimo de 4,5:1 para textos, em ambos os temas. |
| RNF10 | As operações não devem recarregar a página. |
| RNF11 | A interface deve poder ser usada com teclado. |
| RNF12 | A aplicação deve ser distribuída como um único arquivo executável (JAR). |
| RNF13 | O código deve seguir separação em camadas, princípios SOLID e tratamento centralizado de erros. |
| RNF14 | O sistema deve registrar em log as operações de alteração de dados e os erros. |
| RNF15 | O sistema deve ter testes automatizados de domínio, serviço, persistência e endpoints. |
| RNF16 | Por padrão, a aplicação deve aceitar conexões somente da própria máquina, pois não tem autenticação. |

## 5. Regras de negócio

| ID | Regra |
|---|---|
| RN01 | Toda tarefa pertence a exatamente uma data programada. |
| RN02 | O título é obrigatório, com no máximo 120 caracteres. Espaços no início e no fim são removidos. |
| RN03 | A descrição é opcional, com no máximo 1000 caracteres. Uma descrição só com espaços é tratada como ausente. |
| RN04 | Toda tarefa nova é criada como pendente. |
| RN05 | A data programada é definida no cadastro e não pode ser alterada na edição. |
| RN06 | É permitido cadastrar tarefas em datas passadas, presentes ou futuras. |
| RN07 | Na lista de um dia, as tarefas pendentes aparecem antes das concluídas. Dentro de cada grupo, a ordem é a de cadastro. |
| RN08 | A data de criação é registrada no cadastro e não muda. A data de atualização é renovada a cada alteração, incluindo a mudança de situação. |
| RN09 | A exclusão é definitiva. |
| RN10 | Um dia é marcado em verde no calendário quando tem tarefas e todas estão concluídas, e em azul quando há ao menos uma pendente. |

## 6. Critérios de aceitação

| Requisito | Critério |
|---|---|
| RF01, RF04 | Ao abrir a aplicação, o mês atual é exibido, com o dia de hoje destacado e selecionado. |
| RF02 | Os botões de anterior e próximo trocam o mês exibido sem recarregar a página, inclusive na virada de ano. |
| RF03 | Após navegar para outro mês, o botão "Hoje" volta ao mês atual e seleciona o dia de hoje. |
| RF05, RN10 | Um dia com uma tarefa pendente mostra um ponto azul; ao concluir a última pendente, o ponto fica verde; ao excluir todas as tarefas, o ponto desaparece. |
| RF06 | Ao clicar em um dia, o painel mostra a data por extenso e somente as tarefas daquela data. |
| RF07, RN02 | Cadastrar com título vazio é recusado com a mensagem "Informe o título da tarefa." Cadastrar com título válido faz a tarefa aparecer na lista e exibe "Tarefa adicionada." |
| RF08 | Após editar e salvar, o novo título e a nova descrição aparecem na lista, e a data continua a mesma. |
| RF09 | Ao clicar em excluir, é pedida confirmação. Cancelar mantém a tarefa; confirmar a remove e exibe "Tarefa excluída." |
| RF10, RF11 | Marcar o checkbox e recarregar a página mantém a tarefa como concluída. |
| RF12 | Uma tarefa concluída aparece com título riscado, cor atenuada e fundo diferente. |
| RF13, RF14 | O botão de tema alterna o tema; ao fechar e abrir o navegador, o último tema escolhido é mantido. |
| RF15 | Se o servidor estiver parado, as operações exibem uma mensagem de erro que explica o problema. |
| RNF04, RNF05 | Na primeira execução o arquivo do banco é criado; após reiniciar a aplicação, as tarefas continuam lá. |
| RNF06 | Definir `TASKMANAGER_DB_PATH` faz a aplicação usar o arquivo indicado. |
| RNF08 | Em uma tela de 390 px de largura, as sete colunas do calendário e todos os controles ficam visíveis sem rolagem horizontal. |

## 7. Restrições e premissas

### Restrições

- Backend em Java com Spring Boot; banco SQLite com Spring Data JPA e Hibernate.
- Sem SGBD externo.
- Código publicado em repositório público no GitHub, sob licença MIT.
- O arquivo do banco local não é versionado.

### Premissas

- O sistema é de uso pessoal, por um único usuário, na própria máquina.
- O usuário tem Java 21 instalado ou pode instalá-lo.
- O navegador é moderno (versões atuais de Chrome, Edge, Firefox ou Safari), com suporte a módulos JavaScript e ao elemento `<dialog>`.
- As datas seguem o calendário local do navegador; a semana começa no domingo.
- A interface é exibida em português do Brasil.
