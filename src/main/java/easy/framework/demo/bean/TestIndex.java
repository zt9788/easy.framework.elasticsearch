package easy.framework.demo.bean;

import easy.framework.elasticsearch.annotation.ElsDefaultValue;
import easy.framework.elasticsearch.annotation.ElsId;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author: Zhangtong
 * @description:
 * @Date: 2020/2/26.
 */
@Data
@Document(indexName = "test_null_value")
public class TestIndex {
    @ElsId(mutilField = true)
    private Long id;

    @Field(type = FieldType.Text,analyzer = "comma",searchAnalyzer = "comma",fielddata = true)
//    @ElsDefaultValue("key1")
    private String key1;
    @Field(type = FieldType.Keyword)
    @ElsDefaultValue("key2")
    private Date key2;
    @Field(type = FieldType.Keyword)
    @ElsDefaultValue("key3")
    private String key3;
    @Field
    @ElsDefaultValue(value = "0")
    private Long key4;

    @Field(type = FieldType.Keyword)
    private BigDecimal bd;
    @Field(type=FieldType.Text)
    private BigDecimal bd2;
    @Field(type= FieldType.Double)
    private BigDecimal dou;

}
