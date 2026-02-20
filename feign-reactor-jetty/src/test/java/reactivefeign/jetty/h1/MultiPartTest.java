package reactivefeign.jetty.h1;

import org.junit.jupiter.api.Disabled;
import reactivefeign.ReactiveFeignBuilder;
import reactivefeign.jetty.JettyReactiveFeign;

@Disabled
//TODO add support for Jetty based
public class MultiPartTest extends reactivefeign.MultiPartTest {

    @Override
    protected ReactiveFeignBuilder<MultipartClient> builder() {
        return JettyReactiveFeign.builder();
    }

}
