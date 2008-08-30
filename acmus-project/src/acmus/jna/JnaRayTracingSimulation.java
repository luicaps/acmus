package acmus.jna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import acmus.tools.rtt.RandomAcousticSource;
import acmus.tools.structures.NormalSector;
import acmus.tools.structures.Triade;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

public class JnaRayTracingSimulation {

    private JnaTriade[] vectors;
    private JnaNormalSector[] sectors;
    private JnaTriade soundSource;
    private JnaTriade sphericalReceptorCenter;
    private HashMap<Double, Double> sphericalReceptorHistogram;

    double sphericalReceptorRadius;
    double soundSpeed;
    double initialEnergy;
    double mCoeficient;
    double k;

    private static List<NormalSector> generateSectorsFor() {

        ArrayList<NormalSector> result = new ArrayList<NormalSector>();
        double w = 10;
        double h = 10;
        double l = 10;
        result.add(new NormalSector(new Triade(0, 0, 1), new Triade(l, w, 0),
                0.02));
        result.add(new NormalSector(new Triade(0, 1, 0), new Triade(l, 0, h),
                0.02));
        result.add(new NormalSector(new Triade(1, 0, 0), new Triade(0, w, h),
                0.02));
        result.add(new NormalSector(new Triade(0, 0, -1), new Triade(l, w, h),
                0.02));
        result.add(new NormalSector(new Triade(0, -1, 0), new Triade(l, w, h),
                0.02));
        result.add(new NormalSector(new Triade(-1, 0, 0), new Triade(l, w, h),
                0.02));
        return result;
    }

    public static void main(String[] args) {
        final int K = 1000;
        final double INITIAL_ENERGY = 100000;

        List<NormalSector> sectors = generateSectorsFor();
        List<Triade> vectors = new RandomAcousticSource().generate(100000);
        Triade soundSourceCenter = new Triade(2.0, 2.0, 2.0);
        Triade sphericalReceptorCenter = new Triade(8, 8, 8);
        double sphericalReceptorRadius = .1;
        double speedOfSound = 344;
        double mCoeficient = 0.01;

        JnaRayTracingSimulation simulation = new JnaRayTracingSimulation(
                sectors, vectors, soundSourceCenter, sphericalReceptorCenter,
                sphericalReceptorRadius, speedOfSound, INITIAL_ENERGY,
                mCoeficient, K);

        double tempo_inicial = System.currentTimeMillis();
        NativeLibrary.addSearchPath("simulacao", "/var/local/caueguerra/");
        MySharedLibrary lib = (MySharedLibrary) Native.loadLibrary("simulacao",
                MySharedLibrary.class);

        lib.simulate(simulation.soundSpeed, simulation.soundSource,
                simulation.vectors, simulation.initialEnergy,
                simulation.sectors, simulation.mCoeficient,
                simulation.sphericalReceptorCenter,
                simulation.sphericalReceptorRadius, simulation.k, vectors
                        .size(), sectors.size());

        // Map<Double, Double> histogram = simulation
        // .getSphericalReceptorHistogram();

        double tempo_final = System.currentTimeMillis();

        System.out.println("tempo gasto: " + (tempo_final - tempo_inicial)
                + "milisegundos");
    }

    public JnaRayTracingSimulation(List<NormalSector> sectors,
            List<Triade> vectors, Triade soundSourceCenter,
            Triade sphericalReceptorCenter, double sphericalReceptorRadius,
            double soundSpeed, double initialEnergy, double mCoeficient, int k) {
        this.sectors = (JnaNormalSector[]) new JnaNormalSector()
                .toArray(sectors.size());
        JnaNormalSector[] ns = sectors.toArray(new JnaNormalSector[sectors
                .size()]);
        this.vectors = (JnaTriade[]) new JnaTriade().toArray(vectors.size());
        JnaTriade[] t = vectors.toArray(new JnaTriade[vectors.size()]);

        for (int i = 0; i < this.sectors.length; i++) {
            this.sectors[i].absorventCoeficient = ns[i].absorventCoeficient;
            this.sectors[i].iPoint = ns[i].iPoint;
            this.sectors[i].normalVector = ns[i].normalVector;
        }

        for (int i = 0; i < this.vectors.length; i++) {
            this.vectors[i].x = t[i].x;
            this.vectors[i].y = t[i].y;
            this.vectors[i].z = t[i].z;
        }

        this.soundSource = new JnaTriade(soundSourceCenter.getX(),
                soundSourceCenter.getY(), soundSourceCenter.getZ());

        this.sphericalReceptorCenter = new JnaTriade(sphericalReceptorCenter
                .getX(), sphericalReceptorCenter.getY(),
                sphericalReceptorCenter.getZ());

        this.sphericalReceptorRadius = sphericalReceptorRadius;
        this.soundSpeed = soundSpeed;
        this.initialEnergy = initialEnergy;
        this.mCoeficient = mCoeficient;
        this.k = k;

        sphericalReceptorHistogram = new HashMap<Double, Double>();
    }

    public Map<Double, Double> getSphericalReceptorHistogram() {
        return sphericalReceptorHistogram;
    }

    public void histogram() {
        double tMax = 0.0;
        double h1 = 0.0, h2 = 0.0, h3 = 0.0, h4 = 0.0, h5 = 0.0, h6 = 0.0;
        Iterator<Double> itr = sphericalReceptorHistogram.keySet().iterator();
        // controi histograma
        while (itr.hasNext()) {
            Double key = itr.next();
            if (key <= 0.01) {
                h1 += sphericalReceptorHistogram.get(key);
            }
            if (key >= 0.01 && key <= 0.02) {
                h2 += sphericalReceptorHistogram.get(key);
            }
            if (key >= 0.02 && key <= 0.03) {
                h3 += sphericalReceptorHistogram.get(key);
            }
            if (key >= 0.03 && key <= 0.04) {
                h4 += sphericalReceptorHistogram.get(key);
            }
            if (key >= 0.04 && key <= 0.05) {
                h5 += sphericalReceptorHistogram.get(key);
            }
            if (key >= 0.05) {
                h6 += sphericalReceptorHistogram.get(key);
            }

            tMax = sphericalReceptorHistogram.get(key);
        }

        System.out.println("0,01 : " + h1 / tMax);
        System.out.println("0,02 : " + h2 / tMax);
        System.out.println("0,03 : " + h3 / tMax);
        System.out.println("0,04 : " + h4 / tMax);
        System.out.println("0,05 : " + h5 / tMax);
        System.out.println("0,06 : " + h6 / tMax);

    }

}
