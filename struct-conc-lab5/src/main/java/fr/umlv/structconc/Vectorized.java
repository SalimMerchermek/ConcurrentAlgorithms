package fr.umlv.structconc;

import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

public class Vectorized {
    public static int sumLoop(int[] array) {
        var sum = 0;
        for (var value : array) {
            sum += value;
        }
        return sum;
    }

    private static final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_PREFERRED;

    public static int sumReduceLanes(int[] array) {
        var sum = 0;
        var i = 0;
        var limit = array.length - (array.length % SPECIES.length());
        for (; i < limit; i += SPECIES.length()) { //main loop
            //utiliser les vecteurs
            IntVector vector = IntVector.fromArray(SPECIES, array, i);
            int result = vector.reduceLanes(VectorOperators.ADD);
            sum += result;
        }
        for (; i < array.length; i++) {   // post loop
            //pas utiliser les vecteurs
            sum += array[i];
        }
        return sum;
    }

    public static int sumLaneWise(int[] array) {
        var sum = 0;
        var i = 0;
        IntVector vectorSum = IntVector.zero(SPECIES);
        var limit = array.length - (array.length % SPECIES.length());
        for (; i < limit; i += SPECIES.length()) { //main loop
            //utiliser les vecteurs
            IntVector vector = IntVector.fromArray(SPECIES, array, i);
            vectorSum = vectorSum.add(vector);
        }
        sum = vectorSum.reduceLanes(VectorOperators.ADD);
        for (; i < array.length; i++) {   // post loop
            //pas utiliser les vecteurs
            sum += array[i];
        }
        return sum;
    }

    public static int differenceLanewise(int[] array) {
        var sum = 0;
        var i = 0;
        IntVector vectorSub = IntVector.zero(SPECIES);
        var limit = array.length - (array.length % SPECIES.length());
        for (; i < limit; i += SPECIES.length()) { //main loop
            //utiliser les vecteurs
            IntVector vector = IntVector.fromArray(SPECIES, array, i);
            vectorSub = vectorSub.sub(vector);
        }
        var subArray = vectorSub.toArray();
        for (var elt : subArray) {
            sum += elt;
        }
        for (; i < array.length; i++) {   // post loop
            //pas utiliser les vecteurs
            sum -= array[i];
        }
        return sum;
    }

    public static int[] minmax(int[] array) {
        var maxVect = IntVector.broadcast(SPECIES, Integer.MIN_VALUE);
        var minVec = IntVector.broadcast(SPECIES, Integer.MAX_VALUE);

        var i = 0;
        var limit = array.length - (array.length % SPECIES.length());
        for (; i < limit; i += SPECIES.length()) { //main loop
            //utiliser les vecteurs
            IntVector vector = IntVector.fromArray(SPECIES, array, i);
            maxVect = maxVect.max(vector);
            minVec = minVec.min(vector);
        }
        var max = maxVect.reduceLanes(VectorOperators.MAX);
        var min = minVec.reduceLanes(VectorOperators.MIN);

        for (; i < array.length; i++) {   // post loop
            if (array[i] > max) {
                max = array[i];
            }

            if (array[i] < min) {
                min = array[i];
            }
        }
        return new int[]{min, max};
    }

}
