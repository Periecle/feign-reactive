package reactivefeign.methodhandler;

import feign.Contract;
import feign.MethodMetadata;
import feign.Param;
import feign.RequestLine;
import feign.Target;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.reactivestreams.Publisher;
import reactivefeign.client.ReactiveHttpRequest;
import reactivefeign.publisher.PublisherHttpClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Measurement(iterations = 5, time = 1)
@Warmup(iterations = 5, time = 1)
@Fork(value = 1, warmups = 0)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class UrlExpansionBenchmark {

    private PublisherClientMethodHandler handler;
    private Object[] args;

    interface BenchmarkClient {
        @RequestLine("GET /api/resources/{id}/details/{subId}?filter={filter}&type={type}")
        Mono<String> getResource(@Param("id") String id, @Param("subId") String subId, @Param("filter") String filter, @Param("type") String type);
    }

    @Setup
    public void setup() {
        Target<BenchmarkClient> target = new Target.HardCodedTarget<>(BenchmarkClient.class, "http://localhost:8080");
        List<MethodMetadata> metadataList = new Contract.Default().parseAndValidateMetadata(BenchmarkClient.class);
        MethodMetadata methodMetadata = metadataList.get(0);

        PublisherHttpClient publisherHttpClient = new PublisherHttpClient() {
            @Override
            public Publisher<Object> executeRequest(ReactiveHttpRequest request) {
                return Mono.empty();
            }
        };

        handler = new PublisherClientMethodHandler(target, methodMetadata, publisherHttpClient);

        args = new Object[] { "12345", "67890", "active", "full" };
    }

    @Benchmark
    public ReactiveHttpRequest buildRequest() {
        return handler.buildRequest(args);
    }
}
