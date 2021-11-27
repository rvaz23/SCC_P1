package scc.data;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.util.SearchPagedIterable;
import com.azure.search.documents.util.SearchPagedResponse;
import org.apache.commons.pool2.proxy.CglibProxySource;
import scc.cache.RedisCache;

import java.util.Map;

public class CognitiveSearch {
    public static final String SEARCHSERVICEQUERYKEY = "search-azurekeys-westeurope.props";
    public static final String INDEXNAME = "SearchServiceQueryKey";
    public static final String SEARCHSERVICE_URL = "SearchServiceName";

    private static SearchClient searchClient;

    public CognitiveSearch(){

       searchClient = new SearchClientBuilder()
                .credential(new AzureKeyCredential(SEARCHSERVICEQUERYKEY))
                .endpoint(SEARCHSERVICE_URL)
                .indexName(INDEXNAME)
                .buildClient();
    }






    public void findWordsInMessages(String queryText){

        SearchOptions options = new SearchOptions().setIncludeTotalCount(true).setTop(5);

        SearchPagedIterable searchPagedIterable = searchClient.search(queryText, options, null);

        for(SearchPagedResponse resultResponse : searchPagedIterable.iterableByPage()) {
            resultResponse.getValue().forEach(searchResult -> {
                for (Map.Entry<String, Object> res : searchResult.getDocument(SearchDocument.class).entrySet()) {

                    //System.out.printf("%s -> %s\n", res.getKey(), res.getValue());
                }
            });
        }
    }


    public void findWordsInMessages2(String queryText){
        SearchOptions options = new SearchOptions().setIncludeTotalCount(true).setFilter("user eq 'Gardner.Labadie'")
                .setSelect("id", "user", "text").setSearchFields("text").setTop(5);

        SearchPagedIterable searchPagedIterable = searchClient.search(queryText, options, null);
        System.out.println("Number of results : " + searchPagedIterable.getTotalCount());

        for (SearchPagedResponse resultResponse : searchPagedIterable.iterableByPage()) {
            resultResponse.getValue().forEach(searchResult -> {
                for (Map.Entry<String, Object> res : searchResult.getDocument(SearchDocument.class).entrySet()) {
                    //System.out.printf("%s -> %s\n", res.getKey(), res.getValue());
                }
            });
        }
    }

}


