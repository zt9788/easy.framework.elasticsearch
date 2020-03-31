# easy.framework.elasticsearch
easy to use elasticsearch for 7.X

```java
ESWrappers<TestModelClass> entityWrapper = ESWrappers.<TestModelClass>build();
        entityWrapper.or().eq("key1","admin");
        entityWrapper.and().eq("key2",3);
        entityWrapper.subCondition()
                .or().lt("key3",1574179200000L)
                .or().isNull("key4")
                .endCondition()
        .order("key5",SortOrder.ASC)
        .setUseFilter(true);
        esCrmCaseMapper.selectList(entityWrapper);
```


​        
It is like

```sql
 where key1 = 'admin' and key2=3 and (key3 > 1574179200000L or key4 is null) order by key5 asc 
```

