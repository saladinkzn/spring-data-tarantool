package ru.shadam.tarantool.core.convert;

import org.springframework.data.annotation.Id;
import ru.shadam.tarantool.annotation.Tuple;

import java.util.Date;
import java.util.List;

import static ru.shadam.tarantool.core.convert.ConversionTestEntities.Indexes.Address.CITY;
import static ru.shadam.tarantool.core.convert.ConversionTestEntities.Indexes.Address.COUNTRY;
import static ru.shadam.tarantool.core.convert.ConversionTestEntities.Indexes.Person.*;

/**
 * @author sala
 */
public class ConversionTestEntities {
    interface Indexes {
        interface Person {
            int ID = 0;
            int FIRSTNAME = 1;
            int GENDER = 2;
            int NICKNAMES = 3;
            int COWORKERS = 4;
            int AGE = 5;
            int ALIVE = 6;
            int BIRTHDAY = 7;
            int ADDRESS = 8;
        }

        interface Address {
            int CITY = 0;
            int COUNTRY = 1;
            int ID = 2;
        }
    }

    /**
     * @author sala
     */
    public static class Person {
        @Id
        @Tuple(index = ID)
        long id;
        @Tuple(index = FIRSTNAME)
        String firstname;
        @Tuple(index = GENDER)
        Gender gender;

        @Tuple(index = NICKNAMES)
        List<String> nicknames;
        @Tuple(index = COWORKERS)
        List<Person> coworkers;
        @Tuple(index = AGE)
        Integer age;
        @Tuple(index = ALIVE)
        Boolean alive;
        @Tuple(index = BIRTHDAY)
        Date birthday;

        @Tuple(index = ADDRESS)
        Address address;


        @Override
        public String toString() {
            return "Person{" +
                    "id=" + id +
                    ", firstname='" + firstname + '\'' +
                    ", gender=" + gender +
                    ", nicknames=" + nicknames +
                    ", coworkers=" + coworkers +
                    ", age=" + age +
                    ", alive=" + alive +
                    ", birthday=" + birthday +
                    ", address=" + address +
                    '}';
        }
    }

    /**
     * @author sala
     */
    public static class Address {
        @Tuple(index = CITY)
        String city;
        @Tuple(index = COUNTRY)
        String country;
    }

    public static class AddressWithId extends Address {
        @Id
        @Tuple(index = Indexes.Address.ID)
        String id;
    }

    public static enum Gender {
        MALE, FEMALE
    }
}
