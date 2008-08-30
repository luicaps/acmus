package acmus.jna;

import com.sun.jna.Library;

public interface MySharedLibrary extends Library {
    void simulate(double soundSpeed, JnaTriade soundSource,
            JnaTriade[] vectors, double initialEnergy,
            JnaNormalSector[] sectors, double mCoeficient,
            JnaTriade sphericalReceptorCenter, double sphericalReceptorRadius,
            double k, int vecSize, int secSize);
}
