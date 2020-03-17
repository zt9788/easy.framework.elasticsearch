package easy.framework.elasticsearch.metadata;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author: Zhangtong
 * @description:
 * @Date: 2020/2/23.
 */
@Data
public class ESPage<T> implements Serializable,Cloneable {
    private int current;
    private int size;
    private long total;
    private int errCode;
    private String scrollId;
    private String msg;
    private List<T> data;
}
