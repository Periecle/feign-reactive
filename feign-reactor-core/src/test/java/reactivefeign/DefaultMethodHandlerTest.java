package reactivefeign;

import org.junit.jupiter.api.Test;
import reactivefeign.methodhandler.DefaultMethodHandler;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultMethodHandlerTest extends BaseReactorTest {

    @Test
    public void shouldThrowErrorOnNotDefaultMethod() throws NoSuchMethodException {
      assertThrows(AbstractMethodError.class, () -> {
        new DefaultMethodHandler(TestInterface.class.getMethod("notDefaultMethod"));
      });
    }

    @Test
    public void shouldFailIfNotBoundToProxy() throws Throwable {
      assertThrows(IllegalStateException.class, () -> {
        DefaultMethodHandler defaultMethodHandler
                = new DefaultMethodHandler(TestInterface.class.getMethod("defaultMethod"));
        defaultMethodHandler.invoke(new Object[0]);
      });
    }

    @Test
    public void shouldFailOnRebind() throws Throwable {
      assertThrows(IllegalStateException.class, () -> {
        DefaultMethodHandler defaultMethodHandler
                = new DefaultMethodHandler(TestInterface.class.getMethod("defaultMethod"));

        TestInterface impl = () -> Mono.empty();
        defaultMethodHandler.bindTo(impl);
        defaultMethodHandler.bindTo(impl);
      });
    }

    @Test
    public void shouldCallNotDefaultMethodOnActualImplementation() throws Throwable {
        DefaultMethodHandler defaultMethodHandler
                = new DefaultMethodHandler(TestInterface.class.getMethod("defaultMethod"));

        AtomicBoolean notDefaultMethodCalled = new AtomicBoolean(false);
        TestInterface impl = () -> {
            notDefaultMethodCalled.set(true);
            return Mono.empty();
        };

        defaultMethodHandler.bindTo(impl);

        defaultMethodHandler.invoke(new Object[0]);

        assertTrue(notDefaultMethodCalled.get());
    }

    interface TestInterface {
        Mono<String> notDefaultMethod();

        default Mono<String> defaultMethod(){
            return notDefaultMethod();
        }
    }

}
