
#include <stdio.h>
#include "cost.h"



void plotcost(double x[9], double lo[9], double hi[9]) {
  FILE *arq;

  arq = fopen("datagraph.m","w");

  fprintf(arq,"Z= [ ");

  // produces an octave code for a 2D graph of cost function 
  for (x[6]=lo[6];x[6]<hi[6];x[6]+=0.05) {
    for (x[7]=lo[7];x[7]<hi[7];x[7]+=0.05)
      fprintf(arq,"%8.5f ",cost(x));
    if (x[6]+0.05<hi[6])
      fprintf(arq,";\n");
    else fprintf(arq,"];\n");
  }

  fprintf(arq,"__gnuplot_set__ contour\n");
  fprintf(arq,"__gnuplot_set__ cntrparam levels 20\n");
  fprintf(arq,"gsplot Z;\n");

  fprintf(arq,"pause;\n\n");


  fprintf(arq,"gset terminal postscript\n");
  fprintf(arq,"gset output \"datagraph.ps\"\n");
  fprintf(arq,"gsplot Z\n");

  fprintf(arq,"pause;\n\n");


  fprintf(arq,"gset output\n");
  fprintf(arq,"gset terminal X11\n");
  fprintf(arq,"__gnuplot_set__ pm3d map\n");
  fprintf(arq,"gsplot Z\n\n");



  fprintf(arq,"gset terminal postscript\n");
  fprintf(arq,"gset output \"datagraphpm3d.ps\"\n");
  fprintf(arq,"__gnuplot_set__ palette gray\n");
  fprintf(arq,"gsplot Z\n");

  fprintf(arq,"pause;\n\n");


  fprintf(arq,"gset terminal X11\n");
  fprintf(arq,"%% mesh(Z);\n");
  fclose(arq);

}


