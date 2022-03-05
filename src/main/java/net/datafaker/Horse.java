package net.datafaker;

/**
 * @since 1.3.0
 */
public class Horse {

    private final Faker faker;

    protected Horse(Faker faker) {
        this.faker = faker;
    }

    public String horse() {
        return faker.fakeValuesService().resolve("creature.horse", this, faker);
    }

}
