package scc.data;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.cosmos.CosmosClient;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.util.SearchPagedIterable;
import com.azure.search.documents.util.SearchPagedResponse;
import org.apache.commons.pool2.proxy.CglibProxySource;
import scc.cache.RedisCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CognitiveSearch {
    public static final String SEARCHSERVICEQUERYKEY = "736DB28373B6849B763051590ABEA67A";
    public static final String INDEXNAME = "cosmosdb-index";
    public static final String SEARCHSERVICE_URL = "https://scc2021rv.search.windows.net/";

    private static CognitiveSearch instance;
    private static SearchClient searchClient;

    public static synchronized CognitiveSearch getInstance() {
        if( instance != null)
            return instance;


        searchClient = new SearchClientBuilder()
                .credential(new AzureKeyCredential(SEARCHSERVICEQUERYKEY))
                .endpoint(SEARCHSERVICE_URL)
                .indexName(INDEXNAME)
                .buildClient();

        instance = new CognitiveSearch(searchClient);
        return instance;
    }

    public CognitiveSearch(SearchClient searchClient) {
        this.searchClient = searchClient;
    }


    public List<String> findMessagesWithWordInChannel(String queryText, String idChannel){
        SearchOptions options = new SearchOptions().setIncludeTotalCount(true).setFilter("channelId eq '"+ idChannel+"'")
                .setSelect("id", "user", "text").setSearchFields("text").setTop(5);

        SearchPagedIterable searchPagedIterable = searchClient.search(queryText, options, null);
        List<String> result = new ArrayList<>();
        for (SearchPagedResponse resultResponse : searchPagedIterable.iterableByPage()) {
            resultResponse.getValue().forEach(searchResult -> {
                for (Map.Entry<String, Object> res : searchResult.getDocument(SearchDocument.class).entrySet()) {
                    result.add(res.getKey()+" -> "+ res.getValue());
                    //System.out.printf("%s -> %s\n", res.getKey(), res.getValue());
                }
            });
        }
        return result;
    }

}//ultimo


