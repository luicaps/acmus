#ifndef DENSITY_H_
#define DENSITY_H_

void densityClustering(double * x, double * lo, double * hi, int ndim, 
						double* optval, int* nfeval, double (*cost)(double[]));


#endif /*DENSITY_H_*/
