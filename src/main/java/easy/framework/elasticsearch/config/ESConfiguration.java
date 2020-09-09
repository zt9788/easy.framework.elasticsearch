package easy.framework.elasticsearch.config;

import easy.framework.elasticsearch.config.ESProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Zhangtong
 * @description: 参考 https://segmentfault.com/a/1190000022102940/
 * @Date: 2020/2/23.
 */
@Slf4j
@Data
@Configuration
public class ESConfiguration {

    @Resource
    ESProperties esProperties;

    @Bean
    public RestHighLevelClient getRestHighLevelClient() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(esProperties.getUsername(), esProperties.getPassword()));
        SSLContext sslContext = null;
        KeyStore keyStore = null;
        if(StringUtils.isNotBlank(esProperties.getCertificatesType())) {
            keyStore = getKeyStoreByType(esProperties.getCertificatesType());
            SSLContextBuilder sslContextBuilder = SSLContexts.custom().loadTrustMaterial(keyStore, (x509Certificates, s) -> true);
            sslContext = sslContextBuilder.build();

        }
        HostnameVerifier allHostsValid = (String hostname, SSLSession session) -> {
            return true;
        };
        String[] uri = esProperties.getUris().split(",");
        List<Node> list = new ArrayList<>();
        for (String url:uri){
            URI netUri = URI.create(url);
            HttpHost httpHost = new HttpHost(netUri.getHost(),netUri.getPort(),netUri.getScheme());
            Node node = new Node(httpHost);
            list.add(node);

        }
        if(list.size() > 0) {
            Node[] node = new Node[list.size()];
            node = list.toArray(node);
            SSLContext finalSslContext = sslContext;
            RestClientBuilder builder = RestClient.builder(node).setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                @Override
                public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
                    //客户端SSL
                    if(finalSslContext != null) {
                        httpAsyncClientBuilder.setSSLContext(finalSslContext);
                    }
                    httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    httpAsyncClientBuilder.setSSLHostnameVerifier(allHostsValid);
                    return httpAsyncClientBuilder.setDefaultIOReactorConfig(IOReactorConfig.DEFAULT);

                }
            });
            RestHighLevelClient client = new RestHighLevelClient(builder);
            return client;
        }
        return null;
    }

    private KeyStore getKeyStoreByType(String type) throws KeyStoreException, CertificateException, NoSuchAlgorithmException {
        KeyStore keyStore = null;
        //TODO need change it
        if (!"pem".equalsIgnoreCase(type)){
            keyStore = KeyStore.getInstance("pkcs12");
            try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(esProperties.getPkcsClientFilePath())) {
                keyStore.load(is, "".toCharArray());
            } catch (IOException e) {
                log.error("",e);
            } catch (CertificateException e) {
                log.error("",e);
            } catch (NoSuchAlgorithmException e) {
                log.error("",e);
            }
        } else {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            Certificate trustedCa;
            try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(esProperties.getPemFilePath())) {
                trustedCa = factory.generateCertificate(is);
                keyStore = KeyStore.getInstance("pkcs12");
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", trustedCa);
            } catch (IOException e) {
                log.error("",e);
            }
        }
        return keyStore;
    }
}
