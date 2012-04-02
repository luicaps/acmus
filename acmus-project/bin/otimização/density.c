#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <float.h>
#include <time.h>
#include "random.h"
#include "optim.h"
#include "cost.h"
#include "defs.h"

// Define um ponto no espaço de busca
typedef struct _sample {
	double point[9];
	double value;	// Valor da função objetivo para estas coordenadas
	int included; 	// Indica se esse sample foi incluído em um Cluster
} sample;

// Define um cluster, com seu ponto central e o raio a partir desse ponto
typedef struct _cluster {
		double point[9];
		double value;
		double ray;
		struct _cluster * next;
} cluster;


double gammaFunction(double x) {
	int i, xi;
	double gamma = 1;
	
	xi = x; // xi == (int)x == floor(x);
	if (fabs(x-xi)<EPS) {
	    for (i = 1; i <= (xi - 1); i++) {
			gamma = gamma * i;
	    }
	}
	else if (fabs(x - (xi + 0.5))<EPS) {
	  //x = x - 0.5;
	    for (i = 1; i <= (2 * xi - 1); i = i + 2) {
			gamma = gamma * i;
	    }
	    gamma = gamma / pow(2, xi);
	    gamma = gamma * sqrt(M_PI);
	}
	else return -1.0;
	
	return gamma;
}

/**
 * Calcula uma distncia, escolhida por Rinnoy e Timmer, a partir dos 
 * valores para i e k especificados, para uma dimenso d.
 * @param i parmetro ao qual o raio ser proporcional
 * @param sigma constante positiva  qual o raio  proporcional
 * @param k nmero de iteraes
 * @param N tamanho da amostra
 * @param d dimenso do problema
 * @param m_S medida de Lebesgue do conjunto vivel
 * @return um raio calculado a partir dos parmetros
 */
double calculateRay(int i, double sigma, int k, int N, int d, double m_S) {
	double ray, g;
    g = gammaFunction(1 + d / 2.0);
	ray = (1 / sqrt(M_PI)) * pow(i * g * m_S * sigma * (log(k * N) / (k * N)), (1 / (double)d));
    return ray;
}

/**
 * Verifica se o ponto p especificado está dentro de um
 * raio r a partir do ponto x.
 * @param d dimensão dos pontos
 * @param p ponto que se deseja saber se está na região crítica
 * @param x centro da região crítica
 * @param r raio da região crítica
 * @return <code>true</code> se o ponto p especificado está dentro de um
 * raio r a partir do ponto x, e <code>false</code> caso contrário.
 */
int withinRay(int d, double * p, double * x, double r) {
	int i;
	double distance = 0.0;

// USAR FUNCAO EUCL_DIST
	for (i = 0; i < d; i++) {
	    distance = distance + ((p[i] - x[i]) * (p[i] - x[i]));
	}
	distance = sqrt(distance);

	printf("\nRaio: %f\n", r);
	printf("Distância do ponto: %f\n", distance);

	if (distance > r) 
		return 0;
	else 
		return 1;
}

