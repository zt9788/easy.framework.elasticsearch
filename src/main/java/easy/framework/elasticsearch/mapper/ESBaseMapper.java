package easy.framework.elasticsearch.mapper;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import easy.framework.elasticsearch.annotation.ESDocument;
import easy.framework.elasticsearch.annotation.ESField;
import easy.framework.elasticsearch.annotation.ElsHighlightField;
import easy.framework.elasticsearch.config.ESProperties;
import easy.framework.elasticsearch.metadata.ESFieldType;
import easy.framework.elasticsearch.metadata.ESPage;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author: Zhangtong
 * @description: elasticsearch crud
 * @Date: 2020/2/20.
 */
@Data
@Slf4j
@SuppressWarnings(value = {"rawstype","unused"})
public abstract class ESBaseMapper<T> {

    @Autowired(required = false)
    protected RestHighLevelClient restHighLevelClient;

    @Autowired(required = false)
    protected ESProperties esProperties;

    protected Class<T> getTClass() {
        Type genericSuperclass = this.getClass().getGenericSuperclass();
        return (Class<T>) ((ParameterizedType )genericSuperclass).getActualTypeArguments()[0];
    }

    public T selectById(Serializable id) throws Exception {
        if(id == null || StringUtils.isBlank(id+""))
            throw new NullPointerException("id is null");
        GetRequest getRequest = new GetRequest(this.getIndexName(),id+"");
        GetResponse response = restHighLevelClient.get(getRequest,RequestOptions.DEFAULT);
        log.debug(response.toString());
        if(response.isExists()){
            T o = JSONObject.parseObject(JSONObject.toJSONString(response.getSource()),this.getTClass());
            return o;
        }
        return null;
    }

