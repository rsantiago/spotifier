# spotifier
Componente para consumir a api do spotify

Esta classe essencialmente acessa a api do spotify para buscar playlists de diferentes categorias e preencher uma estrutura de dados.

## Dado Importante ##

Até onde consegui descobrir, é impossível recuperar a lista de discos por gênero no spotify. O que é possível fazer, na verdade, é recuperar a lista de **Playlists**. O código reflete esta restrição. Os discos, na verdade, são as playlists. E o nome do artista da playlist é o artista da primeira faixa.

Exemplo:
Se o spotify tem uma categoria "pagode", haverá uma infinidade de playlists de pagode. A massa delas tem o mesmo dono: o próprio spotify. Então, o programa pega as N primeiras listas de pagode (parametrizado no código), constrói um objeto `Album` com essa lista de músicas e atribui como artista o artista da primeira faixa da playlist.

Além disso, há gêneros que são acessados que não têm qualquer playlist associada. O sistema também reflete isso, descartando os gêneros sem playlists.

Uma maneira simples de popular a lista de discos é fazendo o webscraping de um site. Por exemplo, o da Som Livre, que tem discos por gênero, com o nome do artista, tudo em formato padrão. Seria bem tranquilo de fazer.

## Pacotes e Classes ##

### cashback.spotifier ### 

A classe `Spotifier` recebe um objeto de contexto da aplicação cashbackDomain.setup.CashbackConfig como um `visitor` e acessa a api do Spotify, populando o visitor com os resultados das buscas.

Foi feito um esforço máximo de reduzir a manipulação de objetos do domínio do negócio neste componente. A maior parte da inteligência dos objetos do domínio do negócio foi transferida para o objeto de contexto (`CashbackConfig) ou para outros lugares do projeto cashbackDomain).

## SpotifierTest ##

Um caso de teste simples, para garantir que as informações buscadas no spotify estão sendo corretamente inseridas no sistema.
