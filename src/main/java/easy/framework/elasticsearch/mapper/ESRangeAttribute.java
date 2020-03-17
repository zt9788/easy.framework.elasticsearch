package easy.framework.elasticsearch.mapper;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author: Zhangtong
 * @description:
 * @Date: 2020/3/2.
 */
@Data
@AllArgsConstructor
public class ESRangeAttribute {
    private Object value;
    private boolean left;
    private boolean eq;
}
