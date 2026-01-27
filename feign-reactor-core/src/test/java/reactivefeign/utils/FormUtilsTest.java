package reactivefeign.utils;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static reactivefeign.utils.FormUtils.serializeForm;

public class FormUtilsTest {

    @Test
    public void shouldSerializeSimpleMap() {
        Map<String, Object> formData = new LinkedHashMap<>();
        formData.put("key1", "value1");
        formData.put("key2", "value2");

        SerializedFormData result = serializeForm(formData, StandardCharsets.UTF_8);

        assertThat(result.getFormDataString()).isEqualTo("key1=value1&key2=value2");
    }

    @Test
    public void shouldSerializeMapWithCollections() {
        Map<String, Object> formData = new LinkedHashMap<>();
        formData.put("key1", Arrays.asList("value1a", "value1b"));
        formData.put("key2", "value2");

        SerializedFormData result = serializeForm(formData, StandardCharsets.UTF_8);

        assertThat(result.getFormDataString()).isEqualTo("key1=value1a&key1=value1b&key2=value2");
    }

    @Test
    public void shouldSerializeMapWithSpecialCharacters() {
        Map<String, Object> formData = new LinkedHashMap<>();
        formData.put("key 1", "value/1");
        formData.put("key&2", "value=2");

        SerializedFormData result = serializeForm(formData, StandardCharsets.UTF_8);

        assertThat(result.getFormDataString()).isEqualTo("key+1=value%2F1&key%262=value%3D2");
    }

    @Test
    public void shouldSerializeMapWithNullValues() {
        Map<String, Object> formData = new LinkedHashMap<>();
        formData.put("key1", "value1");
        formData.put("key2", null);

        SerializedFormData result = serializeForm(formData, StandardCharsets.UTF_8);

        assertThat(result.getFormDataString()).isEqualTo("key1=value1&key2");
    }

    @Test
    public void shouldSerializeMapWithNullValuesInCollection() {
        Map<String, Object> formData = new LinkedHashMap<>();
        formData.put("key1", Arrays.asList("value1", null));

        SerializedFormData result = serializeForm(formData, StandardCharsets.UTF_8);

        assertThat(result.getFormDataString()).isEqualTo("key1=value1&key1");
    }

    @Test
    public void shouldSerializeEmptyMap() {
        Map<String, Object> formData = Collections.emptyMap();

        SerializedFormData result = serializeForm(formData, StandardCharsets.UTF_8);

        assertThat(result.getFormDataString()).isEmpty();
    }
}
