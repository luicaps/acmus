package acmus.jna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import acmus.tools.rtt.MonteCarloRandomAcousticSource;
import acmus.tools.structures.NormalSector;
import acmus.tools.structures.Vector;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

public class JnaRayTracingSimulation {

    private JnaTriade[] jnaVectors;
    private JnaNormalSector[] jnaSectors;
    private JnaTriade jnaSoundSource;
    private JnaTriade jnaSphericalReceptorCenter;
    private HashMap<Double, Double> sphericalReceptorHistogram;

    double sphericalReceptorRadius;
    double soundSpeed;
    double initialEnergy;
    double mCoeficient;
    double k;

    private static List<NormalSector> generateSectorsFor() {

        ArrayList<NormalSector> result = new ArrayList<NormalSector>();
        float w = 10;
        float h = 10;
        float l = 10;
        result.add(new NormalSector(new Vector(0, 0, 1), new Vector(l, w, 0),
                0.02));
        result.add(new NormalSector(new Vector(0, 1, 0), new Vector(l, 0, h),
                0.02));
        result.add(new NormalSector(new Vector(1, 0, 0), new Vector(0, w, h),
                0.02));
        result.add(new NormalSector(new Vector(0, 0, -1), new Vector(l, w, h),
                0.02));
        result.add(new NormalSector(new Vector(0, -1, 0), new Vector(l, w, h),
                0.02));
        result.add(new NormalSector(new Vector(-1, 0, 0), new Vector(l, w, h),
                0.02));
        return result;
    }

    public static void main(String[] args) {
        double tempo_inicial = System.currentTimeMillis();

        final int K = 1000;
        final double INITIAL_ENERGY = 100000;

        List<NormalSector> sectors = generateSectorsFor();
        List<Vector> vectors = new MonteCarloRandomAcousticSource()
                .generate(300000);
        Vector soundSourceCenter = new Vector(2.0f, 2.0f, 2.0f);
        Vector sphericalReceptorCenter = new Vector(8, 8, 8);
        double sphericalReceptorRadius = .1;
        double speedOfSound = 344;
        double mCoeficient = 0.01;

        JnaRayTracingSimulation simulation = new JnaRayTracingSimulation(
                sectors, vectors, soundSourceCenter, sphericalReceptorCenter,
                sphericalReceptorRadius, speedOfSound, INITIAL_ENERGY,
                mCoeficient, K);

        NativeLibrary.addSearchPath("simulation", "/var/local/caueguerra/");
        MySharedLibrary lib = (MySharedLibrary) Native.loadLibrary(
                "simulation", MySharedLibrary.class);

        double tempo_parcial = System.currentTimeMillis();

        System.out.println("tempo gasto para carregar: "
                + (tempo_parcial - tempo_inicial) + "milisegundos");

        lib.simulate(simulation.soundSpeed, simulation.jnaSoundSource,
                simulation.jnaVectors, simulation.initialEnergy,
                simulation.jnaSectors, simulation.mCoeficient,
                simulation.jnaSphericalReceptorCenter,
                simulation.sphericalReceptorRadius, simulation.k, vectors
                        .size(), sectors.size());

        // Map<Double, Double> histogram = simulation
        // .getSphericalReceptorHistogram();

        double tempo_final = System.currentTimeMillis();

        System.out.println("tempo gasto total: "
                + (tempo_final - tempo_inicial) + "milisegundos\n");
    }

    private List<JnaNormalSector> normalSectorConverter(
            List<NormalSector> normalSectors) {
        List<JnaNormalSector> jnaNs = new ArrayList<JnaNormalSector>();

        for (NormalSector ns : normalSectors) {
            JnaNormalSector t = new JnaNormalSector();
            t.absorventCoeficient = ns.absorptionCoeficient;
            t.iPoint = new JnaTriade(ns.iPoint.getX(), ns.iPoint.getY(),
                    ns.iPoint.getZ());
            t.normalVector = new JnaTriade(ns.normalVector.getX(),
                    ns.normalVector.getY(), ns.normalVector.getZ());

            jnaNs.add(t);
        }

        return jnaNs;
    }

    private List<JnaTriade> triadeConverter(List<Vector> triades) {
        List<JnaTriade> jnaT = new ArrayList<JnaTriade>();

        for (Vector t : triades) {
            JnaTriade triade = new JnaTriade(t.getX(), t.getY(), t.getZ());
            jnaT.add(triade);
        }

        return jnaT;
    }

    public JnaRayTracingSimulation(List<NormalSector> sectorsList,
            List<Vector> vectorsList, Vector soundSourceCenter,
            Vector sphericalReceptorCenter, double sphericalReceptorRadius,
            double soundSpeed, double initialEnergy, double mCoeficient, int k) {

        List<JnaNormalSector> jnaSectorsList = normalSectorConverter(sectorsList);
        List<JnaTriade> jnaVectorsList = triadeConverter(vectorsList);

        this.jnaSectors = (JnaNormalSector[]) new JnaNormalSector()
                .toArray(jnaSectorsList.size());
        JnaNormalSector[] ns = jnaSectorsList
                .toArray(new JnaNormalSector[jnaSectorsList.size()]);
        this.jnaVectors = (JnaTriade[]) new JnaTriade().toArray(jnaVectorsList
                .size());
        JnaTriade[] t = jnaVectorsList.toArray(new JnaTriade[jnaVectorsList
                .size()]);

        for (int i = 0; i < this.jnaSectors.length; i++) {
            this.jnaSectors[i].absorventCoeficient = ns[i].absorventCoeficient;
            this.jnaSectors[i].iPoint = ns[i].iPoint;
            this.jnaSectors[i].normalVector = ns[i].normalVector;
        }

        for (int i = 0; i < this.jnaVectors.length; i++) {
            this.jnaVectors[i].x = t[i].x;
            this.jnaVectors[i].y = t[i].y;
            this.jnaVectors[i].z = t[i].z;
        }

        this.jnaSoundSource = new JnaTriade(soundSourceCenter.getX(),
                soundSourceCenter.getY(), soundSourceCenter.getZ());

        this.jnaSphericalReceptorCenter = new JnaTriade(sphericalReceptorCenter
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
