package ru.shadam.tarantool.core.convert;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.shadam.tarantool.core.convert.ConversionTestEntities.Address;
import ru.shadam.tarantool.core.convert.ConversionTestEntities.AddressWithId;
import ru.shadam.tarantool.core.convert.ConversionTestEntities.Indexes;
import ru.shadam.tarantool.core.convert.ConversionTestEntities.Person;
import ru.shadam.tarantool.core.mapping.TarantoolMappingContext;
import ru.shadam.tarantool.test.util.Tuples;

import java.security.SecureRandom;
import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static ru.shadam.tarantool.core.convert.ConversionTestEntities.Gender.MALE;
import static ru.shadam.tarantool.core.convert.ConversionTestEntities.Indexes.Address.CITY;
import static ru.shadam.tarantool.core.convert.ConversionTestEntities.Indexes.Address.COUNTRY;
import static ru.shadam.tarantool.core.convert.ConversionTestEntities.Indexes.Person.*;
import static ru.shadam.tarantool.test.util.IsTupleMatcher.isTuple;


/**
 * @author sala
 */
public class MappingTarantoolConverterUnitTests {

    MappingTarantoolConverter converter;
    Person rand;

    @Before
    public void setUp() {
        converter = new MappingTarantoolConverter(new TarantoolMappingContext());
        converter.afterPropertiesSet();

        rand = new Person();
    }

    @Test
    public void writeAppendsKeyCorrectly() {
        rand.id = 1L;

        Assert.assertThat(write(rand).getId(), is(1L));
    }

    @Test
    public void writeAppendsKeyCorrectlyWhenThereIsAnAdditionalIdFieldInNestedElement() {

        AddressWithId address = new AddressWithId();
        address.id = "tear";
        address.city = "Tear";

        rand.id = 1L;
        rand.address = address;

        TarantoolData data = write(rand);

        assertThat(data.getId(), is(1L));
        assertThat(data.getTuple(), isTuple().containingUtf8String(Path.of(ADDRESS, Indexes.Address.ID), "tear"));
    }

    @Test
    public void writeDoesNotAppendPropertiesWithNullValues() {

        rand.firstname = "rand";

        assertThat(write(rand).getTuple(), isTuple().without(Path.of(GENDER)));
    }

    @Test
    public void writeDoesNotAppendPropertiesWithEmptyCollections() {

        rand.firstname = "rand";

        assertThat(write(rand).getTuple(), isTuple().without(Path.of(NICKNAMES)));
    }

    @Test
    public void writeAppendsSimpleRootPropertyCorrectly() {

        rand.firstname = "nynaeve";

        assertThat(write(rand).getTuple(), isTuple().containingUtf8String(Path.of(FIRSTNAME), "nynaeve"));
    }

    @Test
    public void writeAppendsListOfSimplePropertiesCorrectly() {

        rand.nicknames = Arrays.asList("dragon reborn", "lews therin");

        TarantoolData target = write(rand);

        assertThat(target.getTuple(), isTuple()
                .containingUtf8String(Path.of(NICKNAMES, 0), "dragon reborn")
                .containingUtf8String(Path.of(NICKNAMES, 1), "lews therin"));
    }

    @Test
    public void writeAppendsComplexObjectCorrectly() {

        Address address = new Address();
        address.city = "two rivers";
        address.country = "andora";
        rand.address = address;

        TarantoolData target = write(rand);

        assertThat(target.getTuple(), isTuple()
                .containingUtf8String(Path.of(ADDRESS, CITY), "two rivers")
                .containingUtf8String(Path.of(ADDRESS, COUNTRY), "andora"));
    }

    @Test
    public void writeAppendsListOfComplexObjectsCorrectly() {

        Person mat = new Person();
        mat.firstname = "mat";
        mat.nicknames = Arrays.asList("prince of the ravens");

        Person perrin = new Person();
        perrin.firstname = "perrin";
        perrin.address = new Address();
        perrin.address.city = "two rivers";

        rand.coworkers = Arrays.asList(mat, perrin);
        rand.id = new SecureRandom().nextLong();
        rand.firstname = "rand";

        TarantoolData target = write(rand);

        assertThat(target.getTuple(),
            isTuple()
                .containingUtf8String(Path.of(COWORKERS, 0, FIRSTNAME), "mat") //
                .containingUtf8String(Path.of(COWORKERS, 0, NICKNAMES, 0), "prince of the ravens") //
                .containingUtf8String(Path.of(COWORKERS, 1, FIRSTNAME), "perrin") //
                .containingUtf8String(Path.of(COWORKERS, 1, ADDRESS, CITY), "two rivers"));
    }

    @Test
    public void readConvertsSimplePropertiesCorrectly() {

        TarantoolData rdo = new TarantoolData(Tuples.of(
            Path.of(ID), 1L,
            Path.of(FIRSTNAME), "rand")
        );
        rdo.setId(123L);

        assertThat(converter.read(Person.class, rdo).firstname, is("rand"));
    }

    @Test
    public void readConvertsListOfSimplePropertiesCorrectly() {

        Tuple tuple = new Tuple();
        tuple.set(Path.of(NICKNAMES, 0), "dragon reborn");
        tuple.set(Path.of(NICKNAMES, 1), "lews therin");
        TarantoolData rdo = new TarantoolData(tuple);

        final List<String> nicknames = converter.read(Person.class, rdo).nicknames;
        assertEquals("dragon reborn", nicknames.get(0));
        assertEquals("lews therin", nicknames.get(1));
    }

