package ava.io.authentication_manager.config;

import ava.io.authentication_manager.utils.RestTemplateResponseErrorHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;


@Configuration
public class RestTemplateConfig {

    @Value("${http_source.cert-path}")
    String certPath;

    @Value("${http_source.cert-pass}")
    String password;

    @Bean
    @Qualifier("notSecureRestTemplate")
    public RestTemplate getRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.errorHandler(new RestTemplateResponseErrorHandler())
                .build();
    }

    @Bean
    public ObjectMapper getMapper(){return new ObjectMapper();}
//    @Bean("SecureRestTemplate")
//    public RestTemplate getSecureRestTemplate() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
//
//        SSLContext sslContext = null;
//        try {
//            sslContext = new SSLContextBuilder()
//                    .loadTrustMaterial(new File(certPath), password.toCharArray()).build();
//            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext);
//            CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build();
//            ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
//            return new RestTemplate(requestFactory);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return  null;
//
//
//    }
}
