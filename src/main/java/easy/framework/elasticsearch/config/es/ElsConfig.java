package easy.framework.elasticsearch.config.es;

import lombok.Data;


import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author: Zhangtong
 * @description:
 * @Date: 2020/2/19.
 */
@Data
//@Component
@Configuration
public class ElsConfig{// extends ElasticsearchConfigurationSupport {
    //@Value("${springelasticsearch.rest.uris}")

    private String profile = "_test";
//    @Bean(name = {"elasticsearchOperations", "elasticsearchTemplate"})
//    public ElasticsearchTemplate getBean2() throws UnknownHostException {
//        return new ElasticsearchTemplate(elasticsearchClient());
//    }
//    @Value("{spring.elasticsearch.config.connect-timeout}")
//    private Integer connectTimeout = 1000;
//    @Value("{spring.elasticsearch.config.socket-timeout}")
//    private Integer socketTimeout = 30000;
//    @Value("{spring.elasticsearch.config.request-timeout}")
//    private Integer connRequestTimeout = 500;
//    @Value("{spring.elasticsearch.config.max-conn-per-route}")
//    private Integer maxConnPerRoute = 10;
//    @Value("{spring.elasticsearch.config.max-conn-total}")
//    private Integer maxConnTotal = 30;
    @Resource
    RestHighLevelClient client;
//
////    @Bean
////    public ElasticsearchTemplate getBean2(){
////        return new ElasticsearchTemplate()
////    }
//
//    @Bean
//    public ElasticsearchRestTemplate getBean(){
//        ElasticsearchRestTemplate t;
//        ElasticsearchOperations eo;
//        return new ElasticsearchRestTemplate(client);
//    }
//
////    @Override
////    public RestHighLevelClient elasticsearchClient() {
////        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
////                .connectedTo("183.36.121.205:9200")
////                .build();
////
////        return RestClients.create(clientConfiguration).rest();
////    }
//
////    @SneakyThrows
////    @Bean
//    public Client elasticsearchClient() throws UnknownHostException {
//        Settings settings = Settings.builder().put("cluster.name", "elasticsearch").build();
//        TransportClient client = new PreBuiltTransportClient(settings);
//        client.addTransportAddress(new TransportAddress(InetAddress.getByName("183.36.121.205"), 9200));
//        return client;
//    }
//
////    @Bean(name = {"elasticsearchOperations", "elasticsearchTemplate"})
//    public ElasticsearchTemplate elasticsearchTemplate() throws UnknownHostException {
//        return new ElasticsearchTemplate(elasticsearchClient(), entityMapper());
//    }
//
//    // use the ElasticsearchEntityMapper
////    @Bean
//    public EntityMapper entityMapper() {
//        ElasticsearchConfigurationSupport sp;
//
//        ElasticsearchEntityMapper entityMapper = new ElasticsearchEntityMapper(elasticsearchMappingContext(),
//                new DefaultConversionService());
//        entityMapper.setConversions(elasticsearchCustomConversions());
//        return entityMapper;
//    }
}
