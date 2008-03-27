/*
 *  Parameters.java
 *  This file is part of AcMus.
 *  
 *  AcMus: Tools for Measurement, Analysis, and Simulation of Room Acoustics
 *  
 *  Copyright (C) 2006 Leo Ueda, Bruno Masiero
 *  
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
/*
 * Dsp.java
 * Created on 07/04/2005
 */
package acmus.dsp;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author lku
 */
public class Parameters {

  public static String CHANNEL_NAMES[] = { "62.5", "125", "250", "500", "1000",
      "2000", "4000", "8000", "A", "C", "Linear" };

  public static String energ[] = { "C50", "C80", "ST1", "D50", "D80", "DRR",
      "CT" };
  public static String energUnit[] = { "dB", "dB", "dB", "%", "%", "%", "ms" };
  public static String reverb[] = { "EDT", "T20", "T30", "T40" };

  //==========================================================================
  double _ir[];
  double _ir2[];
  double _temp[];
  double _fs;
  
  public Parameters(double [] ir, double [] ir2, double fs) {
    _ir = ir;
    _ir2 = ir2;
    _fs = fs;
    _temp = new double[_ir.length];
  }

  private final double rms(double []signal) {
    int offset = (int) Math.round(0.9 * signal.length) - 1;
    int length = signal.length - offset;
    for (int j = 0; j < length; j++) {
      _temp[j] = signal[offset + j] * signal[offset + j];
    }
    return Util.average(_temp, 0, length); 
  }
  
  private double [] chuPower(double[] signal) {
    // // for n = 1:size(banda,2)
    // // s(1,n) = ceil(1000*2^(n-5));
    // // comeco = inicio(banda(:,n));
    // // aux = banda(comeco:end,n).^2-RMS(n);
    // // [s(2,n),s(3,n),s(4,n),s(5,n),s(6,n)] = energeticos(aux,fs);
    // // [s(7,n),s(8,n),s(9,n),s(10,n)] = reverberacao(aux,fs,flag);
    // // end

    double RMS = rms(signal);
    int comeco = inicio(signal);
    double power[] = Util.sumLLL(Util.sqrLLL(Util.subArray(signal, comeco,
          signal.length)), -RMS);
    return power;
  }

  public void chuParam(int directSound,
      int firstReflection, PrintStream outTable, String graphFolder,
      IProgressMonitor monitor) {
    monitor = Util.monitorFor(monitor);

    monitor.beginTask("Calculating parameters", 140);

    IProgressMonitor subMonitor = Util.subMonitorFor(monitor, 20);

    // // banda = filtros(IR,fs);
    double[][] banda = filtros(_ir, _fs, subMonitor);

    double[][] bandaLf = null;
    if (_ir2 != null) bandaLf = filtros(_ir2, _fs, subMonitor);

    // // ruido = banda(round(.9*length(banda)):end,:).^2;
    // // RMS = sum(ruido)/length(ruido);

    PrintStream outGraphs[] = channelFiles(graphFolder);

    // // for n = 1:size(banda,2)
    // // s(1,n) = ceil(1000*2^(n-5));
    // // comeco = inicio(banda(:,n));
    // // aux = banda(comeco:end,n).^2-RMS(n);
    // // [s(2,n),s(3,n),s(4,n),s(5,n),s(6,n)] = energeticos(aux,fs);
    // // [s(7,n),s(8,n),s(9,n),s(10,n)] = reverberacao(aux,fs,flag);
    // // end

    Map<String, Parameter> params = new HashMap<String, Parameter>();
    for (int i = 0; i < energ.length; i++) {
      params.put(energ[i], new Parameter(energ[i], energUnit[i]));
    }
    if (_ir2 != null) {
      params.put("LF", new Parameter("LF", "%"));
    }
    for (int i = 0; i < reverb.length; i++) {
      params.put(reverb[i], new Parameter(reverb[i], "s"));
    }

    for (int n = 0; n < banda.length; n++) {
      double aux[] = chuPower(banda[n]);

      double en[] = energeticos(aux, _fs, directSound, firstReflection);
      for (int i = 0; i < energ.length - 1; i++) {
        params.get(energ[i]).val[n] = en[i];
      }

      params.get("CT").val[n] = en[en.length - 1] * 1000;

      if (_ir2 != null) {
        double auxLf[] = chuPower(bandaLf[n]);
        params.get("LF").val[n] = lf(aux, auxLf, _fs);
      }

      double rev[] = reverberacao(outGraphs[n], aux, _fs);
      outGraphs[n].close();

      for (int i = 0; i < rev.length; i++) {
        params.get(reverb[i]).val[n] = rev[i];
      }
      monitor.worked(10);
    }

    // // if nargout == 1
    // // saida = s;
    // // saida(1,9) = (['A']);
    // // saida(1,10) = (['C']);
    // // saida(1,11) = (['L']);
    // // else
    // // tabela(s,size(banda,2))
    // // end

    List<String> pOrder = new ArrayList<String>();
    for (String pName : energ)
      pOrder.add(pName);
    if (_ir2 != null) {
      pOrder.add("LF");
    }
    for (String pName : reverb)
      pOrder.add(pName);

    double br = bassRatio(params.get("T20"));
    double tr = trebleRatio(params.get("T20"));

    tabela(outTable, params, pOrder, banda.length, br, tr, (int) Math
        .round((firstReflection - directSound) / _fs * 1000));
    monitor.done();
  }

  //==========================================================================
  private static int doFFT_nu;

  public static final void doFFT(double[] x, double[] xre, double[] xim) {
    int n;
    // assume n is a power of 2
    n = x.length;
    double ld = (Math.log(n) / Math.log(2.0));

    if ((double) ((int) ld) - ld != 0) {
      System.out.println("Klasse FastFourierTransformation:");
      System.out
          .println("Der uebergebene Vektor hat keine laenge von 2 hoch n.");
      System.out.println("Die Laenge ist:" + n + " Der Logarithmus Dualis ist:"
          + ld);
      return;
    }
    doFFT_nu = (int) ld;
    int n2 = n / 2;
    int nu1 = doFFT_nu - 1;
    double tr, ti, p, arg, c, s;
    for (int i = 0; i < n; i++) {
      xre[i] = x[i];
      xim[i] = 0.0f;
    }
    int k = 0;

    for (int l = 1; l <= doFFT_nu; l++) {
      while (k < n) {
        for (int i = 1; i <= n2; i++) {
          p = bitrev(k >> nu1);
          arg = 2 * (double) Math.PI * p / n;
          c = (double) Math.cos(arg);
          s = (double) Math.sin(arg);
          tr = xre[k + n2] * c + xim[k + n2] * s;
          ti = xim[k + n2] * c - xre[k + n2] * s;
          xre[k + n2] = xre[k] - tr;
          xim[k + n2] = xim[k] - ti;
          xre[k] += tr;
          xim[k] += ti;
          k++;
        }
        k += n2;
      }
      k = 0;
      nu1--;
      n2 = n2 / 2;
    }
    k = 0;
    int r;
    while (k < n) {
      r = bitrev(k);
      if (r > k) {
        tr = xre[k];
        ti = xim[k];
        xre[k] = xre[r];
        xim[k] = xim[r];
        xre[r] = tr;
        xim[r] = ti;
      }
      k++;
    }

  }

