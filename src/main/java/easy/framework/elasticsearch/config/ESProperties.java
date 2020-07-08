package easy.framework.elasticsearch.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author: Zhangtong
 * @description:
 * @Date: 2020/2/25.
 */
@Data
@Slf4j
@Component
public class ESProperties {
    @Value("${spring.elasticsearch.rest.uris}")
    private String uris;
    @Value("${spring.elasticsearch.index.max_result_window}")
    private Long maxResultWindow;

    private String certificatesType;
    @Value("${spring.elasticsearch.username}")
    private String username;
    @Value("${spring.elasticsearch.password}")
    private String password;
    private String pkcsClientFilePath;
    private String pemFilePath;
    private boolean isOpenXPack;
}