    public ESPage<T> selectBySql(String sql){

        return null;
    }
    public ESPage<T> selectByEntity(T entity) throws Exception {
        ESWrappers<T> entityWrapper = ESWrappers.<T>build();
        ESWrapperBuilder.build(entityWrapper,entity,false);
        return selectList(entityWrapper);
    }
    public ESPage<T> selectList(QueryBuilder queryBuilder){
        ESPage<T> esPage = new ESPage<>();
        esPage.setCurrent(1);
        esPage.setTotal(0L);
        esPage.setSize(10);
        SearchRequest searchRequest = new SearchRequest(this.getIndexName());
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        searchSourceBuilder.from((requestVO.getPage()-1)*size);
//        searchSourceBuilder.size(size);
        RestHighLevelClient rhlClient = restHighLevelClient;
        searchSourceBuilder.query(queryBuilder);
        searchRequest.source(searchSourceBuilder);
        log.info(searchRequest.toString());
        try {
            log.info(searchRequest.toString());
            SearchResponse searchResponse = rhlClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits responseHits = searchResponse.getHits();
            SearchHit[] hits = searchResponse.getHits().getHits();
            List<T> list = new ArrayList<>();
            for (SearchHit hit : hits) {
                Map<String, Object> mapsource = hit.getSourceAsMap();
                Map<String, HighlightField> map = hit.getHighlightFields();
                T o = JSONObject.parseObject(JSONObject.toJSONString(mapsource),this.getTClass());
                list.add(o);
            }
            log.info(JSONObject.toJSONString(list));
            esPage.setData(list);
            esPage.setTotal(responseHits.getTotalHits().value);
            esPage.setSize(list.size());
            return esPage;
//            return list;
        } catch (IOException e) {
            log.error("els query error",e);
        }finally {
            // factory.close();

        }
        return esPage;
    }
    public boolean cleanScroll(String scrollId) throws IOException {
        ClearScrollRequest clearScrollRequest = new ClearScrollRequest(); //完成滚动后，清除滚动上下文
        clearScrollRequest.addScrollId(scrollId);
        ClearScrollResponse clearScrollResponse = restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
        boolean succeeded = clearScrollResponse.isSucceeded();
        return succeeded;
    }
    public <T> T selectOne(ESWrappers<T> wrappers) throws Exception {
        ESPage<T> page = selectList(wrappers);
        if(page.getData() != null && page.getData().size()>0){
            return page.getData().get(0);
        }else{
            return null;
        }
    }
    public <T> ESPage<T> selectList(
            ESWrappers<T> wrappers
            ) throws Exception {
        int size = wrappers.getSize();
        if(size == 0) size = 10;
        ESPage<T> esPage = new ESPage<>();
        esPage.setCurrent(wrappers.getPage());
        esPage.setTotal(0L);
        esPage.setSize(size);
        if(!wrappers.isUseScroll() && !wrappers.isUseSearchAfter()) {
            if (wrappers.getPage() * size > esProperties.getMaxResultWindow()) {
                throw new Exception("当前分页数量:" + wrappers.getPage() * size + ",大于系统最大默认值:" + esProperties.getMaxResultWindow());
            }
        }
        SearchRequest searchRequest = new SearchRequest(this.getIndexName());
        if(wrappers.isUseScroll()){
            searchRequest.scroll(wrappers.getTimeValue());
        }
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        if(!wrappers.isUseSearchAfter())
            searchSourceBuilder.from((wrappers.getPage()-1)*size);
        else {
            if(wrappers.getSearchAfterValue() != null && wrappers.getSearchAfterValue().length > 0)
                searchSourceBuilder.searchAfter(wrappers.getSearchAfterValue());
        }
        searchSourceBuilder.size(size);

        searchSourceBuilder.trackTotalHits(true);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        RestHighLevelClient rhlClient = restHighLevelClient;
        Field[] fs = this.getTClass().getDeclaredFields();
        makeQuery(boolQueryBuilder,wrappers);
        searchSourceBuilder.query(boolQueryBuilder);
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        for(Field field:fs){
            if(!field.isAnnotationPresent(ElsHighlightField.class)){
                continue;
            }
            HighlightBuilder.Field highlightTitle =  new HighlightBuilder.Field(field.getName());
            //TODO
            ElsHighlightField highlightField = field.getAnnotation(ElsHighlightField.class);
            if(highlightField.isAuto()) {
                highlightTitle.highlighterType("unified");
                highlightBuilder.field(highlightTitle);
                highlightBuilder.preTags("<font color='#D0021B'>").postTags("</font>");
            }else{
                highlightTitle.highlighterType(highlightField.value());
                highlightBuilder.field(highlightTitle);
                highlightBuilder.preTags(highlightField.preTags()).postTags(highlightField.postTags());
            }
        }
        searchSourceBuilder.highlighter(highlightBuilder);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        if(wrappers.getOrder().size() > 0){
            wrappers.getOrder().forEach((k,v)->{
                Field f = null;
                try {
                    f = getTClass().getDeclaredField(k);
                } catch (NoSuchFieldException e) {
                    log.error("",e);
                }
                if(f == null)
                    throw new NullPointerException("order 不在类中");
                ESField sf = f.getAnnotation(ESField.class);
                String type;
                if(sf == null){
                    type = MappingBuilder.getElasticSearchMappingType(f.getType().getSimpleName().toLowerCase());
                }else{
                    type = sf.type().toString().toLowerCase();
                    if(sf.type() == ESFieldType.Text)
                        type = "text";//MappingBuilder.getElasticSearchMappingType(f.getType().getSimpleName().toLowerCase());
                }
                String order = k;
                if(type.equals("text")){
                    if(sf == null || !sf.fielddata())
                        order = order+".keyword";
                }
                SortBuilder sortBuilder = SortBuilders.fieldSort(order).order(v);
                searchSourceBuilder.sort(sortBuilder);
            });
        }else {
            searchSourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC));
        }
        searchRequest.source(searchSourceBuilder);
        log.info(searchRequest.toString());
        try {
            SearchResponse searchResponse;
            if(wrappers.isUseScroll() && !StringUtils.isNotBlank(wrappers.getScrollId())) {
                SearchScrollRequest searchScrollRequest = new SearchScrollRequest();
                searchScrollRequest.scrollId(wrappers.getScrollId());
                searchScrollRequest.scroll(wrappers.getTimeValue());
                searchResponse = rhlClient.scroll(searchScrollRequest, RequestOptions.DEFAULT);
                esPage.setScrollId(searchResponse.getScrollId());
            }else {
                searchResponse = rhlClient.search(searchRequest, RequestOptions.DEFAULT);
            }
            SearchHits responseHits = searchResponse.getHits();
            SearchHit[] hits = searchResponse.getHits().getHits();
            List<T> list = new ArrayList<>();
            for (SearchHit hit : hits) {
                Map<String, Object> mapsource = hit.getSourceAsMap();
                Map<String, HighlightField> map = hit.getHighlightFields();
                if (wrappers.isHighlight()) {
                    for (Map.Entry<String, HighlightField> entry : map.entrySet()) {
                        StringBuilder sb = new StringBuilder();
                        Text[] text = entry.getValue().fragments();
                        for (Text str : text) {
                            sb.append(str.string());
                        }
                        mapsource.put(entry.getKey(), sb.toString());
                    }
                }
                Object[] value = hit.getSortValues();
                esPage.setAfterValue(value);
                T o = (T) JSONObject.parseObject(JSONObject.toJSONString(mapsource),this.getTClass());
                list.add(o);
            }
            log.info(JSONObject.toJSONString(list));
            esPage.setData(list);
            esPage.setTotal(responseHits.getTotalHits().value);
            esPage.setSize(list.size());
            return esPage;
//            return list;
        } catch (IOException e) {
            log.error("els query error",e);
        }finally {
           // factory.close();

        }
        return esPage;
