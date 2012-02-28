package cz.payola.data

import scala.collection.mutable
import scala.io.Source
import java.util.Properties
import util.control.Exception

class VirtuosoDataSource extends sparql.providers.SingleSourceRdfDataProvider {
    val request : String = "{protocol}://{host}/sparql?{defaultUri}&{namedUri}&{query}&{format}&save=display";
    val protocol : String = "http";
    var host : String = "";
    val defaultUri : String = "default-graph-uri=";
    val namedUri : String = "named-graph-uri=";
    val queryFormat : String = "query={query}";
    val format : String = "format=application%2Frdf%2Bxml"

    override def executeSparqlQuery(sparqlQuery: String): String = {
        val result = new StringBuilder();

        // Query is composed and URL Coded
        val request = composeQueryRequest(sparqlQuery);

        // Read query result
        val source = Source.fromURL(request);
        source.foreach(char => result.append(char));

        // Return query result
        return result.toString();
    }
    
    private def composeQueryRequest (query: String) : String = {
        val q : String =
            queryFormat.replaceAllLiterally(
                "{query}",
                java.net.URLEncoder.encode(query, "UTF-8")
        );
        
        return request.replaceAllLiterally("{protocol}", protocol)
            .replaceAllLiterally("{host}", getHost())
                .replaceAllLiterally("{defaultUri}", defaultUri)
                    .replaceAllLiterally("{namedUri}", namedUri)
                        .replaceAllLiterally("{query}", q)
                            .replaceAllLiterally("{format}", format);
    }

    private def getHost() : String = {
        // Already initialized
        if (host != null && host.size > 0)
            return host;

        // Read ini file with properties
        val prop : Properties = new Properties();
        prop.load(getClass.getResource("/virtuoso.ini").openStream());

        val h : String = prop.getProperty("host");
        val p : String = prop.getProperty("port");

        // Host is composed from ini file values (port may be undefined)
        if (p != null && p.size > 0)
            host = h + ":" + p;
        else
            host = h;

        return host;
    }
}