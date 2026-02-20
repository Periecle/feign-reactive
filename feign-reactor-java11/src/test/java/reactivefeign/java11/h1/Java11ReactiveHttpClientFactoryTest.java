package reactivefeign.java11.h1;

import com.fasterxml.jackson.core.async_.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Target;
import org.junit.jupiter.api.Test;
import org.mockito.exceptions.base.MockitoException;
import reactivefeign.java11.Java11ReactiveOptions;
import reactivefeign.java11.client.Java11ReactiveHttpClientFactory;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.*;

public class Java11ReactiveHttpClientFactoryTest {

    @Test
    public void shouldMakePreliminaryCallToUpgradeToHttp2() throws IOException, InterruptedException {
        HttpClient httpClient;
        try {
            httpClient = mock(HttpClient.class);
        } catch (MockitoException e) {
            assumeTrue(false, "Mockito cannot mock HttpClient on this JDK: " + e.getMessage());
            return;
        }
        when(httpClient.version()).thenReturn(HttpClient.Version.HTTP_2);

        Target mock = mock(Target.class);
        when(mock.url()).thenReturn("http://test.url");

        Java11ReactiveHttpClientFactory reactiveHttpClient = new Java11ReactiveHttpClientFactory(
                httpClient, new JsonFactory(), new ObjectMapper(),
                ((Java11ReactiveOptions.Builder)new Java11ReactiveOptions.Builder().setUseHttp2(true)).build());
        reactiveHttpClient.target(mock);

        verify(httpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

}
