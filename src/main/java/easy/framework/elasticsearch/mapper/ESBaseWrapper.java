package easy.framework.elasticsearch.mapper;

import lombok.Data;

/**
 * @author: Zhangtong
 * @description:
 * @Date: 2020/3/2.
 */
@Data
public abstract class ESBaseWrapper<T> {
    protected ESWrappers<T> esWrappers;
    private ESBaseWrapper(){

    }
    public ESBaseWrapper(ESWrappers<T> esWrappers){
        this();
        this.esWrappers = esWrappers;
    }
    public enum QueryType{
        LT,
        LTE,
        GT,
        GTE,
        MATCH,
        LIKE,
        OR,
        AND

    }
}
