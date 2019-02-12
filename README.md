# spotifier
Componente para consumir a api do spotify

Esta classe essencialmente acessa a api do spotify para buscar playlists de diferentes categorias e preencher uma estrutura de dados.

# Pacotes e Classes Dignas de Nota #

## cashback.spotifier ## 

A classe `Spotifier` recebe um objeto de contexto da aplicação cashbackDomain.setup.CashbackConfig como um `visitor` e acessa a api do Spotify, populando o visitor com os resultados das buscas.

Foi feito um esforço máximo de reduzir a manipulação de objetos do domínio do negócio neste componente. A maior parte da inteligência dos objetos do domínio do negócio foi transferida para o objeto de contexto (`CashbackConfig) ou para outros lugares do projeto cashbackDomain).

## SpotifierTest ##

Um caso de teste simples, para garantir que as informações buscadas no spotify estão sendo corretamente inseridas no sistema.
