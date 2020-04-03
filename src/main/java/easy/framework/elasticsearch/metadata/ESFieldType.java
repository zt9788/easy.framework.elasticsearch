package easy.framework.elasticsearch.metadata;

/**
 * @author: Zhangtong
 * @description:
 * @Date: 2020/4/3.
 */
public enum ESFieldType {
    Text,
    Byte,
    Short,
    Integer,
    Long,
    Date,
    Half_Float,
    Float,
    Double,
    Boolean,
    Object,
    Auto,
    Nested,
    Ip,
    Attachment,
    Keyword;

    private ESFieldType() {
    }
}
