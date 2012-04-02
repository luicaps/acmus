
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>
#include <fftw3.h>
#include "gnuplot_i.h"

#define EPS       1e-10

#define SND_SPEED   340
#define SMPL_RATE 44100
#define FRLOW        20
#define FRHIGH      300
#define IRPOW         2

#define IR_BY_REFLECTIONS
#define NREFLECTIONS 15
#define MINIRLENGTH 0.4
#define IRPORTION 0.5
#define STEREO
#define HIPASS       20
#define LOWPASS    20000
#define WINDOW 0

#define ZETA 5

//#define DEBUG

