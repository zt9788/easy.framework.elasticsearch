package easy.framework.elasticsearch.mapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author: Zhangtong
 * @description: key attribute
 * @Date: 2020/2/21.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class KeyAttribute {
    private String key;
    private Object value;
//    private Boolean isKeyword = false;
}
