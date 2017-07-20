package ru.shadam.tarantool.repository.entity;

import org.springframework.data.annotation.Id;
import ru.shadam.tarantool.annotation.Tuple;

/**
 * @author sala
 */
public class Address {
    @Id
    @Tuple(index =0)
    private Long id;

    @Tuple(index = 1)
    private String city;

    @Tuple(index = 2)
    private String street;

    @Tuple(index = 3)
    private String number;

    protected Address() {}

    public Address(String city, String street, String number) {
        this.city = city;
        this.street = street;
        this.number = number;
    }

    public Address(long id, String city, String street, String number) {
        this.id = id;
        this.city = city;
        this.street = street;
        this.number = number;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