//        return new ArrayList<>();
    }
    private QueryBuilder makeQuery(BoolQueryBuilder boolQueryBuilder,ESWrappers wrappers){
        //in,eq
        makeInQuery(wrappers,wrappers.and().getIn(),boolQueryBuilder, true,ESBaseWrapper.QueryType.MATCH);
        makeInQuery(wrappers,wrappers.or().getIn(),boolQueryBuilder, false,ESBaseWrapper.QueryType.MATCH);
        //like
        makeInQuery(wrappers,wrappers.and().getLike(),boolQueryBuilder, true,ESBaseWrapper.QueryType.LIKE);
        makeInQuery(wrappers,wrappers.or().getLike(),boolQueryBuilder, false,ESBaseWrapper.QueryType.LIKE);
        //< <= > >=
        makeAndRangeQuery(wrappers,wrappers.and().getRange(),boolQueryBuilder);//,field);
        makeOrRangeQuery(wrappers.or().getRange(),boolQueryBuilder);//,field);
        //is null
        makeIsNull(boolQueryBuilder,wrappers);
        //is not null
        makeNotNull(boolQueryBuilder,wrappers);
        if(wrappers.getSubCondition() != null && wrappers.getSubCondition().size() > 0) {
            List<ESWrappers<T>> list = wrappers.getSubCondition();
            list.forEach(item -> {
                BoolQueryBuilder subQueryButilder = QueryBuilders.boolQuery();
                makeQuery(subQueryButilder,item);
                boolQueryBuilder.must(subQueryButilder);
            });
        }
        return boolQueryBuilder;
    }
    private QueryBuilder makeNotNull(BoolQueryBuilder boolQueryBuilder,ESWrappers<T> wrappers){
        if(wrappers.getOr().getIsNotNull() != null && wrappers.getOr().getIsNotNull().size() > 0){
            for(String key:wrappers.getOr().getIsNotNull()) {
                ExistsQueryBuilder orNotIn = QueryBuilders.existsQuery(key);
                boolQueryBuilder.should(orNotIn);
            }
        }
        if(wrappers.getAnd().getIsNotNull() != null && wrappers.getAnd().getIsNotNull().size() > 0){
            for(String key:wrappers.getAnd().getIsNotNull()) {
                ExistsQueryBuilder orNotIn = QueryBuilders.existsQuery(key);
//                boolQueryBuilder.must(orNotIn);
                filterOrMust(wrappers,boolQueryBuilder,orNotIn);
            }
        }
        return boolQueryBuilder;
    }
    private QueryBuilder makeIsNull(BoolQueryBuilder boolQueryBuilder,ESWrappers<T> wrappers){
        if(wrappers.getAnd().getIsNull() != null && wrappers.getAnd().getIsNull().size() > 0){
            BoolQueryBuilder bb = QueryBuilders.boolQuery();
            for(String key:wrappers.getOr().getIsNull()) {
                ExistsQueryBuilder orNotIn = QueryBuilders.existsQuery(key);
                boolQueryBuilder.mustNot(orNotIn);
            }
            filterOrMust(wrappers,boolQueryBuilder,bb);
        }
//        return boolQueryBuilder;
        if(wrappers.getOr().getIsNull() != null && wrappers.getOr().getIsNull().size() > 0){
            BoolQueryBuilder bb = QueryBuilders.boolQuery();
            for(String key:wrappers.getOr().getIsNull()) {
                ExistsQueryBuilder orNotIn = QueryBuilders.existsQuery(key);
                bb.mustNot(orNotIn);
            }
            boolQueryBuilder.should(bb);
        }
        return boolQueryBuilder;
    }
    @Deprecated
    private QueryBuilder makeAndOr(BoolQueryBuilder boolQueryBuilder,List<? extends ESQueryWrapper<? extends T>> list,ESWrappers<? extends T> wrappers){
//        List<? extends ESQueryWrapper<? extends T>> list = wrappers.getAndOr();
        list.forEach(item->{
            BoolQueryBuilder subQueryButilder = QueryBuilders.boolQuery();
//            makeInQuery(wrappers,wrappers.getAnd(),null,subQueryButilder,true, ESBaseWrapper.QueryType.MATCH);
            makeInQuery(wrappers,wrappers.or().getIn(),subQueryButilder, false,ESBaseWrapper.QueryType.MATCH);
//            makeAndOr(subQueryButilder,)
            boolQueryBuilder.must(subQueryButilder);
        });

        return boolQueryBuilder;
    }
    private QueryBuilder filterOrMust(ESWrappers<? extends T> wrappers,BoolQueryBuilder boolQueryBuilder,QueryBuilder queryBuilder){
        if(wrappers.isUseFilter())
            boolQueryBuilder.filter(queryBuilder);
        else
            boolQueryBuilder.must(queryBuilder);
        return boolQueryBuilder;
    }
    private QueryBuilder makeAndRangeQuery(ESWrappers<? extends T> wrappers,
                                           Map<String,List<ESRangeAttribute>> map,
                                           BoolQueryBuilder boolQueryBuilder
//                                         ,Field field
                                            ){
        if(map == null)
            return boolQueryBuilder;
        map.forEach((k,item)->{
//            if(!field.getName().equals(k))
//                return;
            RangeQueryBuilder queryBuilder = QueryBuilders.rangeQuery(k);
            item.forEach(v->{
                rangeMaker(queryBuilder,v);
            });
//            boolQueryBuilder.must(queryBuilder);
            filterOrMust(wrappers,boolQueryBuilder,queryBuilder);
        });
        return boolQueryBuilder;
    }
    private QueryBuilder rangeMaker(RangeQueryBuilder queryBuilder,ESRangeAttribute v){
        if(v.isLeft() && v.isEq()){
            queryBuilder.lte(v.getValue());
        }else if(v.isLeft() && !v.isEq()){
            queryBuilder.lt(v.getValue());
        }else if(!v.isLeft() && v.isEq()){
            queryBuilder.gte(v.getValue());
        }else if(!v.isLeft() && !v.isEq()){
            queryBuilder.gt(v.getValue());
        }
        return queryBuilder;
    }
    private QueryBuilder makeOrRangeQuery(Map<String,List<ESRangeAttribute>> map,
                                          BoolQueryBuilder boolQueryBuilder
                                          //,Field field
                                        ){
        if(map == null)
            return boolQueryBuilder;
        map.forEach((k,item)->{
//            if(!field.getName().equals(k))
//                return;
            item.forEach(v->{
                RangeQueryBuilder queryBuilder = QueryBuilders.rangeQuery(k);
                rangeMaker(queryBuilder,v);
                boolQueryBuilder.should(queryBuilder);
            });

        });
        return boolQueryBuilder;
    }
    private QueryBuilder makeInQuery(
                                    ESWrappers<? extends T> wrappers,
                                    Map<String,List<Object>> map,
                                     //Field field,
                                     BoolQueryBuilder boolQueryBuilder,
                                     boolean isAnd,
                                     ESBaseWrapper.QueryType type){
//        org.springframework.data.elasticsearch.annotations.Field f = field.getAnnotation(org.springframework.data.elasticsearch.annotations.Field.class);
        if(map == null)
            return boolQueryBuilder;
        map.forEach((k,v)->{
            String sname = k;
//            if(field.getName().equals(k)){
//                String sname = field.getName();
                if(isAnd && type != ESBaseWrapper.QueryType.LIKE) {
                    if(v.size() > 1) {
                        QueryBuilder queryBuilder = QueryBuilders.termsQuery(sname, v);
//                        boolQueryBuilder.must(queryBuilder);
                        filterOrMust(wrappers,boolQueryBuilder,queryBuilder);
                    }else if(v.size() == 1){
                        QueryBuilder queryBuilder = QueryBuilders.matchQuery(sname, v.get(0));
//                        boolQueryBuilder.must(queryBuilder);
                        filterOrMust(wrappers,boolQueryBuilder,queryBuilder);
                    }
                }else if(!isAnd && type != ESBaseWrapper.QueryType.LIKE) {
                    if (v.size() > 1) {
                        QueryBuilder queryBuilder = QueryBuilders.termsQuery(sname, v);
                        boolQueryBuilder.should(queryBuilder);
                    } else if (v.size() == 1) {
                        QueryBuilder queryBuilder = QueryBuilders.matchQuery(sname, v.get(0));
                        boolQueryBuilder.should(queryBuilder);
                    }

                }else if(type == ESBaseWrapper.QueryType.LIKE){
                    v.forEach(item->{
                        QueryBuilder queryBuilder = QueryBuilders.wildcardQuery(k,"*"+item+"*");
                        if(isAnd)
//                            boolQueryBuilder.must(queryBuilder);
                            filterOrMust(wrappers,boolQueryBuilder,queryBuilder);
                        else
                            boolQueryBuilder.should(queryBuilder);
                    });
                }
//            }
        });
        return boolQueryBuilder;
    }

    @Deprecated
    private QueryBuilder makeQuery(List<KeyAttribute> keys,
                                   Field field,
                                   BoolQueryBuilder boolQueryBuilder,
                                   boolean isAnd,
                                   ESBaseWrapper.QueryType type
    ){
        ESField f = field.getAnnotation(ESField.class);
        if(keys != null && keys.size() != 0){
            for(KeyAttribute ka: keys){
                if(ka.getKey().equals(field.getName())){
                    String sname = field.getName();
                    Object value = ka.getValue();
//                    if(value == null)
//                        continue;;
                    if(!field.getType().equals(ka.getValue().getClass())){
                        if(field.getType().equals(Date.class)){
                            if(f != null && f.type() == ESFieldType.Date && value != null && value instanceof Long){
                                value = new Date((long)value);
                            }
                        }
                    }
                    QueryBuilder queryBuilder;
                    switch (type){
//                        case MATCH:
//                            queryBuilder = QueryBuilders.matchQuery(sname,value);
//                            break;
                        case LT:
                            queryBuilder = QueryBuilders.rangeQuery(sname).lt(value);
                            break;
                        case LTE:
                            queryBuilder = QueryBuilders.rangeQuery(sname).lte(value);
                            break;
                        case GT:
                            queryBuilder = QueryBuilders.rangeQuery(sname).gt(value);
                            break;
                        case GTE:
                            queryBuilder = QueryBuilders.rangeQuery(sname).gte(value);
                            break;
                        default:
                            queryBuilder = QueryBuilders.matchQuery(sname,value);//.operator(Operator.AND);

                            break;
                    }
                    if(isAnd) {
                        boolQueryBuilder.must(queryBuilder);
                    }else{
                        boolQueryBuilder.should(queryBuilder);
                    }
                }
            }
        }
        return boolQueryBuilder;
    }


    public boolean createIndex(String indexName) throws Exception{
        Settings.Builder settings = Settings.builder();
        ESDocument document = this.getTClass().getAnnotation(ESDocument.class);
        settings.put("index.number_of_shards",document.shards());
        settings.put("index.number_of_replicas",document.replicas());
        settings.put("index.max_result_window",2000000);
        settings.put("analysis.normalizer.my_normalizer.type","custom");
        settings.putList("analysis.normalizer.my_normalizer.filter","lowercase","asciifolding");//"[\"lowercase\", \"asciifolding\"]"
        settings.put("analysis.analyzer.comma.type","pattern");
        settings.put("analysis.analyzer.comma.pattern",",");

        CreateIndexRequest request = new CreateIndexRequest(indexName);//创建索引
//        request.alias(new Alias("IT_IS_ALIAS"));
        request.mapping(MappingBuilder.setMapping(this.getTClass(),true));
        request.settings(settings);
        RestHighLevelClient rhlClient = restHighLevelClient;
        log.info(request.toString());
        CreateIndexResponse createIndexResponse = rhlClient.indices().create(request, RequestOptions.DEFAULT);
        boolean acknowledged = createIndexResponse.isAcknowledged();//指示是否所有节点都已确认请求
        boolean shardsAcknowledged = createIndexResponse.isShardsAcknowledged();
        return acknowledged;
    }
    public boolean createIndex() throws Exception{
        return this.createIndex(this.getTClass());
    }
    public boolean createIndex(Class<? extends T> clz) throws Exception {
        if(!clz.isAnnotationPresent(ESDocument.class))
            throw new Exception("必须有Document注解");
        ESDocument document = clz.getAnnotation(ESDocument.class);
        String indexName = document.indexName().toLowerCase();
        return createIndex(indexName);
    }


    public boolean deleteIndex() throws Exception{
        return this.deleteIndex(this.getIndexName());
    }
    public boolean deleteIndex(Class<? extends T> clz) throws Exception {
        if(!clz.isAnnotationPresent(ESDocument.class))
            throw new Exception("必须有Document注解");
        ESDocument document = clz.getAnnotation(ESDocument.class);
        String indexName = document.indexName().toLowerCase();
        return this.deleteIndex(indexName);
    }
    public boolean deleteIndex(String index) throws IOException {
        RestHighLevelClient rhlClient = restHighLevelClient;
        DeleteIndexRequest request = new DeleteIndexRequest(index);//指定要删除的索引名称

        request.timeout(TimeValue.timeValueMinutes(2)); //设置超时，等待所有节点确认索引删除（使用TimeValue形式）
        request.masterNodeTimeout(TimeValue.timeValueMinutes(1));////连接master节点的超时时间(使用TimeValue方式)
        //设置IndicesOptions控制如何解决不可用的索引以及如何扩展通配符表达式
        request.indicesOptions(IndicesOptions.lenientExpandOpen());

        //同步执行
        AcknowledgedResponse deleteIndexResponse = rhlClient.indices()
                .delete(request,RequestOptions.DEFAULT);
        //Delete Index Response
        boolean acknowledged = deleteIndexResponse.isAcknowledged();//是否所有节点都已确认请求
        return acknowledged;
    }
    private IndexRequest insertParam(T entity){
        IndexRequest indexRequest = new IndexRequest(getIndexName());//, index);
        String source = JSONObject.toJSONString(entity);
        indexRequest.id(MappingBuilder.getID(entity)+"").source(source, XContentType.JSON);
        return indexRequest;
    }
    public boolean insert(T entity) throws Exception {
        RestHighLevelClient rhlClient = restHighLevelClient;
        IndexRequest indexRequest = insertParam(entity);
        IndexResponse indexResponse = rhlClient.index(indexRequest, RequestOptions.DEFAULT);
        log.info(indexResponse.status()+"");
        if(RestStatus.CREATED == indexResponse.status())
            return true;
        return false;
    }
    public boolean insertAll(List<T> entityList) throws Exception{
        RestHighLevelClient rhlClient = restHighLevelClient;
        BulkRequest request = new BulkRequest();
        for (T entity : entityList) {
            IndexRequest indexRequest = insertParam(entity);
            request.add(indexRequest);
        }
        BulkResponse responses = rhlClient.bulk(request, RequestOptions.DEFAULT);
        log.info(responses.status()+"");
        if(responses.hasFailures()) {
            for (BulkItemResponse res : responses.getItems()) {
                if(res.getFailureMessage()!= null && StringUtils.isNotBlank(res.getFailureMessage()))
                    log.error(res.getFailureMessage());
            }
        }
        if(responses.status() == RestStatus.OK)
            return true;
        else
            return false;
    }
    public boolean insertAllAsync(List<T> entityList) throws Exception{
        RestHighLevelClient rhlClient = restHighLevelClient;
        BulkRequest request = new BulkRequest();
        for (T entity : entityList) {
            IndexRequest indexRequest = insertParam(entity);
            request.add(indexRequest);
        }
        rhlClient.bulkAsync(request, RequestOptions.DEFAULT, new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse bulkItemResponses) {
                log.info("异步批量插入>>>>");
                for(BulkItemResponse res :bulkItemResponses.getItems()){
                    log.error(res.getFailureMessage());
                }
            }
            @Override
            public void onFailure(Exception e) {
                log.info("异步批量插入错误",e);
            }
        });
        return true;
    }

    private String getIndexName(){
        ESDocument document = this.getTClass().getAnnotation(ESDocument.class);
        return document.indexName().toLowerCase();
    }
    public boolean update(T entity,ESBaseMapper<T> wrapper) throws Exception {
        UpdateRequest request = new UpdateRequest();
        String lang = "painless";
        StringBuffer sb = new StringBuffer();
        List<Field> list = MappingBuilder.getFields(entity.getClass());
        list.stream().forEach(item->{
            Object obj = ESWrapperBuilder.getInfoValue(entity,item.getName());
            if(obj != null){

            }

        });
//        request.script(new Script(ScriptType.INLINE))
        return true;
    }

    /**
     *
     * @param entity
     * @return
     */
    public boolean updateById(T entity) throws IOException {
        UpdateRequest request;
        RestHighLevelClient rhlClient = restHighLevelClient;
        Object id = MappingBuilder.getID(entity);
        request = new UpdateRequest  (
                    getIndexName(),//索引
                    id+"");//文档ID
        request.doc(JSONObject.toJSONString(entity, SerializerFeature.WriteMapNullValue),XContentType.JSON);
        UpdateResponse response = rhlClient.update(request,RequestOptions.DEFAULT);
        if(RestStatus.NOT_FOUND.equals(response.status())){
            log.info("更新指定索引失败");
            return false;
        }else{
            log.info("更新指定索引成功");
            return true;
        }
    }

    /**
     *
     * @param entityList
     * @return
     */
    public boolean updateAllCoulmusByIdAsync(List<T> entityList) throws Exception{
        BulkRequest bulkRequest = new BulkRequest();
        for(T entity : entityList) {
            UpdateRequest request;
            Object obj = MappingBuilder.getID(entity);
            request = new UpdateRequest(
                    getIndexName(),//索引
                    obj+"");//文档ID
            request.doc(JSONObject.toJSONString(entity), XContentType.JSON);
            bulkRequest.add(request);
        }
        RestHighLevelClient rhlClient = restHighLevelClient;
        rhlClient.bulkAsync(bulkRequest, RequestOptions.DEFAULT, new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse bulkItemResponses) {
                log.info("批量更新>>>>>");
                for(BulkItemResponse res :bulkItemResponses.getItems()){
                    log.error(res.getFailureMessage());
                }
            }
            @Override
            public void onFailure(Exception e) {
                log.error("批量更新错误",e);
            }
        });
        return true;
    }
    public Boolean deleteByIdAsync(List<T> entityList){
        RestHighLevelClient rhlClient = restHighLevelClient;
        BulkRequest bulkRequest = new BulkRequest();
        for(T entity : entityList) {
            DeleteRequest request;
            Object obj = MappingBuilder.getID(entity);
            request = new DeleteRequest(
                    getIndexName(),//索引
                    obj+"");//文档ID
            bulkRequest.add(request);
        }
        rhlClient.bulkAsync(bulkRequest, RequestOptions.DEFAULT,
                new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse bulkItemResponses) {
                log.info("删除条目>>>>>");
                for(BulkItemResponse res :bulkItemResponses.getItems()){
                    log.error(res.getFailureMessage());
                }
            }
            @Override
            public void onFailure(Exception e) {
                log.error("删除条目错误",e);
            }
        });
        return true;
    }
    public Boolean deleteById(Serializable id) throws Exception {
        RestHighLevelClient rhlClient = restHighLevelClient;
        DeleteRequest deleteByQueryRequest = new DeleteRequest(getIndexName());
        deleteByQueryRequest.routing("routing");
        deleteByQueryRequest.timeout(TimeValue.timeValueMinutes(2)); //设置超时，等待所有节点确认索引删除（使用TimeValue形式）
        if(id.getClass().equals(this.getTClass())){
            id = (Serializable) MappingBuilder.getID(id);
        }
        deleteByQueryRequest.id(id.toString());
        DeleteResponse delete;
        boolean result;
        delete = rhlClient.delete(deleteByQueryRequest, RequestOptions.DEFAULT);
        if(RestStatus.NOT_FOUND.equals(delete.status())){
            log.info("删除指定索引失败");
            result = false;
        }else{
            log.info("删除指定索引成功");
            result = true;
        }
        return result;
    }
}
