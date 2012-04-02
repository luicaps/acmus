



/* *******************************************************************************
   NelderMead: simplex method for minimizing a function (*cost).
   x is the starting point, lo and hi are bounds on the variables, and optval and
   nfeval are return arguments with the optimal value and number of function
   evaluations.
 */

void NelderMead (double *x, double *lo, double *hi, int ndim,
		 double* optval, int* nfeval, double (*cost)(double[]));



/* *******************************************************************************
   ProjectedGradient: minimizes a function (*cost) starting at point x using steepest
                      descent restricted to a bounding box defined by lo and hi.
		      optval returns the optimal value and nfeval returns the
		      number of function evaluations.
 */

void ProjectedGradient (double *x, double *lo, double *hi, int ndim,
			double* optval, int* nfeval, double (*cost)(double[]));