  public static void PowerSpectrum(int NumSamples, double[] In, double[] Out) {
    int Half = NumSamples / 2;
    int i;

    double theta = Math.PI / Half;

    double[] RealOut = new double[Half];
    double[] ImagOut = new double[Half];

    for (i = 0; i < Half; i++) {
      RealOut[i] = In[2 * i];
      ImagOut[i] = In[2 * i + 1];
    }

    FFT1d fft = new FFT1d(Half);
    // FFT(Half, 0, tmpReal, tmpImag, RealOut, ImagOut);
    fft.fft(RealOut, ImagOut);

    double wtemp = (double) (Math.sin(0.5 * theta));

    double wpr = -2.0 * wtemp * wtemp;
    double wpi = (double) (Math.sin(theta));
    double wr = 1.0 + wpr;
    double wi = wpi;

    int i3;

    double h1r, h1i, h2r, h2i, rt, it;

    for (i = 1; i < Half / 2; i++) {

      i3 = Half - i;

      h1r = 0.5 * (RealOut[i] + RealOut[i3]);
      h1i = 0.5 * (ImagOut[i] - ImagOut[i3]);
      h2r = 0.5 * (ImagOut[i] + ImagOut[i3]);
      h2i = -0.5 * (RealOut[i] - RealOut[i3]);

      rt = h1r + wr * h2r - wi * h2i;
      it = h1i + wr * h2i + wi * h2r;

      Out[i] = rt * rt + it * it;

      rt = h1r - wr * h2r + wi * h2i;
      it = -h1i + wr * h2i + wi * h2r;

      Out[i3] = rt * rt + it * it;

      wr = (wtemp = wr) * wpr - wi * wpi + wr;
      wi = wi * wpr + wtemp * wpi + wi;
    }

    rt = (h1r = RealOut[0]) + ImagOut[0];
    it = h1r - ImagOut[0];
    Out[0] = rt * rt + it * it;

    rt = RealOut[Half / 2];
    it = ImagOut[Half / 2];
    Out[Half / 2] = rt * rt + it * it;

  }

  public static double[] fftMag(double[] xre, double xim[]) {
    int n = xre.length;
    double[] mag = new double[n / 2];
    // calculate Magnitude
    // mag[0] = (double) (Math.sqrt(xre[0] * xre[0] + xim[0] * xim[0])) / n;
    // for (int i = 1; i < n/2; i++)
    // mag[i] = 2 * (double) (Math.sqrt(xre[i] * xre[i] + xim[i] * xim[i])) / n;
    for (int i = 0; i < n / 2; i++)
      mag[i] = (xre[i] * xre[i] + xim[i] * xim[i]);
    return mag;
  }

  private static int bitrev(int j) {

    int j2;
    int j1 = j;
    int k = 0;
    for (int i = 1; i <= doFFT_nu; i++) {
      j2 = j1 / 2;
      k = 2 * k + j1 - 2 * j2;
      j1 = j2;
    }
    return k;
  }

  //==========================================================================

  // void mexFunction( int nlhs, mxArray *plhs[], int nrhs, const mxArray
  // *prhs[] )
  //
  // {
  // double *vct;
  // double *data;
  // unsigned int i,m,n,t;
  //    
  //
  // /* Check for proper number of arguments */
  //
  // if (nrhs != 1) {
  // mexErrMsgTxt("One input arguments required.");
  // } else if (nlhs != 1) {
  // mexErrMsgTxt("One input arguments required.");
  // }
  //
  // /* Check the dimensions of DATA */
  // m = (int)mxGetM(DATA_IN);
  // n = (int)mxGetN(DATA_IN);
  // t = log2(m);
  //      
  // /* Create a matrix for the return argument */
  // VCT_OUT = mxCreateDoubleMatrix(m, 1, mxREAL);
  //
  // /* Assign pointers to the various parameters */
  // vct = mxGetPr(VCT_OUT);
  // data = mxGetPr(DATA_IN);
  //
  // /* Do the actual computations in a subroutine */
  //
  // if (n != 1) mexErrMsgTxt("DATA must be a colummwise vector");
  // else for(i=0;i<m;i++) vct[i] = data[i];
  //
  // /* Do the actual computations in a subroutine */
  // FHT(vct,t);
  // return;
  // }
  //

  // * fht.c
  // *
  // * Calculates the Fast Hadamard Transform of a radix 2 sequence.
  // * The input vector must be a colummwise vector, os size 2^n.
  // *
  // * Bruno Masiero, Feb 2004
  public static final void fht(double[] data) {
    // // void FHT(double *data, int pwr2)
    // // {
    // // long length, i, j, k, ie, ie_half, kp;
    // // double temp;
    // //
    int pwr2 = (int) Util.log2(data.length);
    // // length = 1<<pwr2; /*tamanho da sequencia*/
    int length = 1 << pwr2;
    // // for (i = pwr2; i > 0; i--) {
    // // ie = 1<<i;
    // // ie_half = ie>>1;
    // //
    // // for (j = 0; j < ie_half; j++) {
    // // for (k = j; k < length; k += ie) {
    // // kp = k+ie_half;
    // // temp = *(data+k)+*(data+kp);
    // // *(data+kp) = *(data+k)-*(data+kp);
    // // *(data+k) = temp;
    // // }
    // // }
    // // }
    for (int i = pwr2; i > 0; i--) {
      int ie = 1 << i;
      int ie_half = ie >> 1;

      for (int j = 0; j < ie_half; j++) {
        for (int k = j; k < length; k += ie) {
          int kp = k + ie_half;
          double temp = data[k] + data[kp];
          data[kp] = data[k] - data[kp];
          data[k] = temp;
        }

      }
    }
    // // }
  }

  // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  // % chuparam.m
  // %
  // %Esta funcao calcula os diversos parametros acusticos de uma sala. O
  // %tratamento do sinal e feito pelo metodo de Chu.
  // %
  // %[s]=chuparam(IR,fs)
  // %
  // %Tem como entrada a resposta impulsiva e a taxa de amostragem.
  // %Tem como saida uma arquivo de texto com o valor dos parametros para as
  // %diversas bandas de frequencia. Se desejado, fornece uma matrix com estes
  // %valores, onde a primeira linha sao as frequencias centrais, ao inves do
  // %arquivo de texto.
  // %A variavel flag = 1 indica se deseja ver os graficos ca curva de Schroeder
  //
  // function [saida]=chuparam(IR,fs,flag)
  public static void chuParamOld(double[] ir, double[] irLf, double fs, int directSound,
      int firstReflection, PrintStream outTable, String graphFolder,
      IProgressMonitor monitor) {
    monitor = Util.monitorFor(monitor);

    monitor.beginTask("Calculating parameters", 140);

    IProgressMonitor subMonitor = Util.subMonitorFor(monitor, 20);

    // // banda = filtros(IR,fs);
    double[][] banda = filtros(ir, fs, subMonitor);

    // // ruido = banda(round(.9*length(banda)):end,:).^2;
    int offset = (int) Math.round(0.9 * banda[0].length) - 1;
    double ruido[][] = new double[banda.length][banda[0].length - offset];
    for (int i = 0; i < ruido.length; i++) {
      for (int j = 0; j < ruido[i].length; j++) {
        ruido[i][j] = banda[i][offset + j] * banda[i][offset + j];
      }
    }

    double[][] bandaLf = null;
    double ruidoLf[][] = null;
    double RMSLf[] =null;
    if (irLf != null) {
      IProgressMonitor subMonitorLf = Util.subMonitorFor(monitor, 10);
      bandaLf= filtros(irLf, fs, subMonitorLf);

    // // ruido = banda(round(.9*length(banda)):end,:).^2;
    int offsetLf = (int) Math.round(0.9 * bandaLf[0].length) - 1;
    ruidoLf = new double[bandaLf.length][bandaLf[0].length - offsetLf];
    for (int i = 0; i < ruidoLf.length; i++) {
      for (int j = 0; j < ruidoLf[i].length; j++) {
        ruidoLf[i][j] = bandaLf[i][offsetLf + j] * bandaLf[i][offsetLf + j];
      }
    }
    RMSLf = Util.sumLines(ruidoLf);
    RMSLf = Util.div(RMSLf, ruidoLf[0].length);
    }

    
    PrintStream outGraphs[] = channelFiles(graphFolder);

    monitor.worked(10);
    // print(banda[4]);
    // System.out.println("banda: ");
    // Matrix mmm = new Matrix(banda);
    // mmm.print(8,4);
    //
    // System.out.println(offset);
    // System.out.println("ruido: ");
    // Matrix mmm2 = new Matrix(ruido);
    // mmm2.print(8,4);

    // // RMS = sum(ruido)/length(ruido);
    double RMS[] = Util.sumLines(ruido);
    RMS = Util.div(RMS, ruido[0].length);


    monitor.worked(10);

    // // for n = 1:size(banda,2)
    // // s(1,n) = ceil(1000*2^(n-5));
    // // comeco = inicio(banda(:,n));
    // // aux = banda(comeco:end,n).^2-RMS(n);
    // // [s(2,n),s(3,n),s(4,n),s(5,n),s(6,n)] = energeticos(aux,fs);
    // // [s(7,n),s(8,n),s(9,n),s(10,n)] = reverberacao(aux,fs,flag);
    // // end

    Map<String, Parameter> params = new HashMap<String, Parameter>();
    for (int i = 0; i < energ.length; i++) {
      params.put(energ[i], new Parameter(energ[i], energUnit[i]));
    }
    if (irLf != null) {
      params.put("LF", new Parameter("LF", ""));
    }
    for (int i = 0; i < reverb.length; i++) {
      params.put(reverb[i], new Parameter(reverb[i], "s"));
    }

    for (int n = 0; n < banda.length; n++) {
      int comeco = inicio(banda[n]);
      // System.out.println("come?o: " + comeco);
      // double aux[] = Util.sumLLL(Util.powLLL(Util.subArray(banda[n], comeco,
      // banda[n].length),
      // 2), -RMS[n]);
      double aux[] = Util.sumLLL(Util.sqrLLL(Util.subArray(banda[n], comeco,
          banda[n].length)), -RMS[n]);
      // if (n==0)
      // print(powLLL(subArray(banda[n], comeco, banda[n].length),2));
      // print(aux);
      double en[] = energeticos(aux, fs, directSound, firstReflection);
      for (int i = 0; i < energ.length - 1; i++) {
        params.get(energ[i]).val[n] = en[i];
      }

      params.get("CT").val[n] = en[en.length - 1] * 1000;

      if (irLf != null) {
        int comecoLf = inicio(bandaLf[n]);
        System.out.println("b " + bandaLf[n][2000]);
        double auxLf[] = Util.sumLLL(Util.sqrLLL(Util.subArray(bandaLf[n], comecoLf,
          bandaLf[n].length)), -RMSLf[n]);
        //System.out.println("auxLf " + auxLf[2000]);
        params.get("LF").val[n] = lf(aux, auxLf, fs);
      }

      double rev[] = reverberacao(outGraphs[n], aux, fs);
      outGraphs[n].close();

      for (int i = 0; i < rev.length; i++) {
        params.get(reverb[i]).val[n] = rev[i];
      }
      monitor.worked(10);
    }

    // // if nargout == 1
    // // saida = s;
    // // saida(1,9) = (['A']);
    // // saida(1,10) = (['C']);
    // // saida(1,11) = (['L']);
    // // else
    // // tabela(s,size(banda,2))
    // // end

    List<String> pOrder = new ArrayList<String>();
    for (String pName : energ)
      pOrder.add(pName);
    if (irLf != null) {
      pOrder.add("LF");
    }
    for (String pName : reverb)
      pOrder.add(pName);

    double br = bassRatio(params.get("T20"));
    double tr = trebleRatio(params.get("T20"));

    tabela(outTable, params, pOrder, banda.length, br, tr, (int) Math
        .round((firstReflection - directSound) / fs * 1000));
    monitor.done();
  }


  
  // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  // % inicio.m
  // %
  // % [ponto,rms] = inicio(impulse)
  // %
  // %% Acha o inicio do sinal de acordo com as recomendacoes da ISO 3382
  // % e se desejado, fornece o nivel de ruido antes do inicio do sinal.
  // % A entrada e a resposta impulsiva do sinal.
  //
  public final static int inicioOld(double impulse[]) {
    // function [ponto,rms] = inicio(impulse)
    //
    // maximo = find(abs(impulse) == max(abs(impulse)));
    int maximo = Util.maxIndAbs(impulse);
    // energia = (impulse/impulse(maximo(1))).^2;
    // double energia[] = Util.powLLL(Util.div(impulse, impulse[maximo]), 2);
    double energia[] = Util.sqrLLL(Util.div(impulse, impulse[maximo]));
    //
    // aux = energia(1:maximo(1)-1);
    // -double aux [] = new double[maximo];
    // ponto = maximo(1);
    int ponto = maximo;
    // if any(aux > 0.01)
    // ponto=min(find(aux > 0.01));
    // aux = energia(1:ponto-1);
    // end
    for (int i = 0; i < maximo; i++) {
      if (energia[i] > 0.01) {
        ponto = i;
        break;
      }
    }

    return ponto;
    //
    // if nargout == 2
    // rms = 10*log10(sum(aux)/length(aux));
    // end
  }

  // // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  // // % inicio.m
  // // %
  // // % [ponto,rms] = inicio(impulse)
  // // %
  // // %% Acha o inicio do sinal de acordo com as recomendacoes da ISO 3382
  // // % e se desejado, fornece o nivel de ruido antes do inicio do sinal.
  // // % A entrada e a resposta impulsiva do sinal.
  // //
  // // function [ponto,rms] = inicio(impulse)
  public final static int inicio(double impulse[]) {
    // //
    // // maximo = find(abs(impulse) == max(abs(impulse)));
    int maximo = Util.maxIndAbs(impulse);

    // // energia = (impulse/impulse(maximo(1))).^2;
    double energia[] = Util.sqrLLL(Util.div(impulse, impulse[maximo]));
    // //
    // // aux = energia(1:maximo(1)-1);
    // // ponto = maximo(1);
    int ponto = maximo;

    // // if any(aux(round(end/2):end) > 0.001)
    // // posicao = (find(aux > 0.001));
    // // ponto = min(posicao(find(posicao > length(aux)/2)));
    // // aux = energia(1:ponto-1);
    // // end

    for (int i = maximo / 2; i < maximo; i++) {
      if (energia[i] > 0.001) {
        ponto = i;
        break;
      }
    }

    return ponto;
    // //
    // // if nargout == 2
    // // rms = 10*log10(sum(aux)/length(aux));
    // // end

  }

  // // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  // // % energeticos.m
  // //
  // // %Esta funcao calcula alguns dos parametros acusticos para
  // // %salas.
  // // %Tem como entrada a resposta impulsiva ao quadrado e a
  // // %taxa de amostragem do sinal.
  // //
  // // function [C50,C80,D50,D80,CT]=energeticos(energia,Fs);
  public final static double[] energeticos(double[] energia, double fs,
      int somDireto, int primeiraReflexao) {
    // // t50 = round(0.05*Fs);
    // // t80 = round(0.08*Fs);
    int t10 = (int) Math.round(0.01 * fs);
    int t20 = (int) Math.round(0.02 * fs);
    int t50 = (int) Math.round(0.05 * fs);
    int t80 = (int) Math.round(0.08 * fs);
    int t100 = (int) Math.round(0.1 * fs);
    // // %Clarity = razao entre energia inicial do sinal, e energia
    // remanescente.
    // // C50 = 10*log10(sum(energia(1:t50))/sum(energia(t50:end)));
    // // C80 = 10*log10(sum(energia(1:t80))/sum(energia(t80:end)));

    // C50 = 10*
    // log10(
    // sum(energia(1:t50)) / sum(energia(t50:end))
    // );
    // System.out.println(energia.length + " " + t50);
    double c50 = 10 * Util.log10(Util.sum(energia, 0, t50)
        / Util.sum(energia, t50 - 1, energia.length));
    double c80 = 10 * Util.log10(Util.sum(energia, 0, t80)
        / Util.sum(energia, t80 - 1, energia.length));
    // System.out.println(t50);
    // System.out.println(Util.sum(energia, 0, t50) + " "
    // + Util.sum(energia, t50 - 1, energia.length) + " " + c50);

    // ST1 = (integral de 20ms a 100ms)/(integral de 0ms a 10ms).
    double st1 = 10 * Util.log10(Util.sum(energia, t20, t100)
        / Util.sum(energia, 0, t10));
    // //
    // // %Definition = razao entre energia inicial do sinal, e energia total do
    // // sinal.
    // // D50 = sum(energia(1:t50))/sum(energia)*100;
    // // D80 = sum(energia(1:t80))/sum(energia)*100;

    double d50 = (Util.sum(energia, 0, t50) / Util.sum(energia)) * 100;
    double d80 = (Util.sum(energia, 0, t80) / Util.sum(energia)) * 100;

    // drr (not in matlab prototype)
    double drr = (Util.sum(energia, somDireto, primeiraReflexao) / Util.sum(
        energia, somDireto, energia.length)) * 100;

    // //
    // // %Tempo Central, equivalente ao centro de gravidade da curva de
    // energia.
    // // x=(0:length(energia)-1)/Fs;
    // // CT = sum(energia(:).*x(:))/sum(energia);

    double x[] = new double[energia.length];
    for (int i = 0; i < x.length; i++)
      x[i] = i / fs;
    double ct = Util.sum(Util.multLLL(x, energia)) / Util.sum(energia);

    double res[] = new double[7];
    res[0] = c50;
    res[1] = c80;
    res[2] = st1;
    res[3] = d50;
    res[4] = d80;
    res[5] = drr;
    res[6] = ct;
    return res;
  }

