default : fresp
fresp : main.c allen.c filters.c cost.c optim.c debug.c random.c density.c defs.h
	gcc -g -lm -lfftw3 -o fresp gnuplot_i.c main.c allen.c filters.c optim.c cost.c debug.c random.c density.c 
clean :
	rm -f fresp
