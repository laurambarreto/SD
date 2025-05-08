1.⁠ ⁠Arquitetura do software:
Esta aplicação googol consiste em quatro classes principais:  Downloader, Barrel, Gateway e Client
Esta arquitetura funciona da seguinte forma: o cliente envia pedidos à gateway, esta por não conter informações apenas reencaminha o pedido a um barrel ativo, executando o método remoto desse barrel. De seguida, a gateway recebe os resultados retornados pelo método e devolve-os ao cliente que obtém, finalmente, a resposta ao que pediu.

    1. Downloaders: São responsáveis por analisar toda a informação das páginas Web, enviando-a aos barrels. Deverão ser executados em vários terminais trabalhando em paralelo, para otimizar o desempenho e aumentar o índice das palavras mais rapidamente. 

    2. Barrels: Representam o servidor central, onde ficam armazenados o ConcurrentHashMap de nome "processed" (é o índice que associa palavras-chave a URLs que as contêm) e um outro ConcurrentHashMap de nome "reachable" (que associa um URL a um conjunto de URLs que apontam para ele, para medir a sua popularidade). Como têm esta informação guardada respondem a pedidos e retornam os resultados à gateway, para esta os devolver ao cliente. Deveriam manter sincronização através de um protocolo de reliable multicast para conterem sempre a mesma informação, no entanto ainda não o conseguimos implementar.

    3. Gateway: Atua como intermediário entre os clientes e o barrel reencaminhando os pedidos para os barrels através de RMI. Recebe posteriormente, o resultado da pesquisa e devolve-o ao cliente. Para além disso, gere a comunicação com os barrels, fazendo a verificação de quais estão ativos. Contém uma queue (fila), onde estão os URLs à espera de serem processados, para serem inseridos no ConcurrentHashMap "processed" presente no barrel.

    4. Client: É o cliente RMI usado pelos utilizadores com um menu bastante simples. O cliente poderá fazer uma pesquisa introduzindo um conjunto de palavras e recebendo depois todos os links presentes no ConcurrentHashMap "processed" e que contenham todas as palavras da pesquisa, vindo ordenados por popularidade (ou seja, por ordem decrescente do número de links que "apontam" para cada Url resultante da pesquisa). Poderá também, indexar um novo Url na Queue, presente na gateway, para, assim que possível, ser processado e acrescentado ao "processed" presente nos barrels. Finalmente, também pode inserir um Url e obter todos os links que contenham esse mesmo Url. Desta forma, o cliente apenas envia pedidos à gateway via RMI.

2.⁠ ⁠Replicação do índice e algoritmo usado para comunicação multicast fiável
Ainda não conseguimos implementar esta etapa do protocolo multicast para os barrels.

3.⁠ ⁠Funcionamento da componente RMI (métodos remotos, callbacks e solução para failover)
Foram usados métodos remotos nomeadamente as interfaces Gateway_int.java e Barrel_int.java. Estas têm funções que podem ser executadas noutras classes caso seja criado um objeto do tipo Gateway_int ou Barrel_int e chamadas funções que essas interfaces implementam. Ainda não foi implementado callbacks, tendo o cliente que esperar pela resposta ao seu pedido antes de realizar outra pesquisa, no entanto achámos que seria mais lógico desta forma. A única solução de failover implementada até então é um ficheiro que guarda o objeto barrel assim que é terminada a sua execução, permitindo que numa próxima execução se tenha acesso ao index até então acumulado desta classe.

4.⁠ ⁠Distribuição de tarefas 
Optámos por desenvolver o trabalho em conjunto, de forma a garantir um conhecimento aprofundado de todas as funcionalidades implementadas.

5.⁠ ⁠Descrição dos testes realizados (tabela com descrição e pass/fail de cada teste)
Funcionalidades que conseguimos implementar:
    1. Indexar novo Url;
    2. Indexar recursivamente/iterativamente todos os Urls encontrados;
    3. Pesquizar páginas que contenham um conjunto de termos (ainda não aparecem agrupados de 10 em 10);
    4. Resustados de pesquisa ordenados por importância;
    5. Consultar lista de páginas com ligação para uma página específica.

Nota: Com algumas dificuldades conseguimos pôr o servidor num computador em conjunto com os barrels e downloaders e aceder com o outro computador sendo um cliente.

Funcionalidade inacabada:
    6. Aprendizagem computacional distribuída de "stop words" (começamos a implementar mas ficou comentado no código)

1. Arquitetura do software 
2. Detalhes sobre a integração do Spring Boot / FastAPI com o Servidor RPC/RMI da primeira meta
3. Detalhes sobre a programação de WebSockets e a sua integração com o servidor RPC/RMI
4. Detalhes sobre a integração com os serviços REST
5. Descrição dos testes feitos à plataforma (pass/fail)
