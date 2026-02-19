package reactivefeign.benchmarks;

import org.openjdk.jmh.annotations.*;
import java.util.*;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toMap;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(java.util.concurrent.TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
public class SubstitutionsBenchmark {

    private Map<Integer, Collection<String>> indexToName;
    private Object[] argv;

    @Setup
    public void setup() {
        indexToName = new HashMap<>();
        // Simulate a typical method with a few path/query params
        indexToName.put(0, Collections.singletonList("param0"));
        indexToName.put(1, Arrays.asList("param1", "alias1"));
        indexToName.put(2, Collections.singletonList("param2"));
        indexToName.put(3, Collections.singletonList("param3"));
        indexToName.put(5, Collections.singletonList("param5")); // Index 4 skipped (maybe body)

        argv = new Object[6];
        argv[0] = "value0";
        argv[1] = 123;
        argv[2] = null; // simulate null value
        argv[3] = "value3";
        argv[4] = "body"; // Not in indexToName
        argv[5] = "value5";
    }

    @Benchmark
    public Map<String, Object> baseline() {
        return indexToName.entrySet().stream()
                .filter(e -> argv[e.getKey()] != null)
                .flatMap(e -> e.getValue().stream()
                        .map(v -> new AbstractMap.SimpleImmutableEntry<>(e.getKey(), v)))
                .collect(toMap(Map.Entry::getValue,
                        entry -> argv[entry.getKey()]));
    }

    @Benchmark
    public Map<String, Object> optimized() {
        Map<String, Object> substitutions = new HashMap<>();
        for (Map.Entry<Integer, Collection<String>> entry : indexToName.entrySet()) {
            Integer index = entry.getKey();
            // Original code didn't check bounds, assuming metadata is correct relative to argv
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
