package easy.framework.demo.mapper;

import easy.framework.demo.bean.TestIndex;
import easy.framework.elasticsearch.mapper.ESBaseMapper;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

/**
 * @author: Zhangtong
 * @description:
 * @Date: 2020/2/26.
 */
@Service
@Repository
public class TestIndexMapper extends ESBaseMapper<TestIndex> {
}
