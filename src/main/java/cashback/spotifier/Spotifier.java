package cashback.spotifier;

import java.io.IOException;


import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Category;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.browse.GetCategorysPlaylistsRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistsTracksRequest;

import cashbackDomain.album.Genre;
import cashbackDomain.setup.CashbackConfig;

/**
 * Classe exclusiva para recuperar os dados do spotify e retornar uma estrutura
 * de dados com os discos e categorias
 * 
 * @author rodrigosantiago
 *
 */
public class Spotifier {
	/**
	 * O modelo de uma loja inteira em um determinado instante
	 */
	private CashbackConfig config;

	/**
	 * Máximo número de categorias padrão 
	 * O número de categorias retornada nesta chamada,
	 * para o Brasil, é sempre menor do que 50
	 * 
	 */
	private static final int DEFAULT_MAX_CATEGORY_COUNT = 50;

	/**
	 * Número máximo padrão de playlists por categoria
	 */
	private static final int DEFAULT_MAX_LIST_COUNT = 50;

	/**
	 * O Identificador do Cliente - como configurado no próprio site do spotify
	 */
	private final static String clientId = "bfa2bac383ef41a591741393bc33b6b0";
	/**
	 * O Segredo do cliente - como configurado no próprio site do spotify
	 */
	private final static String clientSecret = "524b90f982b6423aa52e0709ce1a3302";

	/**
	 * A instância da api que será usada para fazer as chamadas
	 */
	private SpotifyApi spotifyApi = null;

	/**
	 * Número máximo de categorias
	 */
	private int maxCategoryCount;
	/**
	 * Número máximo de playlists por categoria
	 */
	private int maxListCount;
	
	/**
	 * Construtor da classe
	 * 
	 * @param config A configuração de contexto do domínio cashback
	 * 
	 * @throws SpotifyWebApiException Caso a aplicação seja incapaz de buscar as informações no spotify
	 * @throws IOException Caso haja alguma outra exceção de entrada e saída de dados
	 * 
	 */
	public Spotifier(CashbackConfig config) throws SpotifyWebApiException, IOException {
		apiSetup();
		this.config = config;
		this.maxCategoryCount = DEFAULT_MAX_CATEGORY_COUNT;
		this.maxListCount = DEFAULT_MAX_LIST_COUNT;
	}
	
	
	public Spotifier(CashbackConfig config, int maxCategoryCount, int maxListCount) throws SpotifyWebApiException, IOException {
		this(config);
		this.maxCategoryCount = maxCategoryCount;
		this.maxListCount = maxListCount;
	}

	/**
	 * Configura a api com os
	 * 
	 * @return
	 * @throws SpotifyWebApiException
	 * @throws IOException
	 */
	private void apiSetup() throws SpotifyWebApiException, IOException {
		// construindo a instância do spotify api
		spotifyApi = new SpotifyApi.Builder().setClientId(clientId).setClientSecret(clientSecret).build(); // é
																											// necessário
																											// configurar
																											// Id e
																											// segredo

		final ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
		final ClientCredentials clientCredentials = clientCredentialsRequest.execute(); // pega as credenciais do
																						// cliente

		spotifyApi.setAccessToken(clientCredentials.getAccessToken()); // e configura o token de acesso na api
	}

	/**
	 * Constrói as categorias e play lists
	 */
	private void getCategoriesAndPlayLists() {

		// iniciamos aqui a lista de categorias
		// porque precisamos cercar as chamadas ao servidor
		// com um try/catch
		Category[] categories = null;

		try {
			// Set access token for further "spotifyApi" object usage
			Paging<Category> categoryPage = spotifyApi.getListOfCategories().country(CountryCode.BR)
					.limit(maxCategoryCount).build().execute();
			categories = categoryPage.getItems();

		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
		} catch (SpotifyWebApiException e) {
			e.printStackTrace();
		}

		for (Category category : categories) {
			Genre genre = config.getGenreFactory().buildGenre(category.getName());
			genre = config.getData().saveGenre(genre);
			getPlayLists(category, genre);
		}
	}

	/**
	 * Recupera as playlists desta categoria
	 * 
	 * @param category
	 */
	private void getPlayLists(Category category, Genre genre) {
		GetCategorysPlaylistsRequest playlistreq = null;

		Paging<PlaylistSimplified> playList = null;

		// chama o spotify para pegar a lista de playlists
		try {
			playlistreq = spotifyApi.getCategorysPlaylists(category.getId()).limit(maxListCount).build();
			// às vezes, a playlist chega vazia
			playList = playlistreq.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}

		PlaylistSimplified[] lists = null;

		// às vezes, a playlist retorna vazia. Precisamos passar para a próxima
		if (playList == null) {
			// é porque a lista veio vazia. Não executa a leitura das playlists nessa
			// categoria
		} else {
			lists = playList.getItems();

			for (PlaylistSimplified list : lists) {
				String albumName = list.getName();
				//System.out.println(albumName);

				// na hora de gerar o nome do artista,
				// vamos acrescentar algum sufixo,
				// apenas pra dar alguma dinâmica na hora
				// de visualizar os dados
				// tomamos como exemplo o nome do artista 
				// cuja primeira faixa da playlist é de sua autoria
				
				// adiciona um novo album
				config.addAlbum(albumName, getArtistNameSample(list),genre.getId());
			}
		}

	}
	
	/**
	 * carrega dados do spotify e inicializa um cashback config com o estado inicial de uma loja
	 * @param maxCategoryCount Quantidade máxima de categorias carregadas na chamada
	 * @param maxPlaylistCount Quantidade máxima de playlists buscadas por categoria
	 * @param config O domínio do cachback
	 * @return Recupera as informações de contexto do domínio cashback
	 */
	public static CashbackConfig loadDataFromSpotifier(int maxCategoryCount, int maxPlaylistCount, CashbackConfig config) {
		Spotifier spotifier = null;

		CashbackConfig testConfig = config;

		try {
			spotifier = new Spotifier(testConfig, maxCategoryCount, maxPlaylistCount);
			spotifier.getCategoriesAndPlayLists();

		} catch (SpotifyWebApiException | IOException e) {
			e.printStackTrace();
		}
		
		return testConfig;
	}

	public static void main(String[] args) {
		Spotifier.loadDataFromSpotifier(5,5, CashbackConfig.buildMemmoryDBConfig());
	}

	/**
	 * Recupera o nome do artista que é o compositor da primeira faixa
	 * de uma playlist qualquer
	 * 
	 * @param playList
	 * @return
	 */
	private String getArtistNameSample(PlaylistSimplified playList) {
		// utilize o código abaixo, caso queira também navegar pela estrutura de faixas
		// de cada playlist
		GetPlaylistsTracksRequest tracksRequest = spotifyApi.getPlaylistsTracks(playList.getId()).build();
		PlaylistTrack[] items = null;

		try {
			items = tracksRequest.execute().getItems();
		} catch (SpotifyWebApiException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (items.length > 0 && items[0] != null) {
			Track track = items[0].getTrack();
			ArtistSimplified[] artists = track.getArtists();
			if (artists.length > 0 && artists[0] != null) {
				return artists[0].getName();
			} else {
				// do nothing
			}
		} else {
			// do nothing
		}
		
		return "Random Artist " + (Math.random()*100); // caso o sistema não encontre um artista de exemplo, geramos um nome aleatório
	}
}
