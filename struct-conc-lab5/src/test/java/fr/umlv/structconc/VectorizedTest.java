package fr.umlv.structconc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("static-method")
public class VectorizedTest {
    private static Stream<Arguments> provideIntArrays() {
        return IntStream.of(0, 1, 10, 100, 1000, 10_000, 100_000)
                .mapToObj(i -> new Random(0).ints(i, 0, 1000).toArray())
                .map(array -> Arguments.of(array, Arrays.stream(array).reduce(0, Integer::sum)));
    }

    private static Stream<Arguments> provideIntArraysSub() {
        return IntStream.of(0, 1, 10, 100, 1000, 10_000, 100_000)
                .mapToObj(i -> new Random(0).ints(i, 0, 1000).toArray())
                .map(array -> Arguments.of(array, Arrays.stream(array).reduce(0, Integer::sum) * -1));
    }

    private static Stream<Arguments> provideIntArraysMinMax() {
        return IntStream.of(0, 1, 10, 100, 1000, 10_000, 100_000)
                .mapToObj(i -> new Random(0).ints(i, 0, 1000).toArray())
                .map(array -> Arguments.of(array, new int[]{IntStream.of(array).min().orElse(Integer.MAX_VALUE), IntStream.of(array).max().orElse(Integer.MIN_VALUE)}));
    }

    @ParameterizedTest
    @MethodSource("provideIntArrays")
    public void sum(int[] array, int expected) {
        assertEquals(expected, Vectorized.sumLoop(array));
    }

    @ParameterizedTest
    @MethodSource("provideIntArrays")
    public void sumLane(int[] array, int expected) {
        assertEquals(expected, Vectorized.sumReduceLanes(array));
    }

    @ParameterizedTest
    @MethodSource("provideIntArrays")
    public void sumLaneWise(int[] array, int expected) {
        assertEquals(expected, Vectorized.sumLaneWise(array));
    }

    @ParameterizedTest
    @MethodSource("provideIntArraysSub")
    public void subLaneWise(int[] array, int expected) {
        assertEquals(expected, Vectorized.differenceLanewise(array));
    }

    @ParameterizedTest
    @MethodSource("provideIntArraysMinMax")
    public void minMaxLaneWise(int[] array, int[] expected) {
        var arrayMinMax = Vectorized.minmax(array);
        assertEquals(expected[0], arrayMinMax[0]);
        assertEquals(expected[1], arrayMinMax[1]);
    }


}

//Difference fork/join et vecteur
//dans les deux cas c est des api parralele
//les vecteur fait sur le meme cors

//fork/join le fait sur plusieurs cors