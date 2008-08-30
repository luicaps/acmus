#include <stdio.h>
#include <math.h>
#include "simulacao.h"

void simulate(double soundSpeed, JnaTriade* soundSource, JnaTriade vectors[],
                double initialEnergy, JnaNormalSector sectors[],
                double mCoeficient, JnaTriade* sphericalReceptorCenter,
                double sphericalReceptorRadius,
                double k, int vecSize, int secSize) {

	JnaTriade q = *soundSource;
	JnaTriade g;
	JnaTriade v;
	JnaTriade nR;
	double e;
	double lMin = 0.0;
	double dMin = 0.0;
	double alpha = 0.0;
	double lReflection;
	double eTemp;
	int uhu = 0;
	int i, j;

    /* reflection */
    for (i = 0; i < vecSize; i++) {
        JnaTriade vTemp = vectors[i];
            q = *soundSource;
            v = vTemp;
            e = initialEnergy;
            lReflection = 0; /*acumulador de distancia percorrida
            // pelo raio
            // reflexoes do raio
            // teste de qual direcao o raio vai seguir*/
            do {
                uhu++;
                /*notar que V jah estah normalizado*/
                g = q;
                /* correcao no raio...*/
                lMin = 1.7E300; /* this number is our MAX constant*/

                /* verificacao de qual setor(parede) o raio incide*/
				for (j = 0; j < secSize; j++) {
                    /*System.out.println("k#");*/

                    if (produtoEscalar(v, sectors[j].normalVector) >= 0) {
                        continue;
                    } else {
						double d = produtoEscalar(sectors[j].normalVector, sub(sectors[j].iPoint, g));
                        double l = -1 * d / produtoEscalar(v, sectors[j].normalVector);
						/*
                        // testa distancia minima da fonte a parede e ve
                        // se eh
                        // minima, dentre outras
                        // paredes
                        // este teste determina em qual parede o raio
                        // "bate" */
                        if (l <= lMin) {
                            lMin = l;
                            dMin = d;
                            alpha = sectors[j].absorventCoeficient;
                            nR = sectors[j].normalVector;
                        }
                    }
                }/* fim setores */
                q = sum(g, multiplicaVetorEscalar(v, lMin));
                eTemp = e * (1 - alpha)
                        * pow(2.71828183, -1 * mCoeficient * lMin);

					/*
                //
                // desenha o raio
                //

                //
                // verifica se este raio intercepta o receptor esferico
                // TODO corrigir estes calculos que estao errados, pois
                // ocorre
                // um caso em que
                // delta = 2 e na verdade o raio nao intercepta a esfera */
                {
                    JnaTriade oc = sub(g, *sphericalReceptorCenter);
                    double l2oc = produtoEscalar(oc, oc);
                    double tca = produtoEscalar(oc, v);

                    /* o raio intercepta o receptor esferico */
                    if (tca >= 0) {
                        double t2hc = pow(sphericalReceptorRadius, 2)
                                - l2oc + pow(tca, 2);
                        if (t2hc > 0) {
                            /* System.out.println("INTERCEPTA"); */
                            double lThisReflection = tca - sqrt(t2hc);

                            double distance = lReflection + lThisReflection;
                            double time = distance / soundSpeed;
                            double eSphere = e
                                    * (1 - alpha)
                                    * pow(2.71828183, -1 * mCoeficient
                                            * lThisReflection);
                            /*if (sphericalReceptorHistogram.containsKey(time)) {
                                double temp = sphericalReceptorHistogram
                                        .get(time);
                                sphericalReceptorHistogram.put(time, temp
                                        + eSphere);
                                // System.out.println("t: " + time + "e:
                                // " + temp
                                // + eSphere);
                            } else {
                                sphericalReceptorHistogram.put(time, eSphere);
                                // System.out.println("t: " + time + "e:
                                // "
                                // + eSphere);
                            } */ /* tenho que tirar */
                        }
                    }
                }
                lReflection += lMin;
                e = eTemp;
                v = sum(multiplicaVetorEscalar(nR, 2 * dMin), sub(g, q));
                v = multiplicaVetorEscalar(v, 1 / modulo(v));/* AGORA
                // TENHO
                // QUE
                // NORMALIZAR o
                // vetor V*/

            } while (e > (1 / k * initialEnergy)); /* vai para a
            // proxima
            // reflexao, caso
            // a energia seja maior do que o criterio de parada*/

        }/* fim for, vetores*/
		printf("UHU: %d", uhu);
    }

double produtoEscalar(JnaTriade t1, JnaTriade t2) {
	return t1.x * t2.x + t1.y * t2.y + t1.z * t2.z;
}

JnaTriade sub(JnaTriade t1, JnaTriade t2) {
	JnaTriade res;
	res.x = t2.x - t1.x;
	res.y = t2.y - t1.y;
	res.z = t2.z - t1.z;
	return res;
}

JnaTriade sum(JnaTriade t1, JnaTriade t2) {
	JnaTriade res;
	res.x = t1.x + t2.x;
	res.y = t1.y + t2.y;
	res.z = t1.z + t2.z;
	return res;
}

JnaTriade multiplicaVetorEscalar(JnaTriade t1, double esc) {
	JnaTriade res;
	res.x = t1.x * esc;
	res.y = t1.y * esc;
	res.z = t1.z * esc;
	return res;
}

double modulo(JnaTriade t) {
	return sqrt(pow(t.x, 2) + pow(t.y, 2) + pow(t.z, 2));
}
