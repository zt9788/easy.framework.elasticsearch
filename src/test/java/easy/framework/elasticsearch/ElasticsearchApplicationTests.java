package easy.framework.elasticsearch;

import com.alibaba.fastjson.JSONObject;
import easy.framework.demo.bean.TestIndex;
import easy.framework.demo.mapper.TestIndexMapper;
import easy.framework.elasticsearch.config.ESConfiguration;
import easy.framework.elasticsearch.config.ESProperties;
import easy.framework.elasticsearch.mapper.ESWrappers;
import easy.framework.elasticsearch.metadata.ESPage;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.BigDecimal;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootConfiguration
@ContextConfiguration
@SpringBootTest
public class ElasticsearchApplicationTests {

//	@Resource
//	TestIndexMapper indexMapper;

//	@Resource
//	ElasticsearchRestTemplate elasticsearchRestTemplate;
	@Test
	void testES() throws Exception {
		TestIndexMapper indexMapper = new TestIndexMapper();
		ESProperties esProperties = new ESProperties();
		esProperties.setUris("http://183.36.121.205:9500,http://183.36.117.253:9500");
		esProperties.setUsername("elastic");
		esProperties.setPassword("123456");
		esProperties.setMaxResultWindow(1000L);
		ESConfiguration config = new ESConfiguration();
		config.setEsProperties(esProperties);
		indexMapper.setEsProperties(esProperties);
		indexMapper.setRestHighLevelClient(config.getRestHighLevelClient());
//		indexMapper.createIndex();
		TestIndex ti = new TestIndex();
		ti.setBd(new BigDecimal(11));
		ti.setId(22L);
		ti.setAbc("fdafsda");
		indexMapper.insert(ti);
		ESPage<TestIndex> page =indexMapper.selectList(ESWrappers.<TestIndex>build()
				.setSize(2).setUseSearchAfter(true));
		System.out.println(JSONObject.toJSONString(page));

	}

	@Test
	void contextLoads() {
		log.info("test");
	}

	@Test
	public void test() throws ScriptException {
		String script = "var f = {\n" +
				"  value: 0,\n" +
				"  add: function(n) {\n" +
				"    this.value += n;\n" +
				"    return this.value;\n" +
				"  }\n" +
				"};\n" +
				"f; // return object to Java\n";
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
		ScriptObjectMirror obj = (ScriptObjectMirror)engine.eval(script);
		System.out.println("obj.value = " + obj.getMember("value"));
		System.out.println("obj.add(5): " + obj.callMember("add", 5));
		System.out.println("obj.add(-3): " + obj.callMember("add", -3));
		System.out.println("obj.value = " + obj.getMember("value"));
	}
}
