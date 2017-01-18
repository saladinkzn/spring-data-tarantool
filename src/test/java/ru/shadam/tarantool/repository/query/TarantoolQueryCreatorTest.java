package ru.shadam.tarantool.repository.query;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.DefaultParameters;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.parser.PartTree;
import ru.shadam.tarantool.core.convert.ConversionTestEntities.Person;

import java.lang.reflect.Method;

/**
 * @author sala
 */
public class TarantoolQueryCreatorTest {

    @Test
    public void findBySingleSimpleRepository() throws NoSuchMethodException {
        TarantoolQueryCreator creator = createQueryCreatorForMethodWithArgs(
            SampleRepository.class.getMethod("findByFirstname", String.class), new Object[]{ "eddard" }
        );

        final KeyValueQuery<TarantoolQuery> query = creator.createQuery();

        Assert.assertEquals(new TarantoolQuery("firstname", "eddard"), query.getCritieria());
    }

    private TarantoolQueryCreator createQueryCreatorForMethodWithArgs(Method method, Object[] args) {

        PartTree partTree = new PartTree(method.getName(), method.getReturnType());
        TarantoolQueryCreator creator = new TarantoolQueryCreator(partTree, new ParametersParameterAccessor(new DefaultParameters(
                method), args));

        return creator;
    }


    private interface SampleRepository extends Repository<Person, Long> {

        Person findByFirstname(String firstname);

    }
}