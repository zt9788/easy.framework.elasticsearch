package easy.framework.elasticsearch;

import easy.framework.demo.bean.TestIndex;
import easy.framework.demo.mapper.TestIndexMapper;
import easy.framework.elasticsearch.mapper.ESWrappers;
import easy.framework.elasticsearch.metadata.ESPage;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.annotation.Resource;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

@Slf4j
@SpringJUnitConfig
public class ElasticsearchApplicationTests {

	@Resource
	TestIndexMapper indexMapper;

//	@Resource
//	ElasticsearchRestTemplate elasticsearchRestTemplate;
	@Test
	void testES() throws Exception {
		ESPage<TestIndex> page =indexMapper.selectList(ESWrappers.<TestIndex>build()
				.setSize(2).setUseSearchAfter(true));

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
