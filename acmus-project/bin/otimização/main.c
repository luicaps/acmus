
#include "defs.h"
#include "allen.h"
#include "cost.h"
#include "optim.h"
#include <time.h>

#include "debug.h"


int main() {
  double x[9],y[9],lo[9],hi[9],optval,inival,time;
  int i,nfeval;

  /* Reads data (from stdin) */

  printf("Room dimensions (X,Y,Z): ");
  scanf("%lf %lf %lf",&x[0],&x[1],&x[2]);
  printf("%g %g %g\n",x[0],x[1],x[2]);
  printf("Source position (X,Y,Z): ");
  scanf("%lf %lf %lf",&x[3],&x[4],&x[5]);
  printf("%g %g %g\n",x[3],x[4],x[5]);
  printf("Listener position (X,Y,Z): ");
  scanf("%lf %lf %lf",&x[6],&x[7],&x[8]);
  printf("%g %g %g\n\n",x[6],x[7],x[8]);
  getchar();

  /* simple test, allows +/-1 for source and listener positions */
  for (i=0;i<9;i++) {
    lo[i] = 0.2;//x[i]-(i>2);
    hi[i] = x[i%3]-0.2;//x[i]+(i>2);
  }

  plotcost(x,lo,hi);return;

  inival = cost(x);

  printf("Nelder-Mead Simplex method:\n");
  for (i=0;i<9;i++)
    y[i] = x[i];

  time = (double)clock();
  NelderMead(y,lo,hi,9,&optval,&nfeval,&cost);
  time -= (double)clock();
  time /= -CLOCKS_PER_SEC;

  printf("initial value = %g\n",inival);
  printf("optimal value = %g\n",optval);
  printf("# func. eval. = %d\n",nfeval);
  printf("time          = %g\n",time);
  printf("solution = ( ");
  for (i=0;i<9;i++)
    printf("%g ",y[i]);
  printf(")\n\n");

  printf("Projected Gradient method:\n");
  for (i=0;i<9;i++)
    y[i] = x[i];

  time = (double)clock();
  ProjectedGradient(y,lo,hi,9,&optval,&nfeval,&cost);
  time -= (double)clock();
  time /= -CLOCKS_PER_SEC;

  printf("initial value = %g\n",inival);
  printf("optimal value = %g\n",optval);
  printf("# func. eval. = %d\n",nfeval);
  printf("time          = %g\n",time);
  printf("solution = ( ");
  for (i=0;i<9;i++)
    printf("%g ",y[i]);
  printf(")\n\n");

  printf("Density Clustering with Gradient method:\n");
  for (i=0;i<9;i++)
    y[i] = x[i];

  time = (double)clock();
  densityClustering(y,lo,hi,9,&optval,&nfeval,&cost);
  time -= (double)clock();
  time /= -CLOCKS_PER_SEC;

  printf("initial value = %g\n",inival);
  printf("optimal value = %g\n",optval);
  printf("# func. eval. = %d\n",nfeval);
  printf("time          = %g\n",time);
  printf("solution = ( ");
  for (i=0;i<9;i++)
    printf("%g ",y[i]);
  printf(")\n\n");

  return 0;

}