  public static double lf(double[] energia1, double[] energia2, double fs) {
    int t5 = (int) Math.round(0.005 * fs);
    int t80 = (int) Math.round(0.08 * fs);
    double lfc = ( Util.sum(energia2, t5, t80 < energia2.length? t80 : energia2.length) / Util.sum(energia1, 0, t80)) * 100;
    return lfc;
  }

  // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  // % reverberacao.m
  // %
  // %Esta funcao calcula o tempo de reverberacao de uma sala
  // %em funcao da sua resposta impulsiva.
  // %
  // % [EDT,T20,T30,T40] = reverberacao(IR^2,Fs,flag)
  // %
  // %Tem como entrada a a resposta impulsiva ao quadrado e a taxa de amostragem
  // %Esta curva de Schroeder deve ter sido obtida apos descartar o atraso
  // %da resposta impulsiva.
  // %Tem como saida os tempos de reverberacao T20, T30 e T40, alem do EDT
  // %
  // %A variavel de flag define se a funcao ira plotar ou nao as assintotas
  // %calculadas. Caso nao seja definido, a funcao nao ira gerar estes graficos.
  //
  public static double[] reverberacao(PrintStream out, double ir[], double fs) {
    // function [EDT,T20,T30,T40] = reverberacao(varargin)
    //
    // ir = varargin{1};
    // Fs = varargin{2};
    // E(length(ir):-1:1) = (cumsum(ir(length(ir):-1:1))/sum(ir));

    double[] E = Util.divLLL(Util.cumsumLLL(Util.reverse(ir)), Util.sum(ir));
    Util.reverseLLL(E);

    //
    // if find(E < 0)
    // E(min(find(E < 0)):end) = [];
    // E=10*log10(E);
    // else
    // E=10*log10(E);
    // end
    int neg = -1;
    for (int i = 0; i < E.length; i++) {
      if (E[i] < 0) {
        neg = i;
        break;
      }
    }
    if (neg >= 0) {
      E = Util.subArray(E, 0, neg);
    }
    E = Util.multLLL(Util.log10LLL(E), 10);
    //
    // if nargin == 3
    // flag = varargin{3};
    // else
    // flag = 0;
    // end
    //
    // x = (0:length(E)-1)/44100;

    double x[] = new double[E.length];
    for (int i = 0; i < x.length; i++) {
      x[i] = (double) i / 44100;
    }

    // // % Calcula os tempos de reverberacao da resposta impulsiva (T20 e T30)
    // // a partir
    // // % curva de Schroeder (em dB) fornecida no argumento de entrada.
    // //
    // // if flag == 1
    // // %grafico de saida
    // // figure, plot(x,E,'LineWidth',1.5);
    // // end
    out.println(x.length);
    for (int i = 0; i < x.length; i++) {
      out.print(x[i] + " ");
    }
    out.println();
    for (int i = 0; i < E.length; i++) {
      out.print(E[i] + " ");
    }
    out.println();

    // %Calcula o Early Decay Time (EDT) do sinal. A curva de Schroeder
    // fornecida deve
    // %ter sido obtida a partir de uma resposta impulsiva sem ruido de inicio.
    // t10 = min(find(E < -15));
    // [A10,B10] = intlinear(x(1:t10),E(1:t10));
    // EDT = (-60)/(B10);

    int t10 = Util.firstLessThan(E, -15);
    double[] ab = Util.intlinear(Util.subArray(x, 0, t10 + 1), Util.subArray(E,
        0, t10 + 1));
    double EDT = ((double) -60) / (ab[1]);

    out.println(EDT);
    out.println(ab[0] + " " + ab[1] + " " + t10);

    //
    // begin = min(find(E < -5));
    // t25 = min(find(E < -25)); %Se a curva nao for monotonica, o primeiro
    // ponto onde a curva
    // t35 = min(find(E < -35)); %atinge -25 e -35dB limita a regiao de
    // iteracao.
    // t45 = min(find(E < -45));

    int begin = Util.firstLessThan(E, -5);
    int t25 = Util.firstLessThan(E, -25);
    int t35 = Util.firstLessThan(E, -35);
    int t45 = Util.firstLessThan(E, -45);

    //
    // %Usando 20dB
    // if ~isempty(t25)
    // [A20,B20] = intlinear(x(begin:t25),E(begin:t25));
    // T20 = (-60)/(B20);
    // else
    // T20=NaN; %Caso a resposta impulsiva nao apresentefaixa dinamica
    // suficiente
    // end

    double T20 = Double.NaN;
    if (t25 >= 0) {
      ab = Util.intlinear(Util.subArray(x, begin, t25 + 1), Util.subArray(E,
          begin, t25 + 1));
      T20 = ((double) -60) / (ab[1]);
      out.println(ab[0] + " " + ab[1] + " " + T20);
    } else
      out.println("?");

    //
    // %Usando 30dB
    // if ~isempty(t35)
    // [A30,B30] = intlinear(x(begin:t35),E(begin:t35));
    // T30 = (-60)/(B30);
    // else
    // T30=NaN; %Caso a resposta impulsiva nao apresentefaixa dinamica
    // suficiente
    // end

    double T30 = Double.NaN;
    if (t35 >= 0) {
      ab = Util.intlinear(Util.subArray(x, begin, t35 + 1), Util.subArray(E,
          begin, t35 + 1));
      T30 = ((double) -60) / (ab[1]);
      out.println(ab[0] + " " + ab[1] + " " + T30);
    } else
      out.println("?");

    //
    // if nargout == 4
    // %Usando 40dB
    // if ~isempty(t45)
    // [A40,B40] = intlinear(x(begin:t45),E(begin:t45));
    // T40 = (-60)/(B40);
    // else
    // T40=NaN;
    // end
    // else
    // T40=NaN; %Caso a resposta impulsiva nao apresentefaixa dinamica
    // suficiente
    // end

    double T40 = Double.NaN;
    if (t45 >= 0) {
      ab = Util.intlinear(Util.subArray(x, begin, t45 + 1), Util.subArray(E,
          begin, t45 + 1));
      T40 = ((double) -60) / (ab[1]);
      out.println(ab[0] + " " + ab[1] + " " + T40);
    } else
      out.println("?");

    double res[] = new double[4];
    res[0] = EDT;
    res[1] = T20;
    res[2] = T30;
    res[3] = T40;
    return res;
    //
    // if flag == 1
    // title('Aproximacao dos tempos de Decaimento');
    // ylim([-70 0]);
    // ylimit = ylim;
    // xlabel('tempo (s)'), ylabel('dB')
    // xlim([0 max([T20 T30 T40])*1.1]);
    // xlimit=xlim;
    //      
    // line([0,(-60-A10)/(B10)],[A10,-60],'Color','m','LineWidth',.5);
    // line([0,(-60-A20)/(B20)],[A20,-60],'Color','r','LineWidth',.5);
    // line([0,(-60-A30)/(B30)],[A30,-60],'Color','g','LineWidth',.5);
    // if nargout == 4
    // line([0,(-60-A40)/(B40)],[A40,-60],'Color','y','LineWidth',.5);
    // end
    // line([xlimit(1),xlimit(2)],[-60,-60],'Color',[.4,.4,.4],'LineWidth',.5);
    //    
    // legend('Curva de Schroeder',['EDT (ms) = ',num2str(EDT*1000)],['T_2_0
    // (ms) = ',num2str(T20*1000)],...
    // ['T_3_0 (ms) = ',num2str(T30*1000)],['T_4_0 (ms) = ',num2str(T40*1000)])
    // end
  }

