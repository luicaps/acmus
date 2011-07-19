
#include <stdlib.h>
#include <stdio.h>
#include <math.h>

#define TINY 1e-10
#define SMALL 1e-5
#define NMAX 500
#define INITSTEP 1
#define SIGMA 1e-5
#define BETA   0.5



/* *******************************************************************************
   NMtry: auxiliary function for Nelder-Mead simplex method.
   See Numerical Recipes in C for details
 */ 

double NMtry (double **p, double y[], double psum[], double *lo, double *hi,
	       int ndim, int ihi, double fac, double (*cost)(double[])) {
  int j,feas;
  double fac1, fac2, ytry;
  static double *ptry=NULL;

  if (ptry==NULL)
    ptry = (double*)malloc(ndim*sizeof(double));
  fac1 = (1.0-fac)/ndim;
  fac2 = fac1 - fac;
  for (feas=1,j=0;j<ndim;j++) {
    ptry[j] = psum[j]*fac1-p[ihi][j]*fac2;
    if (ptry[j]<lo[j]-TINY || ptry[j]>hi[j]+TINY)
      feas=0;
  }
  ytry = feas?(*cost)(ptry):1.0/TINY;
  if (ytry<y[ihi]) {
    y[ihi] = ytry;
    for (j=0;j<ndim;j++) {
      psum[j] += ptry[j]-p[ihi][j];
      p[ihi][j] = ptry[j];
    }
  }
  return ytry;
}


/* *******************************************************************************
   NelderMead: simplex method for minimizing a function (*cost).
   x is the starting point, lo and hi are bounds on the variables, and optval and
   nfeval are return arguments with the optimal value and number of function
   evaluations.
 */

void NelderMead (double *x, double *lo, double *hi, int ndim,
		 double* optval, int* nfeval, double (*cost)(double[])) {
  static double **p=NULL, *y, *psum;
  double ftol=TINY, rtol, sum, swap, ysave, ytry;
  int i, ihi, ilo, inhi, j;

  /* allocates memory for vectors and matrices (first call only) */
  if (p==NULL) {
    p = (double **)malloc((ndim+1)*sizeof(double *));
    for (i=0;i<ndim+1;i++)
      p[i] = (double*)malloc(ndim*sizeof(double));
    psum = (double*)malloc(ndim*sizeof(double));
    y = (double*)malloc((ndim+1)*sizeof(double));
  }
  
  /* initializes matrix p and vector y */
  for (i=0; i<ndim; i++)
    for (j=0; j<ndim+1; j++)
      p[j][i] = x[i];
  y[0] = (*cost)(x);
  for (i=0; i<ndim; i++) {
    if (x[i]<hi[i]-TINY)
      p[i+1][i] = 0.01*(x[i]+hi[i]);
    else if (x[i]>lo[i]+TINY)
      p[i+1][i] = 0.01*(x[i]+lo[i]);
    else p[i+1][i] = x[i];
    y[i+1] = cost(p[i+1]);
  }

  /* initialize # of function evaluations and psum (used by NMtry) */
  *nfeval = 1;
  for (j=0;j<ndim;j++) {
    for (sum=0,i=0;i<ndim+1;i++)
      sum += p[i][j];
    psum[j] = sum;
  }
  /* main loop (termination criteria below) */
  for (;;) {
    /* finds the lowest point, highest point and next-to-highest point */
    ilo=0;
    ihi = (y[0]>y[1])?(inhi = 1,0):(inhi=0,1);
    for (i=0;i<ndim+1;i++) {
      if (y[i]<=y[ilo]) ilo = i;
      if (y[i]>y[ihi]) {
	inhi = ihi;
	ihi = i;
      }
      else if (y[i]>y[inhi] && i!=ihi)
	inhi = i;
    }
    /* termination criteria: either function values are practically the same,
       or a maximum number of iterations has been reached */
    rtol = 2.0*fabs(y[ihi]-y[ilo])/(fabs(y[ihi])+fabs(y[ilo])+TINY);
    if (rtol<ftol || *nfeval>=NMAX) {
      swap = y[0]; y[0] = y[ilo]; y[ilo] = swap;
      for (i=0;i<ndim;i++) {
	swap = p[0][i]; p[0][i] = p[ilo][i]; p[ilo][i] = swap;
	x[i] = p[0][i];
      }
      *optval = y[0];
      break;
    }
    *nfeval += 2;
    /* tries to replace the highest point for a candidate on the other side */
    ytry = NMtry(p,y,psum,lo,hi,ndim,ihi,-1.0,cost);
    /* if this is better than the best point, stretches a little more */
    if (ytry<=y[ilo])
      ytry = NMtry(p,y,psum,lo,hi,ndim,ihi,2.0,cost);
    /* if this is worse than the worst point left, contracts */
    else if (ytry>=y[inhi]) {
      ysave = y[ihi];
      ytry = NMtry(p,y,psum,lo,hi,ndim,ihi,0.5,cost);
      /* if it gets worse, contracts around the lowest point */
      if (ytry>=ysave) {
	for (i=0; i<ndim+1; i++) {
	  if (i!=ilo) {
	    for (j=0;j<ndim;j++)
	      p[i][j] = psum[j] = 0.5*(p[i][j]+p[ilo][j]);
	    y[i] = cost(psum);
	  }
	}
	*nfeval += ndim;
	for (j=0;j<ndim;j++) {
	  for (sum=0,i=0;i<ndim+1;i++)
	    sum += p[i][j];
	  psum[j] = sum;
	}
      }
    }
    else --(*nfeval);
  }
}





