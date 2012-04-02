
#include "defs.h"
#include "allen.h"



/* *******************************************************************************
   Function eucl_dist: euclidean distance between points in R^N */

double eucl_dist(double *x, double *y, int N) {
  double dist;
  int i;

  for (i=0,dist=0; i<N; i++)
    dist += pow(x[i]-y[i],2);

  dist = sqrt(dist);

  return dist;

}



/* *******************************************************************************
   Function create_rays: creates a list of sound rays.
   number of rays is determined by maximum radius (in meters)
   returns the number of sound rays */

int create_rays (sound_ray *ray_list, int maxrays,
                 double radius,
                 double *room, double *abs_coefs,
                 double *source, double *listener) {

    int i[3],imax[3],isign[3],j,nrays;
    double dist,virt_src[3];

    /* counts number of rays */
    nrays=0;
    
    /* enumerates every room within given radius,
       using indices i[0], i[1] and i[2]
       (for X, Y, and Z directions) */

    /* maximum room index i[0] */
    imax[0] = radius/room[0];
    isign[0]=(imax[0]%2==0)?1:-1; /* isign[0] == pow(-1,i[0]) */
    /* sweeps room index i[0] */
    for (i[0]=-imax[0]; i[0]<imax[0]; i[0]++,isign[0]=-isign[0]) {

      /* maximum room index i[1], given i[0] */
#ifdef IR_BY_REFLECTIONS
      imax[1] = radius/room[0]-abs(i[0]); /* ensures constant number of reflections */
#else
      imax[1] = sqrt(radius*radius-i[0]*i[0]*room[0]*room[0])/room[1];
#endif
      isign[1]=(imax[1]%2==0)?1:-1; /* isign[1] == pow(-1,i[1]) */
      /* sweeps room index i[1] */
      for (i[1]=-imax[1]; i[1]<imax[1]; i[1]++,isign[1]=-isign[1]) {

        /* maximum room index i[2], given i[0],i[1] */
#ifdef IR_BY_REFLECTIONS
	imax[2] = radius/room[0]-abs(i[0])-abs(i[1]);
#else
	imax[2] = sqrt(radius*radius-i[0]*i[0]*room[0]*room[0]
		       -i[1]*i[1]*room[1]*room[1])/room[2];
#endif
        isign[2] = (imax[2]%2==0)?1:-1; /* isign[2] == pow(-1,i[2]) */
        /* sweeps room index i[2] */
        for (i[2]=-imax[2]; i[2]<imax[2]; i[2]++,isign[2]=-isign[2]) {

          /* calculates position of virtual source in room (i[0],i[1],i[1]) */
          for (j=0; j<3; j++)
            virt_src[j] = 2*room[j]*ceil(i[j]/2.0)+isign[j]*source[j];

          /* calculates distance from virtual source to listener */
          dist = eucl_dist(virt_src,listener,3);

          /* computes delay and incidences for this sound ray */
          ray_list[nrays].delay = dist/SND_SPEED;
          for (j=0; j<3; j++) {
            ray_list[nrays].incidences[2*j] = abs(floor(i[j]/2.0));
            ray_list[nrays].incidences[2*j+1] = abs(ceil(i[j]/2.0));
          }

          nrays++;

          /* in case prior estimative of number of rays isn't enough: */
          if (nrays>=maxrays) return maxrays;

        } /* for (i[2]...) */

      } /* for (i[1]...) */

    } /* for (i[0]...) */

    return nrays;

} /* int create_rays(...) */





/* *******************************************************************************
   Function sray_energy: computes remaining energy of a sound ray
                         after all reflections */

double sray_energy (sound_ray r, double *abs_coefs) {
  int i;
  double energy;

  /* distance law for energy spectrum */
  energy = 1.0/(pow(r.delay*SND_SPEED,IRPOW));
  if (energy>1.0)
    energy = 1.0;

  /* applies absortion for each wall */
  for (i=0; i<6; i++)
    energy *= pow((1-abs_coefs[i]),r.incidences[i]);

  return energy;

} /* double sray_energy(...) */






/* *******************************************************************************
   Function create_ImpulseResponse: creates an Impulsive Response,
                                    given a list of sound rays and
                                    absorption coefficients for the room */

void create_ImpulseResponse (double *IR, /* Impulse Response */
                             int N,      /* size of IR (in audio samples) */
                             sound_ray *ray_list, /* list of sound rays */
                             int nrays,     /* size of list */
                             double *abs_coefs /* absorption coefficients */
                             ) {
  int i,index;

  /* initializes IR */
  for (i=0; i<N; i++)
    IR[i]=0;

  /* sweeps ray list */
  for (i=0; i<nrays; i++) {

    /* computes index in IR for ray placement.
       Index value is rounded to nearest integer */
    index = (ray_list[i].delay)*SMPL_RATE+0.5;

    /* uses only rays which fit in IR */
    if (index<N)
      IR[index] += sray_energy(ray_list[i],abs_coefs);

  }

} /* create_ImpulseResponse(...) */




/* *******************************************************************************
   Function create_FrequencyResponse: creates a Frequency Response
                                      given an Impulsive Response
                                      and a frequency range,
                                      averaging values over nbins
                                      adjacent bins.
                                      returns size of FR */

int create_FrequencyResponse ( double *FR[2],              /* output vector */
                               double *IR, int N,       /* impulse response */
                               fftw_complex *FOURIER,   /* aux vector */
                               double *freq_range,/* frequency range */
                               int nbins ) {
  int i,imin,imax,i0,i1,j;
  fftw_plan p;

  /* computes fft using fftw3 */
  p = fftw_plan_dft_r2c_1d(N, IR, FOURIER, FFTW_ESTIMATE);
  fftw_execute(p);
  fftw_destroy_plan(p);

  /* computes index range based on frequency range */
  imin = (freq_range[0]/SMPL_RATE)*N;
  imax = (freq_range[1]/SMPL_RATE)*N;

  /* computes magnitude frequency response */
  for (i=0; i<imax-imin+1; i++) {

    FR[0][i]=((double)(i+imin)*SMPL_RATE)/N;

    /* selects index range for averaging */
    i0=i+imin-nbins; i0=(i0<0)?0:i0;
    i1=i+imin+nbins; i1=(i1>N/2.0)?N/2.0:i1;

    /* computes mean of dB magnitude response  */
    for (j=i0,FR[1][i]=0; j<=i1; j++)
      FR[1][i] += log10(2*sqrt(FOURIER[j][0]*FOURIER[j][0]+FOURIER[j][1]*FOURIER[j][1]));

    FR[1][i] = 10*(FR[1][i]/(i1-i0+1));

  }

  /* returns size of computed FR */
  return imax-imin+1;

} /* int create_Frequence Response(...) */

