
#include "defs.h"
#include "allen.h"
#include "filters.h"


/* *******************************************************************************
   standard deviation of FR (according to D'Antonio) */

double stddev(double *FR, int M) {
  int i;
  double mean,sd;

  for (i=0,mean=0; i<M; i++)
    mean += FR[i];
  mean /= M;

  for (i=0,sd=0; i<M; i++)
    sd += pow(FR[i]-mean,2);

  return sqrt(sd/(M-1));

} /* double sd(...) */







/* *******************************************************************************
   cost: according to D'Antonio
   performs simulation of IR using Allen & Berkley,
   transforms into long-term and short-term spectra,
   and computes standard deviation */

double cost(double x[9]) {
  static int FIRSTCALL = 1;
  static gnuplot_ctrl *gnuplot_h1, *gnuplot_h2; /* gnuplot window handles */
  static double *IR, /* impulse response */
    *IR1, /* impulse response (left channel) */
    *IR2, /* impulse response (right channel) */
    *Times, /* time values (for plotting) */
    *FR[2]; /* frequency response.
               FR[0][i]=frequency
               FR[1][i]=magnitude in dB */
  static fftw_complex *FOURIER; /* auxiliary vector for computing FFT of IR */
  static sound_ray *ray_list1, /* saves information about individual rays coming from
			   all possible virtual sources */
    *ray_list2;
  static double room[3],  /* room dimensions */
    sourceL[3], listener[3], /* positions of source and listener */
    sourceR[3],
    abs_coefs[6], /* absorption coefficients for each room wall */
    freq_range[2], /* frequency range for computing FR */
    t1, t2, time, /* used for performance measures */
    radius, /* radius for considering virtual rooms */
    IRlength, /* maximum delay (in sec) of a ray coming from a virtual room */
    room_volume,
    total_volume, /* used for determining the size of the ray_list */
    cost,
    delay0; /* delay of direct sound (for both sources) */
  static int N,M, /* sizes of Impulse Response and Frequency Response */
    nrays1, nrays2, maxrays, /* size (and limit) for ray_list */
    maxwinsize, /* limit for impulse response */
    i,j;

  /* separates input vector in room, source and listener */
  for (i=0; i<3; i++) room[i] = x[i];
  for (i=0; i<3; i++) sourceL[i] = x[i+3];
  for (i=0; i<3; i++) listener[i] = x[i+6];
  
  /* second loudspeaker is mirrored in Y-axis */
  sourceR[0]=sourceL[0];sourceR[1]=room[1]-sourceL[1];sourceR[2]=sourceL[2];

  /* define important values (only first call) */
  if (FIRSTCALL) {
    FIRSTCALL = 0;

    /* constant absorption coefficient (as in D'Antonio) */
    abs_coefs[0]=abs_coefs[1]=abs_coefs[2]=abs_coefs[3]=abs_coefs[4]=abs_coefs[5]=0.12;

    /* radius for simultating IR */
    radius = NREFLECTIONS*room[0]; /* after D'Antonio */
    IRlength = radius/SND_SPEED;
    /* prevents too small transforming windows */
    if (IRlength<MINIRLENGTH) IRlength=MINIRLENGTH;
    /* or the other way around: first define IRlength then radius=IRlength*SND_SPEED; */

    /* other values derived from these: */
    total_volume = (4.0/3)*M_PI*pow(radius,3);
    room_volume = room[0]*room[1]*room[2];
    maxrays = ceil(total_volume/room_volume);

    /* maxwinsize is the next power-of-2 where IR fits */
    maxwinsize = pow(2,ceil(log(IRlength*SMPL_RATE)/M_LN2));

#ifdef DEBUG
    printf("radius = %5.2f,\tIRlength = %5.2f,\tmaxwinsize = %d\n",radius,IRlength,maxwinsize);
#endif

    /* allocates memory for all vectors */
    ray_list1 = (sound_ray*)malloc(maxrays*sizeof(sound_ray));
    ray_list2 = (sound_ray*)malloc(maxrays*sizeof(sound_ray));
    FR[0] = (double*)malloc(maxwinsize*sizeof(double));
    FR[1] = (double*)malloc(maxwinsize*sizeof(double));
    IR = (double*)fftw_malloc(maxwinsize*sizeof(double));
    IR1 = (double*)fftw_malloc(maxwinsize*sizeof(double));
    IR2 = (double*)fftw_malloc(maxwinsize*sizeof(double));
    Times = (double*)fftw_malloc(maxwinsize*sizeof(double));
    FOURIER = (fftw_complex*) fftw_malloc(maxwinsize*sizeof(fftw_complex));
    if (ray_list1==NULL || ray_list2==NULL || FR==NULL || IR==NULL || FOURIER==NULL) {
      printf("Not enough memory!\n");
      exit(0);
    }
    
#ifdef DEBUG
    /* initialize gnuplot handles for graphics */
    gnuplot_h1 = gnuplot_init();
    gnuplot_setstyle(gnuplot_h1, "impulses") ;
    gnuplot_h2 = gnuplot_init();
    gnuplot_setstyle(gnuplot_h2, "lines") ;
#endif
  }

  /* simulates ray reflections in the room */
  t1=(double)clock();
  nrays1 = create_rays(ray_list1,maxrays,radius,room,abs_coefs,sourceL,listener);
  /* creates mirror source */
  nrays2 = create_rays(ray_list2,maxrays,radius,room,abs_coefs,sourceR,listener);
  t2=(double)clock();
  time=(t2-t1)/CLOCKS_PER_SEC;
#ifdef DEBUG
  printf("maxrays = %d,\t nrays = %d\n",maxrays,nrays1);
  printf("createRAYS:\ttime=%5.2f\n",time);
#endif

  /* creates Impulse Response */
  N=maxwinsize;
  for (i=0; i<N; i++)
    Times[i] = (double)i/SMPL_RATE;
  t1=(double)clock();
  create_ImpulseResponse(IR1,N,ray_list1,nrays1,abs_coefs);
  create_ImpulseResponse(IR2,N,ray_list2,nrays2,abs_coefs);
  t2=(double)clock();
  time=(t2-t1)/CLOCKS_PER_SEC;
#ifdef DEBUG
  printf("createIR:\ttime=%5.2f\n",time);
#endif

  /* sums IR from both sources, multiplying by window */
  for (i=0;i<N;i++)
#ifdef STEREO
    IR[i] = (IR1[i]+IR2[i]);
#else
    IR[i] = IR1[i];
#endif

#ifdef HIPASS
  hipass(IR,N,HIPASS); /* hipass filter according to Allen & Berkley */
#endif
#ifdef LOWPASS
  lowpass(IR,N,LOWPASS); /* cuts components above Nyquist frequency */
#endif
  /* Applies Hamming window for transforming */
  for (i=0;i<N;i++)
    IR[i] = IR[i]*window(i,N,WINDOW);
#ifdef DEBUG
  //gnuplot_plot_xy(gnuplot_h1, Times, IR, IRPORTION*N, "long-term IR");
#endif

  /* creates Frequency Response */
  freq_range[0]=FRLOW;freq_range[1]=FRHIGH;
  t1=(double)clock();
  M = create_FrequencyResponse (FR,IR,N,FOURIER,freq_range,1);
  t2=(double)clock();
  time=(t2-t1)/CLOCKS_PER_SEC;
#ifdef DEBUG
  printf("createFR:\ttime=%5.2f\n",time);
  gnuplot_plot_xy(gnuplot_h2, FR[0], FR[1], M, "long-term FR");
#endif

  /* computes cost function (distance from flat response) */
  cost = stddev(FR[1],M);

  /* same thing for 64ms after arrival of direct sound */
  delay0 = eucl_dist(sourceL,listener,3)/SND_SPEED;
  N = (delay0+0.064)*SMPL_RATE;
  /* Applies squared cosine window as in D'Antonio */
  sqrcoswindow(IR,N,(delay0+0.032)*SMPL_RATE);
  /* creates Frequency Response */
  t1=(double)clock();
  M = create_FrequencyResponse (FR,IR,N,FOURIER,freq_range,0);
  t2=(double)clock();
  time=(t2-t1)/CLOCKS_PER_SEC;
#ifdef DEBUG
  printf("createFR:\ttime=%5.2f\n",time);
  gnuplot_plot_xy(gnuplot_h1, Times, IR, N, "short-term IR");
  gnuplot_plot_xy(gnuplot_h2, FR[0], FR[1], M, "short-term FR");
#endif

  /* computes cost function (distance from flat response) */
  cost += stddev(FR[1],M);

  return 0.5*cost;

  //gnuplot_close(gnuplot_h1);
  //gnuplot_close(gnuplot_h2);

  //free(ray_list1);
  //free(ray_list2);
  //free(FR[0]); free(FR[1]);
  //fftw_free(IR);
  //fftw_free(IR1);
  //fftw_free(IR2);
  //fftw_free(FOURIER);

}