/* *******************************************************************************
   pgrad: computes the gradient of the cost function at point x
          projected on the bounding box defined by lo and hi.
 */

void pgrad (double *x, double *lo, double *hi, double *grad, int ndim, double fval,
	    int *nfeval, double (*cost)(double[])) {
  static double y[9];
  int i;

  for (i=0; i<ndim; i++) y[i] = x[i];
  for (i=0; i<ndim; i++) {
    if (lo[i]>hi[i]-TINY) {
      grad[i] = 0.0;
      continue;
    }
    (*nfeval)++;
    if (x[i]<hi[i]-SMALL) {
      y[i] += SMALL;
      grad[i] = ((*cost)(y)-fval)/SMALL;
      y[i] = x[i];
      if (-grad[i]<-SMALL && x[i]<lo[i]+SMALL)
	grad[i] = 0;
    }
    else {
      y[i] -= SMALL;
      grad[i] = -((*cost)(y)-fval)/SMALL;
      y[i] = x[i];
      if (-grad[i]>SMALL)
	grad[i] = 0;
    }
  }

}




/* *******************************************************************************
   ProjectedGradient: minimizes a function (*cost) starting at point x using steepest
                      descent restricted to a bounding box defined by lo and hi.
		      optval returns the optimal value and nfeval returns the
		      number of function evaluations.
 */

void ProjectedGradient (double *x, double *lo, double *hi, int ndim,
			double* optval, int* nfeval, double (*cost)(double[])) {
  static double grad[9],xtry[9];
  double fval,newval,step,beta_m,min,norm;
  int i,nos;

  /* computes cost of x and initializes # of function evaluations */
  newval = (*cost)(x);
  *nfeval = 1;
  nos = 0;

  do {
    fval = newval;
    /* computes projected gradient */
    pgrad(x,lo,hi,grad,ndim,fval,nfeval,cost);
    /* computes maximum stepsize and norm of gradient */
    min = INITSTEP;
    norm=0;
    for (i=0; i<ndim; i++) {
      if (-grad[i]>TINY) {
        step = -(hi[i]-x[i])/grad[i];
	min = (step<min)?step:min;
      }
      if (-grad[i]<-TINY) {
        step = -(lo[i]-x[i])/grad[i];
	min = (step<min)?step:min;
      }
      norm += grad[i]*grad[i];
    }
    step = min;
    /* when projected gradient is close to zero, we have a local minimum */
    if (norm<TINY)
      break;
    /* uses Armijo line-search rule */
    beta_m = 1.0/BETA;
    do {
      beta_m *= BETA;
      for (i=0; i<ndim; i++) {
	xtry[i] = x[i]-beta_m*step*grad[i];
	if (xtry[i]<lo[i]-TINY || xtry[i]>hi[i]+TINY)
	  printf("!!!!!!!!!!!!!!!!!!!!!\n");
      }
      newval = (*cost)(xtry);
      (*nfeval)++;
    } while (fval-newval<SIGMA*beta_m*norm);
    /* copies new point into x */
    if (newval<fval-TINY)
      for (i=0; i<ndim; i++)
	x[i] = xtry[i];
    //printf("outer steps=%d\n",++nos);
  } while (*nfeval<NMAX && fabs(fval-newval)>TINY && norm>TINY);

  //printf("norm=%g\n",norm);

  *optval = fval;

}

