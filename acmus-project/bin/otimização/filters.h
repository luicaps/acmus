

/* *******************************************************************************
   Applies a squared cosine window from START to N
 */

void sqrcoswindow (double *IR, int N, int START);

/* *******************************************************************************
   Several windows for transforming IR into FR
 */

double window(int i, int N, int type);

/* *******************************************************************************
   2-pole 2-zero HiPass filter with cutoff frequency cutfreq (Hz)
   as in Allen & Berkley
 */

void hipass(double *IR, int N, double cutfreq);

/* *******************************************************************************
   2-pole 2-zero LowPass filter with cutoff frequency cutfreq (Hz)
 */

void lowpass(double *IR, int N, double cutfreq);