  public static double bassRatio(Parameter t20) {
    return (t20.val[1] + t20.val[2]) / (t20.val[3] + t20.val[4]);
  }

  public static double trebleRatio(Parameter t20) {
    return (t20.val[5] + t20.val[6]) / (t20.val[3] + t20.val[4]);
  }

  // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  // % tabela.m
  // %
  // %Esta funcao apenas gera uma tabela em .txt com os valores obtidos para os
  // %parametros acusticos.
  // %
  // %tabela(s,n)
  //
  public final static void tabela(PrintStream out, Map<String, Parameter> s,
      List<String> paramsOrder, int n, double br, double tr, int itdg) {
    // function tabela(s,n)

    DecimalFormat format;// = new DecimalFormat("#.###");
    NumberFormat f = DecimalFormat.getInstance(new Locale("en"));
    if (f instanceof DecimalFormat) {
      format = (DecimalFormat) DecimalFormat.getInstance(new Locale("en"));
      format.applyPattern("#.###");
    } else {
      format = new DecimalFormat("#.###");
    }
    int k = 9;
    //
    //
    // fid = fopen('parametros.txt','w');
    //
    // fprintf(fid,'freq [Hz]');
    // for m=1:(n-3)
    // fprintf(fid,' %5d ',s(1,m));
    // end
    // fprintf(fid,' A ');
    // fprintf(fid,' C ');
    // fprintf(fid,' Linear ');
    // fprintf(fid,'\n');

    out.print(Util.fs("freq [Hz]", k));
    for (int i = 0; i < n - 3; i++) {
      out.print(Util.fs(CHANNEL_NAMES[i], k) + " ");
    }
    out.print(Util.fs("A", k) + " ");
    out.print(Util.fs("C", k) + " ");
    out.print(Util.fs("Linear", k) + " ");
    out.println();

    // fprintf(fid,' C50 [dB]');
    // for m=1:n
    // fprintf(fid,' %-5.2f ',s(2,m));
    // end
    // fprintf(fid,'\n');
    //
    // fprintf(fid,' C80 [dB]');
    // for m=1:n
    // fprintf(fid,' %-5.2f ',s(3,m));
    // end
    // fprintf(fid,'\n');
    //
    // fprintf(fid,' D50 [%%] ');
    // for m=1:n
    // fprintf(fid,' %-5.2f ',s(4,m));
    // end
    // fprintf(fid,'\n');
    //
    // fprintf(fid,' D80 [%%] ');
    // for m=1:n
    // fprintf(fid,' %-5.2f ',s(5,m));
    // end
    // fprintf(fid,'\n');
    //
    // fprintf(fid,' CT [ms]');
    // for m=1:n
    // fprintf(fid,' %-5.2f ',s(6,m)*1000);
    // end
    // fprintf(fid,'\n');
    //
    // fprintf(fid,' EDT [s] ');
    // for m=1:n
    // fprintf(fid,' %-2.3f ',s(7,m));
    // end
    // fprintf(fid,'\n');
    //
    // fprintf(fid,' T20 [s] ');
    // for m=1:n
    // fprintf(fid,' %-2.3f ',s(8,m));
    // end
    // fprintf(fid,'\n');
    //
    // fprintf(fid,' T30 [s] ');
    // for m=1:n
    // fprintf(fid,' %-2.3f ',s(9,m));
    // end
    // fprintf(fid,'\n');
    //
    // fprintf(fid,' T40 [s] ');
    // for m=1:n
    // fprintf(fid,' %-2.3f ',s(10,m));
    // end
    //    
    // fprintf(fid,'\n');
    // fprintf(fid,'\n');
    // fprintf(fid,'\n');
    //  

//    String lineLabels[] = { "C50 [dB]", "C80 [dB]", "ST1 [dB]", "D50 [%%]",
//        "D80 [%%]", "DRR [%%]", "CT [ms]", "EDT [s]", "T20 [s]", "T30 [s]",
//        "T40 [s]" };

    for (String p : paramsOrder) {
      out.print(Util.fs(p + " [" + s.get(p).unit + "]", k) + " ");
      for (int j = 0; j < n; j++) {
        double val = s.get(p).val[j];
        if (val == Double.NaN)
          out.print(Util.fs("NaN", k));
        else
          out.print(Util.fs(format.format(val), k));
        out.print(" ");
      }
      out.println();
    }
    out.println();
    out.println();
    // fprintf(fid,' BR: %-2.3f \n',(s(8,2)+s(8,3))/(s(8,4)+s(8,5)));
    // fprintf(fid,' TR: %-2.3f \n',(s(8,6)+s(8,7))/(s(8,4)+s(8,5)));

    out.println("   BR:   " + format.format(br));
    // + format.format((s[7][1] + s[7][2]) / (s[7][3] + s[7][4])));
    out.println("   TR:   " + format.format(tr));
    // + format.format((s[7][5] + s[7][6]) / (s[7][3] + s[7][4])));
    out.println(" ITDG:   " + (itdg));

    //      
    // fclose(fid)
  }

  // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  // % Lundeby.m
  // %
  // %Esta funcao realiza as iteracoes do metodo de Lundeby.
  // %Aplicar metodo de Lundeby para determinar ponto de truncamento.
  // %
  // %[ponto,C]=lundeby(IR,Fs,flag)
  // %
  // %Tem como entrada a resposta impulsiva e a taxa de amostragem
  // %Tem como saida o ponto de cruzamento da assintota de decaimento o nivel
  // de
  // %ruido e se desejado, a constante C usada para compensar o truncamento no
  // %calculo da curva de Schroeder.
  // %Se nao for fornecida variavel de saida, a funcao imprime um grafico dos
  // %niveis encontrados.
  //
  public static double[] lundeby(double[] ir, double fs) {
    // function [ponto,C]=lundeby(varargin)

    double ponto, C;

    //
    // warning off
    //
    // energia_impulso = varargin{1}.^2;
    // Fs = varargin{2};

    double energia_impulso[] = new double[ir.length];
    for (int i = 0; i < energia_impulso.length; i++) {
      energia_impulso[i] = ir[i] * ir[i];
    }

    // if nargin == 3
    // flag = varargin{3};
    // else
    // flag = 0;
    // end
    //
    //
    // %Calcula o nivel de ruido dos ultimos 10% do sinal, onde se assume que o
    // %ruido ja domine o sinal
    // rms_dB =
    // 10*log10(mean(energia_impulso(round(.9*length(energia_impulso)):end))/max(energia_impulso));

    // rms_dB =
    // 10*
    // log10(
    // mean(
    // energia_impulso(round(.9*length(energia_impulso)):end)
    // )
    // /max(energia_impulso)
    // );
    double max = Util.max(energia_impulso);
    double rms_dB = 10 * Util.log10(Util.mean(energia_impulso, (int) Math
        .round(0.9 * energia_impulso.length) - 1, energia_impulso.length)
        / max);

    System.out.println("max: " + max + "  rms:" + rms_dB);
    //
    // %divide em intervalos e obtem media
    // t = floor(length(energia_impulso)/Fs/0.01);
    // v = floor(length(energia_impulso)/t);

    int t = (int) Math.floor(energia_impulso.length / fs / 0.01);
    int v = (int) Math.floor(energia_impulso.length / t);

    System.out.println("t: " + t + "  v:" + v);

    //
    // for n=1:t
    // media(n) = mean(energia_impulso((((n-1)*v)+1):(n*v)));
    // eixo_tempo(n) = ceil(v/2)+((n-1)*v);
    // end
    double media[] = new double[t];
    double eixo_tempo[] = new double[t];
    for (int n = 0; n < t; n++) {
      // media[n] = mean(energia_impulso, ((n - 1) * v), n * v);
      // eixo_tempo[n] = Math.ceil(v / 2) + ((n - 1) * v);
      media[n] = Util.mean(energia_impulso, n * v, (n + 1) * v);
      eixo_tempo[n] = Math.ceil((double) v / 2) + (n * v);
    }

    Util.print(media);
    Util.print(eixo_tempo);

    // mediadB = 10*log10(media/max(energia_impulso));

    double mediadB[] = Util.multLLL(Util.log10LLL(Util.div(media, max)), 10);

    //
    // %obtem a regressao linear o intervalo de 0dB e a media mais proxima de
    // %rms+10dB
    // r = max(find(mediadB > rms_dB+10));
    int r = -1;
    for (int i = 0; i < mediadB.length; i++) {
      if (mediadB[i] > rms_dB + 10)
        r = i;
    }

    // if any (mediadB(1:r) < rms_dB+10)
    // r = min(find(mediadB(1:r) < rms_dB+10));
    // end
    // if isempty(r)
    // r=10
    // elseif r<10
    // r=10;
    // end
    System.out.println("r: " + r);
    for (int i = 0; i <= r; i++) {
      if (mediadB[i] < rms_dB + 10) {
        r = i;
        break;
      }
    }
    System.out.println("r: " + r);
    if (r < 9) {
      r = 9;
    }

    System.out.println("r: " + r);

    //
    // [A,B] = intlinear(eixo_tempo(1:r),mediadB(1:r));
    // cruzamento = (rms_dB-A)/B;

    double[] ab = Util.intlinear(Util.subArray(eixo_tempo, 0, r + 1), Util
        .subArray(mediadB, 0, r + 1));
    double cruzamento = (rms_dB - ab[0]) / ab[1];
    System.out.println("cruzamento: " + cruzamento + " " + rms_dB);

    // if rms_dB > -20
    if (rms_dB > -20) {

      // %Relacao sinal ruido insuficiente
      // ponto=length(energia_impulso);
      ponto = energia_impulso.length;
      // if nargout==2
      // C=0;
      // end
      C = 0;

      // else
    } else {
      //
      // %%%%%%%%%%%%%%%%%%%%%%%%INICIA A PARTE ITERATIVA DO
      // PROCESSO%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
      //
      // erro=1;
      // INTMAX=50;
      // vezes=1;
      double erro = 1;
      int INTMAX = 50;
      int vezes = 1;

      // while (erro > 0.0001 & vezes <= INTMAX)
      while (erro > 0.0001 & vezes <= INTMAX) {
        //
        // %Calcula novos intervalos de tempo para media, com aproximadamente p
        // %passos por 10dB
        // clear r t v n media eixo_tempo;
        //
        // p = 5; %numero de passos por decada
        int p = 5;
        // delta = abs(10/B); %numero de amostras para o a linha de tendencia
        // % decair 10dB
        double delta = Math.abs(10 / ab[1]);
        // v = floor(delta/p); %intervalo para obtencao de media
        v = (int) Math.floor(delta / p);
        // t = floor(length(energia_impulso(1:round(cruzamento-delta)))/v);
        t = (int) Math.floor((Math.round(cruzamento - delta)) / v);

        System.out.println("t: " + t + "  v:" + v + " delta: " + delta);

        // if t < 2 %numero de intervalos para obtencao da nova media no
        // % intervalo
        // t=2; %que vai do inicio ate 10dB antes do ponto de cruzamento.
        // elseif isempty(t)
        // t=2;
        // end
        if (t < 2) {
          t = 1;
        }

        //
        // for n=1:t
        // media(n) = mean(energia_impulso((((n-1)*v)+1):(n*v)));
        // eixo_tempo(n) = ceil(v/2)+((n-1)*v);
        // end

        if (t != media.length) {
          media = new double[t];
          eixo_tempo = new double[t];
        }
        System.out.println("eim " + energia_impulso.length + " " + t);
        for (int n = 0; n < t; n++) {
          System.out.println("  " + n);
          media[n] = Util.mean(energia_impulso, n * v, (n + 1) * v);
          eixo_tempo[n] = Math.ceil((double) v / 2) + (n * v);
        }

        Util.print(media);
        Util.print(eixo_tempo);
        // mediadB = 10*log10(media/max(energia_impulso));
        mediadB = Util.multLLL(Util.log10LLL(Util.div(media, Util
            .max(energia_impulso))), 10);

        //
        // clear A B noise energia_ruido rms_dB;
        // [A,B] = intlinear(eixo_tempo,mediadB);
        ab = Util.intlinear(eixo_tempo, mediadB);

        // %nova media da energia do ruido, iniciando no ponto da linha de
        // % tendencia 10dB abaixo do cruzamento.
        // noise = energia_impulso(round(cruzamento+delta):end);
        int noiseStart = (int) Math.round(cruzamento + delta) - 1;
        // if (length(noise) < round(.1*length(energia_impulso)))
        // noise = energia_impulso(round(.9*length(energia_impulso)):end);
        // end
        if (energia_impulso.length - noiseStart < Math
            .round(0.1 * energia_impulso.length)) {
          noiseStart = (int) Math.round(0.9 * energia_impulso.length);
        }
        double noise[] = Util.subArray(energia_impulso, noiseStart);
        System.out.println("ns: " + noiseStart + " " + noise.length);
        // print(noise);

        // rms_dB = 10*log10(mean(noise)/max(energia_impulso));
        rms_dB = 10 * Util.log10(Util.mean(noise) / Util.max(energia_impulso));

        System.out.println("rms: " + rms_dB);
        Util.print(ab);
        //
        // %novo ponto de cruzamento.
        // erro = abs(cruzamento - (rms_dB-A)/B)/cruzamento;
        erro = Math.abs(cruzamento - (rms_dB - ab[0]) / ab[1]) / cruzamento;
        // cruzamento = round((rms_dB-A)/B);
        cruzamento = Math.round((rms_dB - ab[0]) / ab[1]);
        // vezes = vezes + 1;
        vezes++;
        // end
        System.out.println(vezes + " erro: " + erro + " cruzamento:"
            + cruzamento);
        System.out.println("---------------------------");

      }
      // end
    // } <--- bug no protótipo?

    //
    // if nargout == 1
    // if cruzamento > length(energia_impulso) %caso o sinal nao atinja o
    // patamar de ruido
    // ponto = length(energia_impulso); %nas amostras fornecidas, considera-se o
    // ponto
    // else %de cruzamento a ultima amosta, o que equivale
    // ponto = cruzamento; %a nao truncar o sinal.
    // end
    // elseif nargout == 2
    // if cruzamento > length(energia_impulso)
    // ponto = length(energia_impulso);
    // else
    // ponto = cruzamento;
    // end
    // C=max(energia_impulso)*10^(A/10)*exp(B/10/log10(exp(1))*cruzamento)/(-B/10/log10(exp(1)));
    // end
    ponto = (cruzamento > energia_impulso.length) ? energia_impulso.length
        : cruzamento;
    C = Util.max(energia_impulso) * Math.pow(10, (ab[0] / 10))
        * Math.exp(ab[1] / 10 / Util.log10(Math.exp(1)) * cruzamento)
        / (-ab[1] / 10 / Util.log10(Math.exp(1)));

    }// <--- correcao do bug???
    
    System.out.println(ponto + " C:" + C);
    //
    // if (nargout == 0 | flag == 1)
    // figure
    // plot((1:length(energia_impulso))/Fs,10*log10(energia_impulso/max(energia_impulso)));
    // hold
    // stairs(eixo_tempo/Fs,mediadB,'r');
    // plot((1:cruzamento+1000)/Fs,A+(1:cruzamento+1000)*B,'g');
    // line([cruzamento-1000,length(energia_impulso)]/Fs,[rms_dB,rms_dB],'Color',[.4,.4,.4]);
    // plot(cruzamento/Fs,rms_dB,'o','MarkerFaceColor','y','MarkerSize',10);
    // hold
    // end
    //
    //  
    double[] res = new double[2];
    res[0] = ponto;
    res[1] = C;
    return res;
  }

  // // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  // // % ldbparam.m
  // // %
  // // %Esta funcao calcula os diversos parametros acusticos de uma sala. O
  // // %tratamento do sinal e feito pelo metodo de Lundeby.
  // // %
  // // %[s]=parametros(IR,fs,flag)
  // // %
  // // %Tem como entrada a resposta impulsiva e a taxa de amostragem.
  // // %Tem como saida uma arquivo de texto com o valor dos parametros para as
  // // %diversas bandas de frequencia. Se desejado, fornece uma matrix com
  // estes
  // // %valores, onde a primeira linha sao as frequencias centrais, ao inves do
  // // %arquivo de texto.
  // // %A variavel flag = 1 indica se deseja ver os graficos ca curva de
  // Schroeder
  // // %e metodo de Lundeby
  // //
  // // function [saida]=ldbparam(IR,fs,flag)
  public static void lundebyParam(double[] ir, double irLf[], double fs, int directSound,
      int firstReflection, PrintStream outTable, String graphFolder) {
    // // banda = filtros(IR,fs);
    double[][] banda = filtros(ir, fs, null);
    // // t = size(banda,2);
    int t = banda.length;

    double[][] bandaLf = null;
    if (irLf != null) bandaLf = filtros(irLf, fs, null);

    //double s[][] = new double[12][banda.length];

    PrintStream outGraphs[] = channelFiles(graphFolder);

    Map<String, Parameter> params = new HashMap<String, Parameter>();
    for (int i = 0; i < energ.length; i++) {
      params.put(energ[i], new Parameter(energ[i], energUnit[i]));
    }
    if (irLf != null) {
      params.put("LF", new Parameter("LF", ""));
    }
    for (int i = 0; i < reverb.length; i++) {
      params.put(reverb[i], new Parameter(reverb[i], "s"));
    }

    for (int n = 0; n < t; n++) {
      // // for n = 1:t
      // // s(1,n) = ceil(1000*2^(n-5));

      // // comeco = inicio(banda(:,n));
      int comeco = inicio(banda[n]);
      // // fim = lundeby(banda(comeco:end,n),fs,flag);
      //int fim = (int) lundeby(Util.subArray(banda[n], comeco), fs)[0];
      int fim = comeco+ (int) lundeby(Util.subArray(banda[n], comeco), fs)[0];
      // // title(['Ponto de Cruzamento - Banda ',num2str(s(1,n))])
      // // if n == t-2
      // // title('Ponto de Cruzamento - Compensacao A ')
      // // elseif n == t-1
      // // title('Ponto de Cruzamento - Compensacao C ')
      // // elseif n == t
      // // title('Ponto de Cruzamento - Linear ')
      // // end
      // //
      // // aux = banda(comeco:fim,n).^2;
      double aux[] = Util.powLLL(Util.subArray(banda[n], comeco, fim), 2);
      // // [s(2,n),s(3,n),s(4,n),s(5,n),s(6,n)] = energeticos(aux,fs);
      // // [s(7,n),s(8,n),s(9,n),s(10,n)] = reverberacao(aux,fs,flag);
      double en[] = energeticos(aux, fs, directSound, firstReflection);

      for (int i = 0; i < energ.length - 1; i++) {
        params.get(energ[i]).val[n] = en[i];
      }

      params.get("CT").val[n] = en[en.length - 1] * 1000;

      if (irLf != null) {
        int comecoLf = inicio(bandaLf[n]);
        int fimLf = comecoLf + (int) lundeby(Util.subArray(bandaLf[n], comecoLf), fs)[0];
        //double auxLf[] = Util.powLLL(Util.subArray(bandaLf[n], comecoLf, fimLf + 1), 2);
        System.out.println(comecoLf + " " + fimLf);
        double auxLf[] = Util.powLLL(Util.subArray(bandaLf[n], comecoLf, fimLf), 2);
        
        params.get("LF").val[n] = lf(aux, auxLf, fs);
      }

      double rev[] = reverberacao(outGraphs[n], aux, fs);
      for (int i = 0; i < rev.length; i++) {
        params.get(reverb[i]).val[n] = rev[i];
      }
      // // title(['Curva de Decaimento - Banda ',num2str(s(1,n))])
      // // if n == t-2
      // // title('Curva de Decaimento - Compensacao A ')
      // // elseif n == t-1
      // // title('Curva de Decaimento - Compensacao C ')
      // // elseif n == t
      // // title('Curva de Decaimento - Linear ')
      // // end
      // //
      // // end
    }
    // // if nargout == 1
    // // saida = s;
    // // saida(1,t-2) = (['A']);
    // // saida(1,t-1) = (['C']);
    // // saida(1,t) = (['L']);
    // // else
    // // tabela(s,size(banda,2))
    // // end

    List<String> pOrder = new ArrayList<String>();
    for (String pName : energ)
      pOrder.add(pName);
    if (irLf != null) {
      pOrder.add("LF");
    }
    for (String pName : reverb)
      pOrder.add(pName);

    double br = bassRatio(params.get("T20"));
    double tr = trebleRatio(params.get("T20"));

    tabela(outTable, params, pOrder, banda.length, br, tr, (int) Math
        .round((firstReflection - directSound) / fs * 1000));

    // tabela(outTable, s, banda.length,
    // (int)Math.round((firstReflection-directSound)/fs*1000));
  }

  // // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  // // % hrtparam.m
  // // %
  // // %Esta funcao calcula os diversos parametros acusticos de uma sala. O
  // // %tratamento do sinal e feito pelo metodo de Hirata.
  // // %
  // // %[s]=parametros(IR1,IR2,fs,flag)
  // // %
  // // %Tem como entrada as duas resposta impulsiva e a taxa de amostragem. As
  // // %duas respostas devem ser obtidas sob a mesma configuracao do sistema de
  // // %medida.
  // // %Tem como saida uma arquivo de texto com o valor dos parametros para as
  // // %diversas bandas de frequencia. Se desejado, fornece uma matrix com
  // estes
  // // %valores, onde a primeira linha sao as frequencias centrais, ao inves do
  // // %arquivo de texto.
  // // %A variavel flag = 1 indica se deseja ver os graficos ca curva de
  // Schroeder
  // //
  // //
  // // function [saida]=hrtparam(IR1,IR2,fs,flag)
  public static void hirataParam(double[] ir1, double ir2[], double fs, int directSound,
      int firstReflection, PrintStream outTable, String graphFolder) {
    // //
    // // if size(IR1,1) < size(IR2,1) %condiciona o tamanho das sequencias
    // // IR2 = IR2(1:size(IR1,1));
    // // elseif size(IR1,1) > size(IR2,1)
    // // IR1 = IR1(1:length(IR2));
    // // end
    if (ir1.length < ir2.length) {
      ir2 = Util.subArray(ir2, 0, ir1.length);
    } else if (ir1.length > ir2.length) {
      ir1 = Util.subArray(ir1, 0, ir2.length);
    }

    // //
    // // banda1 = filtros(IR1,fs);
    // // banda2 = filtros(IR2,fs);
    double[][] banda1 = filtros(ir1, fs, null);
    double[][] banda2 = filtros(ir2, fs, null);

    // //
    // // for n = 1:size(banda1,2)
    // // s(1,n) = ceil(1000*2^(n-5));
    // // aux = banda1(:,n).*banda2(:,n);
    // // comeco = inicio(aux);
    // // aux = aux(comeco:end);
    // // [s(2,n),s(3,n),s(4,n),s(5,n),s(6,n)] = energeticos(aux,fs);
    // // [s(7,n),s(8,n),s(9,n),s(10,n)] = reverberacao(aux,fs,flag);
    // // end

    PrintStream outGraphs[] = channelFiles(graphFolder);
    Map<String, Parameter> params = new HashMap<String, Parameter>();
    for (int i = 0; i < energ.length; i++) {
      params.put(energ[i], new Parameter(energ[i], energUnit[i]));
    }
    for (int i = 0; i < reverb.length; i++) {
      params.put(reverb[i], new Parameter(reverb[i], "s"));
    }

    for (int n = 0; n < banda1.length; n++) {
      // // s(1,n) = ceil(1000*2^(n-5));

      double aux[] = Util.multLLL(banda1[n], banda2[n]);

      // // comeco = inicio(banda(:,n));
      int comeco = inicio(banda1[n]);

      aux = Util.subArray(aux, comeco);

      // // [s(2,n),s(3,n),s(4,n),s(5,n),s(6,n)] = energeticos(aux,fs);
      // // [s(7,n),s(8,n),s(9,n),s(10,n)] = reverberacao(aux,fs,flag);
      double en[] = energeticos(aux, fs, directSound, firstReflection);

      for (int i = 0; i < energ.length - 1; i++) {
        params.get(energ[i]).val[n] = en[i];
      }

      params.get("CT").val[n] = en[en.length - 1] * 1000;

      double rev[] = reverberacao(outGraphs[n], aux, fs);
      for (int i = 0; i < rev.length; i++) {
        params.get(reverb[i]).val[n] = rev[i];
      }

    }
    // //
    // // if nargout == 1
    // // saida = s;
    // // saida(1,9) = (['A']);
    // // saida(1,10) = (['C']);
    // // saida(1,11) = (['L']);
    // // else
    // // tabela(s,size(banda1,2))
    // // end
    // tabela(out, s, banda1.length,
    // (int)Math.round((firstReflection-directSound)/fs*1000));

    List<String> pOrder = new ArrayList<String>();
    for (String pName : energ)
      pOrder.add(pName);
    for (String pName : reverb)
      pOrder.add(pName);

    double br = bassRatio(params.get("T20"));
    double tr = trebleRatio(params.get("T20"));

    tabela(outTable, params, pOrder, banda1.length, br, tr, (int) Math
        .round((firstReflection - directSound) / fs * 1000));
  }

