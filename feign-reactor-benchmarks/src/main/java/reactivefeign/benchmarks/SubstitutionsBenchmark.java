package reactivefeign.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class SubstitutionsBenchmark {

    private Map<Integer, Collection<String>> indexToName;
    private Object[] argv;

    @Setup
    public void setup() {
        indexToName = new HashMap<>();
        indexToName.put(0, Collections.singletonList("param1"));
        indexToName.put(1, Arrays.asList("param2", "param3"));
        indexToName.put(2, Collections.singletonList("param4"));
        // simulating a null argument at index 3
        indexToName.put(3, Collections.singletonList("param5"));

        argv = new Object[]{"value1", "value2", 12345, null};
    }

    @Benchmark
    public Map<String, Object> streamBased() {
        return indexToName.entrySet().stream()
                .filter(e -> argv[e.getKey()] != null)
                .flatMap(e -> e.getValue().stream()
                        .map(v -> new AbstractMap.SimpleImmutableEntry<>(e.getKey(), v)))
                .collect(toMap(Map.Entry::getValue,
                        entry -> argv[entry.getKey()]));
    }

    @Benchmark
    public Map<String, Object> loopBased() {
        Map<String, Object> substitutions = new LinkedHashMap<>();
        for (Map.Entry<Integer, Collection<String>> entry : indexToName.entrySet()) {
            Integer index = entry.getKey();
            Object value = argv[index];
            if (value != null) {
                for (String name : entry.getValue()) {
                    substitutions.put(name, value);
                }
            }
        }
        return substitutions;
    }
}
