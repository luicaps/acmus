

/* information about each sound ray:
   - delay in seconds
   - # of incidences on each wall (6 walls for cuboid rooms) */

typedef struct {
  double delay;
  int incidences[6];
} sound_ray;


/* *******************************************************************************
   Function eucl_dist: euclidean distance between points in R^N */

double eucl_dist(double *x, double *y, int N);


/* *******************************************************************************
   Function create_rays: creates a list of sound rays.
   number of rays is determined by maximum radius (in meters)
   returns the number of sound rays */

int create_rays (sound_ray *ray_list, int maxrays,
                 double radius,
                 double *room, double *abs_coefs,
                 double *source, double *listener);



/* *******************************************************************************
   Function create_ImpulseResponse: creates an Impulsive Response,
                                    given a list of sound rays and
                                    absorption coefficients for the room */

void create_ImpulseResponse (double *IR, /* Impulse Response */
                             int N,      /* size of IR (in audio samples) */
                             sound_ray *ray_list, /* list of sound rays */
                             int nrays,     /* size of list */
                             double *abs_coefs /* absorption coefficients */
                             );

