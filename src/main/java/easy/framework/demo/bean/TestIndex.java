package easy.framework.demo.bean;

import easy.framework.elasticsearch.annotation.ESDocument;
import easy.framework.elasticsearch.annotation.ESField;
import easy.framework.elasticsearch.annotation.ElsDefaultValue;
import easy.framework.elasticsearch.annotation.ElsId;
import easy.framework.elasticsearch.metadata.ESFieldType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author: Zhangtong
 * @description:
 * @Date: 2020/2/26.
 */
@Data
@ESDocument(indexName = "test_null_value")
public class TestIndex {
    @ElsId(mutilField = true)
    private Long id;

    @ESField(type = ESFieldType.Text,analyzer = "comma",searchAnalyzer = "comma",fielddata = true)
//    @ElsDefaultValue("key1")
    private String key1;
    @ESField(type = ESFieldType.Keyword)
    @ElsDefaultValue("key2")
    private Date key2;
    @ESField(type = ESFieldType.Keyword)
    @ElsDefaultValue("key3")
    private String key3;
    @ESField
    @ElsDefaultValue(value = "0")
    private Long key4;

    @ESField(type = ESFieldType.Keyword)
    private BigDecimal bd;
    @ESField(type=ESFieldType.Text)
    private BigDecimal bd2;
    @ESField(type= ESFieldType.Double)
    private BigDecimal dou;

}
