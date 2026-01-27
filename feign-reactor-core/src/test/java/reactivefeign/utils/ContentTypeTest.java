package reactivefeign.utils;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class ContentTypeTest {

    @Test
    public void shouldParseContentTypeWoCharset() {
        ContentType contentType = ContentType.parse("application/json");
        assertThat(contentType.getMediaType()).isEqualTo("application/json");
        assertThat(contentType.getCharset()).isEqualTo(StandardCharsets.UTF_8);

        contentType = ContentType.parse("text/plain");
        assertThat(contentType.getMediaType()).isEqualTo("text/plain");
        assertThat(contentType.getCharset()).isEqualTo(StandardCharsets.UTF_8);
    }

    @Test
    public void shouldParseContentTypeWithCharset() {
        ContentType contentType = ContentType.parse("application/json; charset=utf-8");
        assertThat(contentType.getMediaType()).isEqualTo("application/json");
        assertThat(contentType.getCharset()).isEqualTo(StandardCharsets.UTF_8);

        contentType = ContentType.parse("text/plain; charset=ISO-8859-1");
        assertThat(contentType.getMediaType()).isEqualTo("text/plain");
        assertThat(contentType.getCharset()).isEqualTo(StandardCharsets.ISO_8859_1);

        contentType = ContentType.parse("text/html; charset=\"UTF-16\"");
        assertThat(contentType.getMediaType()).isEqualTo("text/html");
        assertThat(contentType.getCharset()).isEqualTo(StandardCharsets.UTF_16);

        contentType = ContentType.parse("multipart/form-data; boundary=something; charset=utf-8");
        assertThat(contentType.getMediaType()).isEqualTo("multipart/form-data");
        assertThat(contentType.getCharset()).isEqualTo(StandardCharsets.UTF_8);
    }
}
