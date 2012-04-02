
#include "defs.h"


/* *******************************************************************************
   Applies a squared cosine window from START to N
 */

void sqrcoswindow (double *IR, int N, int START) {
 int i;

 for (i=START; i<N; i++)
   IR[i] *= pow(cos((i-START)*M_PI/(2.0*(N-START))),2);

}

/* *******************************************************************************
   Several windows for transforming IR into FR
 */
double window(int i, int N, int type) {
  if (type>10) {
    if (i<N/2) return 1;
    else type -= 10;
  }
  switch (type) {
  case 0: return 1; /* rectangular */
  case 1: return (i<N/2)?i/(N/2.0):(N-i)/(N/2.0); /* triangular */
  case 2: return sin((M_PI*i)/(N-1.0)); /* sine, non-differentiable */
  case 3: return pow(sin((M_PI*i)/(N-1.0)),2); /* squared sine, diff. */
  case 4: return 0.54-0.46*cos(i*2.0*M_PI/(N-1.0)); /* Hamming */
  case 5: return 0.5-0.5*cos(i*2.0*M_PI/(N-1.0)); /* Hanning */
  case 6: return 0.42-0.5*cos(i*2.0*M_PI/(N-1.0))+0.08*cos(i*4.0*M_PI/(N-1.0));
  default: return 0;
  }
}

/* *******************************************************************************
   2-pole 2-zero HiPass filter with cutoff frequency cutfreq (Hz)
   as in Allen & Berkley
 */
void hipass(double *IR, int N, double cutfreq) {
  double R,phi,a1,a2,b1,b2,x0,y0,y1,y2;
  int i;

  phi = 2.0*M_PI*cutfreq/SMPL_RATE;
  R = exp(-phi);
  b1 = 2.0*R*cos(phi);
  b2 = -R*R;
  a1 = -(1.0+R);
  a2 = R;
  y0 = y1= y2 = 0;
  for (i=0;i<N;i++) {
    x0 = IR[i];
    IR[i] = y0+a1*y1+a2*y2;
    y2 = y1;
    y1 = y0;
    y0 = x0+b1*y1+b2*y2;
  }

}

/* *******************************************************************************
   2-pole 2-zero LowPass filter with cutoff frequency cutfreq (Hz)
 */
void lowpass(double *IR, int N, double cutfreq) {
  double R,phi,a1,a2,b1,b2,x0,y0,y1,y2;
  int i;

  phi = 2.0*M_PI*cutfreq/SMPL_RATE;
  R = exp(-phi);
  b1 = 2.0*R*cos(phi);
  b2 = -R*R;
  a1 = (1.0+R);
  a2 = R;
  y0 = y1= y2 = 0;
  for (i=0;i<N;i++) {
    x0 = IR[i];
    IR[i] = y0+a1*y1+a2*y2;
    y2 = y1;
    y1 = y0;
    y0 = x0+b1*y1+b2*y2;
  }

}

