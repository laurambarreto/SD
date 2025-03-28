Como executar:

NOTA: Poderá ter de converter os ficheiros .sh para usarem as terminações e end of line characters corretas 
do seu sistema operativo; Se usa Windows recomendamos utilizar os ficheiros .cmd.

Escrever em terminais separados:

1.⁠ Execute o ficheiro "build.sh" (compila as classes incorporando o Jsoup) e de seguida execute no mesmo terminal 
o ficheiro "run-gateway.sh" (inicializa o servidor gateway);
2.⁠ ⁠Execute "run-barrel.sh" (inicializa um barrel);
3.⁠ Execute ⁠"run-downloader.sh" (inicializa o downloader que começa de imediato a navegar entre as páginas web 
processando as palavras e links);
4.⁠ Execute ⁠"run-client.sh" (terminal onde o cliente poderá experimentar as várias funcionalidades do menu: pesquisar,
adicionar um novo URL para ser processado e ainda saber todas as páginas a partir das quais um determinado URL inserido
por ele podem ser encontradas).

Nota: deverá correr mais do que um downloader para mais resultados serem encontrados rapidamente.