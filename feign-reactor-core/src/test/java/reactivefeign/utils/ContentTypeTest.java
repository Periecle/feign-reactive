package reactivefeign.utils;

import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class ContentTypeTest {

    @Test
    public void shouldParseContentTypeWoCharset() {
        ContentType contentType = ContentType.parse("application/json");
        assertThat(contentType.getMediaType()).isEqualTo("application/json");
        assertThat(contentType.getCharset()).isEqualTo(StandardCharsets.UTF_8);
    }

    @Test
    public void shouldParseContentTypeWithCharset() {
        // Standard case
        ContentType contentType = ContentType.parse("application/json; charset=UTF-8");
        assertThat(contentType.getMediaType()).isEqualTo("application/json");
        assertThat(contentType.getCharset()).isEqualTo(StandardCharsets.UTF_8);

        // Lowercase charset
        contentType = ContentType.parse("application/json; charset=utf-8");
        assertThat(contentType.getMediaType()).isEqualTo("application/json");
        assertThat(contentType.getCharset()).isEqualTo(StandardCharsets.UTF_8);

        // Quoted charset
        contentType = ContentType.parse("application/json; charset=\"UTF-8\"");
        assertThat(contentType.getMediaType()).isEqualTo("application/json");
        assertThat(contentType.getCharset()).isEqualTo(StandardCharsets.UTF_8);

        // Different charset
        contentType = ContentType.parse("text/plain; charset=ISO-8859-1");
        assertThat(contentType.getMediaType()).isEqualTo("text/plain");
        assertThat(contentType.getCharset()).isEqualTo(StandardCharsets.ISO_8859_1);

        // Extra parameters (boundary) without charset
        contentType = ContentType.parse("application/json; boundary=something");
        assertThat(contentType.getMediaType()).isEqualTo("application/json");
        assertThat(contentType.getCharset()).isEqualTo(StandardCharsets.UTF_8);

        // Charset not the first parameter
        contentType = ContentType.parse("multipart/form-data; boundary=something; charset=UTF-8");
        assertThat(contentType.getMediaType()).isEqualTo("multipart/form-data");
        assertThat(contentType.getCharset()).isEqualTo(StandardCharsets.UTF_8);

        // Unsupported charset fallback
        contentType = ContentType.parse("application/json; charset=UNKNOWN");
        assertThat(contentType.getMediaType()).isEqualTo("application/json");
        assertThat(contentType.getCharset()).isEqualTo(StandardCharsets.UTF_8);

        // Spaces around semicolon
        contentType = ContentType.parse("application/json ; charset=UTF-8");
        assertThat(contentType.getMediaType()).isEqualTo("application/json");
        assertThat(contentType.getCharset()).isEqualTo(StandardCharsets.UTF_8);
    }
}
