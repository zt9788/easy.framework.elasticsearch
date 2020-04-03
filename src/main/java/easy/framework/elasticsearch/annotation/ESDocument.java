package easy.framework.elasticsearch.annotation;

import org.elasticsearch.index.VersionType;

import java.lang.annotation.*;

/**
 * @author: Zhangtong
 * @description:
 * @Date: 2020/4/3.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ESDocument {
    String indexName();

    String type() default "";

    boolean useServerConfiguration() default false;

    short shards() default 5;

    short replicas() default 1;

    String refreshInterval() default "1s";

    String indexStoreType() default "fs";

    boolean createIndex() default true;

    VersionType versionType() default VersionType.EXTERNAL;
}
