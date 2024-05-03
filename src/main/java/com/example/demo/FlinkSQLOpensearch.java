package com.example.demo;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;

import javax.naming.Context;
import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.security.KeyStore;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FlinkSQLOpensearch {
    public static void main(String[] args) throws Exception {

        Logger.getLogger("org.apache.flink").setLevel(Level.WARNING);

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        EnvironmentSettings settings = EnvironmentSettings.newInstance().inStreamingMode().build();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env, settings);

        // Define and register custom source
        DataStream<Row> dataStream = env.addSource(new OpenSearchSource());

        // Convert the data stream into a table
        Table dataTable = tableEnv.fromDataStream(dataStream);

        // Register the table in the catalog
        tableEnv.createTemporaryView("OpenSearchTable", dataTable);

        // Run SQL query
        Table result = tableEnv.sqlQuery("SELECT * FROM OpenSearchTable");

        DataStream<Row> resultStream = tableEnv.toAppendStream(result, Row.class);
        resultStream.print();
        // Print the result table
//        tableEnv.toAppendStream(result, Row.class).print();
        DataStreamSink<Row> rowDataStreamSink = tableEnv.toAppendStream(result, Row.class)
                .addSink(new TabularSink());
        // Execute the Flink job
        env.execute("OpenSearch SQL Job");
    }

    public static class TabularSink extends RichSinkFunction<Row> {
        private boolean headerPrinted = false;
        private long count = 0;

        @Override
        public void open(org.apache.flink.configuration.Configuration parameters) throws Exception {
            // Print header when the sink is opened
            System.out.printf("%10s %15s %20s %30s %-5s\n", "ID", "Time", "Server", "Title", "Title_url");
            System.out.println("------------------------------------------------------------------------------------------------------------------------");
            headerPrinted = true;
        }

        @Override
        public void invoke(Row value, Context context) throws Exception {
            count++;
            Row row1 = (Row) value.getFieldAs(0);
            // Print row data
            System.out.printf("%10s %15s %20s %30s %-5s\n", row1.getField(0), row1.getField(1), row1.getField(2), row1.getField(3), row1.getField(4));
        }
    }

    // Custom source to stream data from OpenSearch
    public static class OpenSearchSource implements SourceFunction<Row> {
        private volatile boolean isRunning = true;

        @Override
        public void run(SourceContext<Row> ctx) throws Exception {
            System.out.println("ctx");
            try{
                String java_path = System.getenv("JAVA_HOME");

                FileInputStream is = new FileInputStream(java_path+"/lib/security/cacerts");
                KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                String password = "changeit";
                ks.load(is, password.toCharArray());
                SSLContext sslContext = SSLContextBuilder.create()
                        .loadKeyMaterial(ks, password.toCharArray())
                        .loadTrustMaterial(new TrustSelfSignedStrategy())
                        .build();
                String connString = "https://admin:myPass2403@localhost:9200";
                URI connUri = URI.create(connString);

                String userInfo = connUri.getUserInfo();
                String[] auth = userInfo.split(":");

                CredentialsProvider cp = new BasicCredentialsProvider();
                cp.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(auth[0], auth[1]));

                RestHighLevelClient client = new RestHighLevelClient(
                        RestClient.builder(new HttpHost(connUri.getHost(), connUri.getPort(), connUri.getScheme()))
                                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                                        .setSSLContext(sslContext)
                                        .setDefaultCredentialsProvider(cp)));
                SearchRequest searchRequest = new SearchRequest("heya2_index");
                searchRequest.source().query(QueryBuilders.matchAllQuery());

                SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
                for (SearchHit hit : searchResponse.getHits().getHits()) {
                    String id = hit.getId();
                    int time = Integer.parseInt(hit.getSourceAsMap().get("timestamp").toString());
                    String server_url = hit.getSourceAsMap().get("server_url").toString();
                    String title = hit.getSourceAsMap().get("title").toString();
                    String title_url = hit.getSourceAsMap().get("title_url").toString();
                    Row row = Row.of(id, time, server_url, title, title_url);
                    ctx.collect(row);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void cancel() {
            isRunning = false;
        }
    }
}