    @Test
    public void readConvertsUnorderedListOfSimplePropertiesCorrectly() {

        Tuple map = new Tuple();
        map.set(Path.of(NICKNAMES, 9), "car'a'carn");
        map.set(Path.of(NICKNAMES, 10), "lews therin");
        map.set(Path.of(NICKNAMES, 1), "dragon reborn");
        TarantoolData rdo = new TarantoolData(map);

        final List<String> nicknames = converter.read(Person.class, rdo).nicknames;
        assertEquals("dragon reborn", nicknames.get(1));
        assertEquals("car'a'carn", nicknames.get(9));
        assertEquals("lews therin", nicknames.get(10));
    }

    @Test
    public void readComplexPropertyCorrectly() {

        Tuple map = new Tuple();
        map.set(Path.of(ADDRESS, CITY), "two rivers");
        map.set(Path.of(ADDRESS, COUNTRY), "andor");
        TarantoolData rdo = new TarantoolData(map);

        Person target = converter.read(Person.class, rdo);

        assertThat(target.address, notNullValue());
        assertThat(target.address.city, is("two rivers"));
        assertThat(target.address.country, is("andor"));
    }

    @Test
    public void readListComplexPropertyCorrectly() {
        Tuple map = new Tuple();
        map.set(Path.of(COWORKERS, 0, FIRSTNAME), "mat");
        map.set(Path.of(COWORKERS, 0, NICKNAMES, 0), "prince of the ravens");
        map.set(Path.of(COWORKERS, 0, NICKNAMES, 1), "gambler");
        map.set(Path.of(COWORKERS, 1, FIRSTNAME), "perrin");
        map.set(Path.of(COWORKERS, 1, ADDRESS, CITY), "two rivers");
        TarantoolData rdo = new TarantoolData(map);

        Person target = converter.read(Person.class, rdo);

        assertThat(target.coworkers, notNullValue());
        assertThat(target.coworkers.get(0).firstname, is("mat"));
        assertThat(target.coworkers.get(0).nicknames, notNullValue());
        assertThat(target.coworkers.get(0).nicknames.get(0), is("prince of the ravens"));
        assertThat(target.coworkers.get(0).nicknames.get(1), is("gambler"));

        assertThat(target.coworkers.get(1).firstname, is("perrin"));
        assertThat(target.coworkers.get(1).address.city, is("two rivers"));
    }

    @Test
    public void readUnorderedListOfComplexPropertyCorrectly() {

        Tuple map = new Tuple();
        map.set(Path.of(COWORKERS, 1, FIRSTNAME), "perrin");
        map.set(Path.of(COWORKERS, 1, ADDRESS, CITY), "two rivers");
        map.set(Path.of(COWORKERS, 0, FIRSTNAME), "mat");
        map.set(Path.of(COWORKERS, 0, NICKNAMES, 1), "gambler");
        map.set(Path.of(COWORKERS, 0, NICKNAMES, 0), "prince of the ravens");

        TarantoolData rdo = new TarantoolData(map);

        Person target = converter.read(Person.class, rdo);

        assertThat(target.coworkers, notNullValue());
        assertThat(target.coworkers.get(0).firstname, is("mat"));
        assertThat(target.coworkers.get(0).nicknames, notNullValue());
        assertThat(target.coworkers.get(0).nicknames.get(0), is("prince of the ravens"));
        assertThat(target.coworkers.get(0).nicknames.get(1), is("gambler"));

        assertThat(target.coworkers.get(1).firstname, is("perrin"));
        assertThat(target.coworkers.get(1).address.city, is("two rivers"));
    }

    @Test
    public void writesIntegerValuesCorrectly() {

        rand.age = 20;

        assertThat(write(rand).getTuple(), isTuple().containingUtf8String(Path.of(AGE), "20"));
    }

    @Test
    public void writesEnumValuesCorrectly() {

        rand.gender = MALE;

        assertThat(write(rand).getTuple(), isTuple().containingUtf8String(Path.of(GENDER), "MALE"));
    }
    
    @Test
    public void readsEnumValuesCorrectly() {
        Tuple tuple = new Tuple();
        tuple.set(Path.of(GENDER), "MALE");

        Person target = converter.read(Person.class, new TarantoolData(tuple));

        assertThat(target.gender, is(MALE));
    }

    @Test
    public void writesBooleanValuesCorrectly() {

        rand.alive = Boolean.TRUE;

        assertThat(write(rand).getTuple(), isTuple().containingUtf8String(Path.of(ALIVE), "1"));
    }

    @Test
    public void readsBooleanValuesCorrectly() {

        Person target = converter.read(Person.class,
                new TarantoolData(Tuples.of(Path.of(ALIVE), "1")));

        assertThat(target.alive, is(Boolean.TRUE));
    }

    @Test
    public void readsStringBooleanValuesCorrectly() {

        Person target = converter.read(Person.class,
                new TarantoolData(Tuples.of(Path.of(ALIVE), "true")));

        assertThat(target.alive, is(Boolean.TRUE));
    }

    @Test
    public void writesDateValuesCorrectly() {

        Calendar cal = Calendar.getInstance();
        cal.set(1978, 10, 25);

        rand.birthday = cal.getTime();

        assertThat(write(rand).getTuple(), isTuple().containingDateAsMsec(Path.of(BIRTHDAY), rand.birthday));
    }

    @Test
    public void readsDateValuesCorrectly() {

        Calendar cal = Calendar.getInstance();
        cal.set(1978, 10, 25);

        Date date = cal.getTime();

        Person target = converter.read(Person.class, new TarantoolData(
                Tuples.of(Path.of(BIRTHDAY), Long.valueOf(date.getTime()).toString())));

        assertThat(target.birthday, is(date));
    }

    private TarantoolData write(Object source) {

        TarantoolData rdo = new TarantoolData();
        converter.write(source, rdo);
        return rdo;
    }

    private <T> T read(Class<T> type, List<Object> source) {
        return converter.read(type, new TarantoolData(new Tuple(source)));
    }
}