void densityClustering(double * x, double * lo, double * hi, int ndim, 
						double* optval, int* nfeval, double (*cost)(double[])) {

	/* Parametros do algoritmo */
// TALVEZ PARAMETRO GLOBAL EM DEFS.H
	int k_max_iterations = 2; /* numero max. de iteracoes do algoritmo */
// TORNAR N PARAMETRO GLOBAL NSAMPLES EM DEFS.H
	int N = 10; /* numero de amostras em uma distribuicao uniforme */
	double gamma = 0.2; /* fator de reducao */
	double m_S;
	double ndim_var = 0; 	/* numero de dimensoes que variam */ 
		
	/* VariÃ¡veis auxiliares */
	sample * samples = (sample *)malloc(N * sizeof(sample));
	int n, m, p;
	int inclusion;
	double aux;

	/* VariÃ¡veis da amostra reduzida */
	int reducedN;
	sample * reducedSamples;
	
	/* Variáveis que armazenam a lista dinâmica de clusters */
	cluster * cl_begin = NULL;
	cluster * cl_actual = NULL;
	cluster * cl_aux = NULL;
	cluster * cl_best = NULL;
	int cl_size = 0;
	
	/* ray: raio de Rinnoy e Timmer, determinando o tamanho das bolas
	 * a partir do ponto semente. */
	double ray;

	/* sigma: constante positiva para cálculo do raio crítico */
	double sigma = 5;
	
	/* Ponto semente */
	double x_star[9];
	double * seedPoint;
	
	/* Inicializar valor de m_S e corrige o valor de ndim */
	//m_S = x[0] * x[1] * x[2];  // ERRADO - produtória hi(i) - lo(i) - i: hi!=lo(i)
	m_S = 1.0;
	for (n = 0; n < ndim; ++n) {
		double aux = abs(hi[n] - lo[n]);
		if (aux > EPS) {
			ndim_var++;
			m_S = m_S * aux;

		}
	}
#ifdef DEBUG_DC
		printf("\nndim_var = %d, m_S = %f\n", ndim_var, m_S);
#endif

	/* 
	 * Passo 0 - Inicializar parametros	do loop
	 */
	int k = 1;
	int included;
	
	while(k <= k_max_iterations) {
	
#ifdef DEBUG_DC
		printf("\n**** Passo 1 ****\n");
		printf("--- k = %d ---\n", k);
#endif

		int i = 1;
		int j = 0;

		/* 
		 * Passo 1 - Redução
		 */
		/* Gerar N pontos em uma distribuição uniforme sobre o conjunto viável */
		for(n=0; n < N; n++) {
			samples[n].point[0] = x[0];
			samples[n].point[1] = x[1];
			samples[n].point[2] = x[2];
			samples[n].point[3] = randomPoint(lo[3], hi[3]);
			samples[n].point[4] = randomPoint(lo[4], hi[4]);
			samples[n].point[5] = randomPoint(lo[5], hi[5]);
			samples[n].point[6] = randomPoint(lo[6], hi[6]);
			samples[n].point[7] = randomPoint(lo[7], hi[7]);
			samples[n].point[8] = randomPoint(lo[8], hi[8]);
			
			// Calcular o valor da funÃ§Ã£o objetivo
			// TODO Cache
// ACHO QUE NAO VAI PRECISAR DE CACHE (MARCELO)
			samples[n].value = cost(samples[n].point);
			samples[n].included = 0;
		}

		/* Determinar os gamma*k*N melhores pontos */
// NO ALGORITMO ORIGINAL SAO GAMMA*K*N DE TODO O HISTORICO
// TALVEZ USEMOS APENAS GAMMA*N
		reducedN = (int)(gamma * k * N);
		reducedSamples = (sample *)malloc(reducedN * sizeof(sample));
		int max_value = 0;
		// TROCAR COPIA DE STRUCT POR COPIA DE PONTEIRO:
		for (n = 0; n < reducedN; n++) {
			reducedSamples[n] = samples[n];
		}
// VERSAO MAIS EFICIENTE PARA ACHAR K MENORES ?? ACHO QUE NAO...
		for (; n < N; n++) {
			max_value = 0;
			for (m = 0; m < reducedN; m++) {
				if(reducedSamples[max_value].value < reducedSamples[m].value)
					max_value = m;
			}
			if (samples[n].value < reducedSamples[max_value].value) {
				reducedSamples[max_value] = samples[n];
			}
		}
		
#ifdef DEBUG_DC
		printf("Amostras\n");
		for (n = 0; n < N; n++) {
			printf("Ponto %d: (%f,%f,%f) (%f,%f,%f) = %f\n",n,
				samples[n].point[3],samples[n].point[4],samples[n].point[5],
				samples[n].point[6],samples[n].point[7],samples[n].point[8],
				samples[n].value);
		}
		printf("\n");			
		printf("Amostras reduzidas\n");
		for (n = 0; n < reducedN; n++) {
			printf("Ponto %d: (%f,%f,%f) (%f,%f,%f) = %f\n",n,
				reducedSamples[n].point[3],reducedSamples[n].point[4],reducedSamples[n].point[5],
				reducedSamples[n].point[6],reducedSamples[n].point[7],reducedSamples[n].point[8],
				reducedSamples[n].value);
		}
		printf("====\n");
#endif		

		/* Calcular o raio */
		/* TODO Vai antes ou depois?!?! */
// CORRIGIDO ndim_var NO LUGAR DE ndim
		ray = calculateRay(i, sigma, k, N, ndim_var, m_S);
#ifdef DEBUG_DC
		printf("ray = %f\n", ray);
#endif		
	    /* 
	     * Passo 2 
	     */
	    /* Se todos os pontos da amostra reduzida já estão em um cluster, 
	     * vá para o passo 4 */
		included = 0;
		while (included < reducedN) {
#ifdef DEBUG_DC
			printf("\n**** Passo 2 ****\n");
#endif
			if (j < cl_size) {
				n = 0;
				cl_aux = cl_begin;
				while (n < j) {
					cl_aux = cl_aux->next;
					n++;
				}
			    seedPoint = cl_aux->point;
#ifdef DEBUG_DC
			    printf("Novo ponto semente!\n");
				printf("Melhor ponto de X*:\n(%f,%f,%f) (%f,%f,%f) = %f\n",
					cl_aux->point[3],cl_aux->point[4],cl_aux->point[5],
					cl_aux->point[6],cl_aux->point[7],cl_aux->point[8],
					cl_aux->value);
#endif
			}
			else {				    		
				/* Procurar o ponto com menor valor da função objetivo dentro amostra reduzida (x') que não está em nenhum cluster */
				/* TODO JÁ PODIA TER SIDO FEITO ANTES, AO MONTAR O VETOR REDUZIDO */
				aux = DBL_MAX;
				for (n = 0, p = 0; n < reducedN; n++) {
					if (reducedSamples[n].value < aux && !reducedSamples[n].included) {
						aux = reducedSamples[n].value;
						p = n;
					}
				}
				
				/* Achar o mínimo local a partir deste ponto (usar Simplex/Gradiente) (x*) */
				for (n=0; n<9; n++) {
					x_star[n] = reducedSamples[p].point[n];
				}
#ifdef DEBUG_DC
				printf("Calculando gradiente...\n");
#endif				
				ProjectedGradient(x_star,lo,hi,ndim,optval,nfeval,cost);

#ifdef DEBUG_DC
				printf("Ponto mínimo com gradiente:\n(%f,%f,%f) (%f,%f,%f) = %f\n",
					x_star[3],x_star[4],x_star[5],
					x_star[6],x_star[7],x_star[8],
					*optval);
#endif				
				
				/* Se x* pertencer a X*, designar x' ao cluster */
				cl_aux = cl_begin;
// USAR FUNCAO DIST_EUCL
				while (cl_aux != NULL) {
					if (cl_aux->point[3] == x_star[3] && cl_aux->point[4] == x_star[4] && cl_aux->point[5] == x_star[5]&& 
						cl_aux->point[6] == x_star[6] && cl_aux->point[7] == x_star[7] && cl_aux->point[8] == x_star[8]) { 
						/* Adiciona x' a esse cluster */
						reducedSamples[p].included = 1;
						included++;
					}
					cl_aux = cl_aux->next;
				}
				
				/* Senão, cria um cluster com x* como centro, designa x' a esse cluster e x* é o novo ponto semente */
				if (!reducedSamples[p].included) {
	
					/* Cria novo cluster */
					cl_aux = (cluster *)malloc(sizeof(cluster));	
					cl_aux->next = NULL;
					for (n=0; n<9; n++) {
						cl_aux->point[n] = x_star[n]; 
					}
					cl_aux->value = *optval;
// O RAIO MUDA CONFORME A ITERACAO (TIRAR CAMPO DO STRUCT)
					cl_aux->ray = ray;
					
					/* Atualiza a lista de clusters */
// CONFIRMAR CL_ACTUAL E' SEMPRE ULTIMO (OK)
					if (cl_begin == NULL) {
						cl_begin = cl_aux;
						cl_actual = cl_aux;
					} else {
						cl_actual->next = cl_aux;
						cl_actual = cl_aux; 
					}
					cl_size++;
							
					/* x* é o novo ponto semente */
					seedPoint = x_star;
		
					/* Adiciona x' a esse cluster */
					reducedSamples[p].included = 1;
					included++;
				}
			}
					
		    /* 
		     * Passo 3 
		     */
			/* Adicione todos os pontos sem cluster da amostra que
			 * estejam dentro de uma distância ri(xmin) do ponto semente.*/
			inclusion = 1;
			while (inclusion) {
#ifdef DEBUG_DC
				printf("\n**** Passo 3 ****\n");
#endif
				inclusion = 0;
				for (n = 0; n < reducedN; ++n) {
					if (!reducedSamples[n].included) {
						if (withinRay(ndim, reducedSamples[n].point, seedPoint, ray)) {
#ifdef DEBUG_DC
							printf("Entro!!\n");
#endif
							reducedSamples[n].included = 1;
							included++;
							inclusion = 1;
						}
					}
				}
				
				// Se não incluiu nenhum ponto da amostra reduzida, j++ e volta para passo 2
				if (!inclusion) {
					printf("j = %d", j);
					j++;
				} 
				// Senão, i++ e volta para passo 3
				else {
					i++;
					ray = calculateRay(i, sigma, k, N, ndim_var, m_S);
#ifdef DEBUG_DC
					printf("i = %d, novo raio = %f", i, ray);
#endif
				}
			}
		}

	    /* 
	     * Passo 4 
	     */
#ifdef DEBUG_DC
		printf("\n**** Passo 4 ****\n");
#endif
		k++;	
		free(reducedSamples);

	}

	/* Seleciona o melhor ponto dos cluster e retorna */
	aux = DBL_MAX;
	cl_aux = cl_begin;
	while (cl_aux != NULL) {
		if (cl_aux->value < aux) {
			aux = cl_aux->value;
			cl_best = cl_aux;			
		}
		cl_aux = cl_aux->next;
	}
	
	for (n=0;n<9;n++) {
		x[n] = cl_best->point[n];
	}
// CONSERTAR CALCULO DE NFEVAL
	*nfeval = 1;
	*optval = cl_best->value;	

#ifdef DEBUG_DC
				printf("\nMelhor ponto de X*:\n(%f,%f,%f) (%f,%f,%f) = %f\n",
					cl_best->point[3],cl_best->point[4],cl_best->point[5],
					cl_best->point[6],cl_best->point[7],cl_best->point[8],
					cl_best->value);
#endif								

	/* Libera a memória */
	cl_actual = cl_begin;
	while(cl_actual != NULL) {
		cl_aux = cl_actual->next;
		free(cl_actual);
		cl_actual = cl_aux;
	}	
	free(samples);

}