  public static PrintStream[] channelFiles(String graphFolder) {
    PrintStream res[] = new PrintStream[11];
    String separator = System.getProperty("file.separator", "/");
    for (int i = 0; i < res.length; i++) {
      try {
        res[i] = new PrintStream(new FileOutputStream(graphFolder + separator
            + CHANNEL_NAMES[i] + ".txt"));
      } catch (FileNotFoundException e) {
        // eh?
        e.printStackTrace();
      }
    }
    return res;
  }

  // // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  // // %filtros.m
  // // %
  // // % [bandas] = filtros(sinal,fs)
  // // %
  // // %Banco de Filtros 1/8 e de compensacao A e C. A ultima linha apresenta a
  // // %resposta impulsiva sem ser filtrada.
  // // %Fs = frequencia de amostragem
  // // %Os filtros de compensacao foram extraidos do tool box Octave.
  // // %Realiza a filtragem em bandas de oitava, como recomendado pela norma
  // // %IEC 61620.
  // //
  // // function [bandas] = filtros(sinal,fs)
  public static double[][] filtros(double[] signal, double fs,
      IProgressMonitor monitor) {
    monitor = Util.monitorFor(monitor);

    monitor.beginTask("filtering", 100);

    double[][] res = new double[11][];

    // // warning off MATLAB:nearlySingularMatrix
    // //
    // // fc = 1000 * 2.^[-4 -3 -2 -1 0 1 2 3]; %frequencia central [63 125 250
    // 500
    // // 1k 2k 4k 8k]
    // // n = 3; %ordem do filtro butterworth

    double fc[] = { 62.5, 125, 250, 500, 1000, 2000, 4000, 8000 };
    // int n = 3;

    //    
    // // delta = inv(sqrt(2)*(sqrt(2)-1)^(1/2/n)); %Correcao para filtro causal
    // // a = (delta+sqrt(delta^2+4))/2;

    // double delta = 8.1899443301849650e-01; // for n=3
    // double a = (delta + Math.sqrt(Math.pow(delta,2)+4))/2;

    //    
    // // %-------------63--------------
    // // [b63,a63] = butter(n,[fc(1)/(fs/2)/a,fc(1)/(fs/2)*a]);
    // // bandas(:,1) = filtfilt(b63,a63,sinal);
    // //
    // // %-------------125--------------
    // // [b125,a125] = butter(n,[fc(2)/(fs/2)/a,fc(2)/(fs/2)*a]);
    // // bandas(:,2) = filtfilt(b125,a125,sinal);
    // //
    // // %-------------250--------------
    // // [b250,a250] = butter(n,[fc(3)/(fs/2)/a,fc(3)/(fs/2)*a]);
    // // bandas(:,3) = filtfilt(b250,a250,sinal);
    // //
    // // %-------------500--------------
    // // [b500,a500] = butter(n,[fc(4)/(fs/2)/a,fc(4)/(fs/2)*a]);
    // // bandas(:,4) = filtfilt(b500,a500,sinal);
    // //
    // // %-------------1000--------------
    // // [b1000,a1000] = butter(n,[fc(5)/(fs/2)/a,fc(5)/(fs/2)*a]);
    // // bandas(:,5) = filtfilt(b1000,a1000,sinal);
    // //
    // // %-------------2000--------------
    // // [b2000,a2000] = butter(n,[fc(6)/(fs/2)/a,fc(6)/(fs/2)*a]);
    // // bandas(:,6) = filtfilt(b2000,a2000,sinal);
    // //
    // // %-------------4000--------------
    // // [b4000,a4000] = butter(n,[fc(7)/(fs/2)/a,fc(7)/(fs/2)*a]);
    // // bandas(:,7) = filtfilt(b4000,a4000,sinal);
    // //
    // // %-------------8000--------------
    // // [b8000,a8000] = butter(n,[fc(8)/(fs/2)/a,fc(8)/(fs/2)*a]);
    // // bandas(:,8) = filtfilt(b8000,a8000,sinal);

    for (int i = 0; i < fc.length; i++) {
      Filter f = FilterBank.get1d8(fc[i], fs);
      System.out.println(f);
      res[i] = Filter.filtfilt(f.b, f.a, signal);
      monitor.worked(10);
    }

    //    
    // // %-------------Compencao A-------
    // // f1 = 20.598997;
    // // f2 = 107.65265;
    // // f3 = 737.86223;
    // // f4 = 12194.217;
    // // A1000 = 1.9997;
    // // NUMs = [ (2*pi*f4)^2*(10^(A1000/20)) 0 0 0 0 ];
    // // DENs = conv([1 +4*pi*f4 (2*pi*f4)^2],[1 +4*pi*f1 (2*pi*f1)^2]);
    // // DENs = conv(conv(DENs,[1 2*pi*f3]),[1 2*pi*f2]);
    // // [B,A] = bilinear(NUMs,DENs,fs);
    // // bandas(:,9) = filter(B,A,sinal);

    Filter f = FilterBank.getComp("a", fs);
    res[8] = Filter.filter(f.b, f.a, signal);
    monitor.worked(10);

    //
    // // %-------------Compencao C-------
    // // f1 = 20.598997;
    // // f4 = 12194.217;
    // // C1000 = 0.0619;
    // // pi = 3.14159265358979;
    // // NUMs = [ (2*pi*f4)^2*(10^(C1000/20)) 0 0 ];
    // // DENs = conv([1 +4*pi*f4 (2*pi*f4)^2],[1 +4*pi*f1 (2*pi*f1)^2]);
    // // [B,A] = bilinear(NUMs,DENs,fs);
    // // bandas(:,10) = filter(B,A,sinal);

    f = FilterBank.getComp("c", fs);
    res[9] = Filter.filter(f.b, f.a, signal);
    monitor.worked(10);

    //    
    // // %-------------Linear------------
    // // bandas(:,11) = sinal;

    res[10] = signal;

    monitor.done();
    return res;
  }

}

class Parameter {
  public String name;
  public String unit;
  public double val[];

  public Parameter(String name, String unit) {
    this.name = name;
    this.unit = unit;
    this.val = new double[11];
  }
}