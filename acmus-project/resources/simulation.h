#ifndef simulation_H
#define simulation_H

typedef struct _JnaTriade {
	int x, y, z;
	char* nome;
} JnaTriade;

typedef struct _JnaNormalSector {
	JnaTriade normalVector;
	double absorventCoeficient;
	JnaTriade iPoint;
} JnaNormalSector;

double produtoEscalar(JnaTriade t1, JnaTriade t2);
JnaTriade sub(JnaTriade t1, JnaTriade t2);
JnaTriade sum(JnaTriade t1, JnaTriade t2) ;
JnaTriade multiplicaVetorEscalar(JnaTriade t1, double esc);
double modulo(JnaTriade t);

void simulate(double soundSpeed, JnaTriade* soundSource, JnaTriade vectors[],
                double initialEnergy, JnaNormalSector sectors[],
                double mCoeficient, JnaTriade* sphericalReceptorCenter,
                double sphericalReceptorRadius,
                double k, int vecSize, int secSize);
#endif
