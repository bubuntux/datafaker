package net.datafaker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.datafaker.matchers.MatchesRegularExpression.matchesRegularExpression;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FakeCollectionTest extends AbstractFakerTest {
    @Test
    public void generateCollection() {
        List<String> names = new FakeCollection.Builder<String>()
            .suppliers(() -> faker.name().firstName(), () -> faker.name().lastName())
            .minLen(3)
            .maxLen(5).build().get();
        assertThat(names.size(), is(lessThanOrEqualTo(5)));
        assertThat(names.size(), is(greaterThanOrEqualTo(3)));
        for (String name : names) {
            assertThat(name, matchesRegularExpression("[a-zA-Z']+"));
        }
    }

    @Test
    public void generateCollectionWithDifferentObjects() {
        List<Object> objects = new FakeCollection.Builder<>()
            .suppliers(() -> faker.name().firstName(), () -> faker.random().nextInt(100))
            .maxLen(5).build().get();
        assertEquals(5, objects.size());
        for (Object object : objects) {
            assertTrue(object instanceof Integer || object instanceof String);
        }
    }

    @Test
    public void checkWrongArguments() {
        IllegalArgumentException iae = Assertions.assertThrows(IllegalArgumentException.class, () ->
            new FakeCollection.Builder<String>()
                .suppliers(() -> faker.name().firstName())
                .minLen(10)
                .maxLen(5).build().get());
        assertEquals("Max length must be not less than min length and not negative", iae.getMessage());
    }

    private interface Data {
        String name();

        String value();

        String range();

        String unit();
    }

    private static class BloodPressure implements Data {

        @Override
        public String name() {
            return "Mean Blood Pressure";
        }

        @Override
        public String value() {
            return faker.random().nextInt(60, 180) + "";
        }

        @Override
        public String range() {
            return "";
        }

        @Override
        public String unit() {
            return "mm Hg";
        }
    }

    private static class Glucose implements Data {

        @Override
        public String name() {
            return "Glucose";
        }

        @Override
        public String value() {
            return String.format("%.1f", faker.random().nextDouble(3.2, 5.5));
        }

        @Override
        public String range() {
            return "3.2-5.5";
        }

        @Override
        public String unit() {
            return "mmol/L";
        }
    }

    private static class Temperature implements Data {

        @Override
        public String name() {
            return "Temperature";
        }

        @Override
        public String value() {
            return faker.random().nextInt(30, 50) + "";
        }

        @Override
        public String range() {
            return "";
        }

        @Override
        public String unit() {
            return "degrees C";
        }
    }

    @Test
    public void myTest() {
        String separator = "$$$";
        int limit = 5;
        String csv = new FakeCollection.Builder<Data>().minLen(limit).maxLen(limit)
            .suppliers(BloodPressure::new, Glucose::new, Temperature::new)
            .build()
            .toCsv()
            .headers(() -> "name", () -> "value", () -> "range", () -> "unit")
            .columns(Data::name, Data::value, Data::range, Data::unit)
            .separator(separator)
            .build().get();

        int numberOfLines = 0;
        int numberOfSeparator = 0;
        for (int i = 0; i < csv.length(); i++) {
            if (csv.regionMatches(i, System.lineSeparator(), 0, System.lineSeparator().length())) {
                numberOfLines++;
            } else if (csv.regionMatches(i, separator, 0, separator.length())) {
                numberOfSeparator++;
            }
        }

        assertEquals(limit + 1, numberOfLines); // limit + 1 line for header
        assertEquals((limit + 1) * (4 - 1), numberOfSeparator); // number of lines * (number of columns - 1)
    }
}
