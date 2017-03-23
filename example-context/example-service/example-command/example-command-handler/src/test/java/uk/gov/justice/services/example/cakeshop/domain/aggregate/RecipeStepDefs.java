package uk.gov.justice.services.example.cakeshop.domain.aggregate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static java.lang.System.out;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

public class RecipeStepDefs {
    private Stream<Object> events;
    private Class<?> clazz;
    private Object object;
    private static final String fmt = "%24s: %s%n";

    @Given("no previous events")
    public void no_previous_events() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        assert events == null;
    }

    @When("the method (.*) is called on the aggregate (.*)")
    public void add_new_receipe_2(final String methodName, final String aggregate, final String message) throws Exception {
        checkIfAggregateCreated(aggregate);
        Class<?>[] pType = paramsTypes(methodName);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualNode = mapper.readTree(message);
        Map argumentsMap = mapper.convertValue(actualNode, Map.class);
        List valuesList = new ArrayList(argumentsMap.values());
        checkIfUUID(valuesList);
        Object[] methodArgs = valuesList.toArray();
        Method method = object.getClass().getMethod(methodName, pType);
        if (argumentsMap.size() == 0) {
            events = (Stream<Object>) method.invoke(object, null);
        } else {
            events = (Stream<Object>) method.invoke(object, methodArgs);
        }
    }

    @Then("the events are generated with following data")
    public void new_recipe_event_generated(final String message) throws ClassNotFoundException, IOException, IllegalAccessException, InstantiationException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualNode = mapper.readTree(message);
        List<String> expectedEvents = mapper.convertValue(actualNode.get("events"), List.class);
        final List<Object> eventList = events.collect(toList());
        assertThat(eventList, hasSize(1));
        assertThat(eventList.size(), is(expectedEvents.size()));
        Iterator it = mapper.convertValue(actualNode, Map.class).entrySet().iterator();
        for (int index = 0; index < expectedEvents.size(); index++) {
            final Object event = eventList.get(0);
            final String expectedEvent = expectedEvents.get(index);
            assertThat(event, instanceOf(Class.forName(expectedEvent)));
            for (Field field : event.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    if (field.getName().equalsIgnoreCase((String) pair.getKey())) {
                        assertThat(field.get(event).toString(), is(pair.getValue().toString()));
                        break;
                    }
                    it.remove(); // avoids a ConcurrentModificationException
                }
            }
        }
    }

    private void checkIfAggregateCreated(final String aggregate) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (clazz == null && object == null) {
            this.clazz = Class.forName(aggregate);
            this.object = clazz.newInstance();
        }
    }

    private Class<?>[] paramsTypes(final String methodName) {
        Class<?>[] paramsTypes = null;
        for (Method m : clazz.getDeclaredMethods()) {
            if (!m.getName().equals(methodName)) {
                continue;
            }
            paramsTypes = m.getParameterTypes();
        }
        return paramsTypes;
    }

    private void checkIfUUID(final List argumentValues) {
        for (int index = 0; index < argumentValues.size(); index++) {
            try {
                if (argumentValues.get(index) instanceof String) {
                    UUID uuid = UUID.fromString((String) argumentValues.get(index));
                    argumentValues.remove(index);
                    argumentValues.add(index, uuid);
                }
            } catch (IllegalArgumentException exception) {
                continue;
            }
        }
    }
}
