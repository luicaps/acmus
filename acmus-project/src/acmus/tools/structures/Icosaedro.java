package acmus.tools.structures;

import java.util.ArrayList;

public class Icosaedro {
	/* lista dos v�rtices, arestas e faces do icosaedro */
	ArrayList vertices = new ArrayList();
	ArrayList arestas = new ArrayList();
	ArrayList faces = new ArrayList();
	/**
	 * 
	 */
	public Icosaedro() {
		// TODO Auto-generated constructor stub
		 double ouro,mod;

		 for(int j = 0; j < 30; j++) {
			 arestas.add(new Edge());
			 if(j < 20) {
				 faces.add(new Face());
			 }
		 }
		  ouro = (1 + Math.sqrt(5))/2;

		  /* V�rtices do Icosaedro */
		  mod = Math.sqrt(ouro*ouro + 1);
		  vertices.add(Triade.divideVetorEscalar(auxInicIco(1,ouro,0),mod));
		  vertices.add(Triade.divideVetorEscalar(auxInicIco(-1,ouro,0),mod));
		  vertices.add(Triade.divideVetorEscalar(auxInicIco(1,-ouro,0),mod));
		  vertices.add(Triade.divideVetorEscalar(auxInicIco(-1,-ouro,0),mod));
		  vertices.add(Triade.divideVetorEscalar(auxInicIco(0,1,ouro),mod));
		  vertices.add(Triade.divideVetorEscalar(auxInicIco(0,-1,ouro),mod));
		  vertices.add(Triade.divideVetorEscalar(auxInicIco(0,1,-ouro),mod));
		  vertices.add(Triade.divideVetorEscalar(auxInicIco(0,-1,-ouro),mod));
		  vertices.add(Triade.divideVetorEscalar(auxInicIco(ouro,0,1),mod));
		  vertices.add(Triade.divideVetorEscalar(auxInicIco(ouro,0,-1),mod));
		  vertices.add(Triade.divideVetorEscalar(auxInicIco(-ouro,0,1),mod));
		  vertices.add(Triade.divideVetorEscalar(auxInicIco(-ouro,0,-1),mod));
		  
		  /* Arestas do Icosaedro */
		  ((Edge)arestas.get(0)).setP1((Triade)vertices.get(4));
		  ((Edge)arestas.get(1)).setP1((Triade)vertices.get(4));
		  ((Edge)arestas.get(2)).setP1((Triade)vertices.get(0));
		  ((Edge)arestas.get(3)).setP1((Triade)vertices.get(6));
		  ((Edge)arestas.get(4)).setP1((Triade)vertices.get(6));
		  ((Edge)arestas.get(5)).setP1((Triade)vertices.get(5));
		  ((Edge)arestas.get(6)).setP1((Triade)vertices.get(5));
		  ((Edge)arestas.get(7)).setP1((Triade)vertices.get(2));
		  ((Edge)arestas.get(8)).setP1((Triade)vertices.get(7));
		  ((Edge)arestas.get(9)).setP1((Triade)vertices.get(7));
		  ((Edge)arestas.get(10)).setP1((Triade)vertices.get(0));
		  ((Edge)arestas.get(11)).setP1((Triade)vertices.get(0));
		  ((Edge)arestas.get(12)).setP1((Triade)vertices.get(8));
		  ((Edge)arestas.get(13)).setP1((Triade)vertices.get(2));
		  ((Edge)arestas.get(14)).setP1((Triade)vertices.get(2));
		  ((Edge)arestas.get(15)).setP1((Triade)vertices.get(1));
		  ((Edge)arestas.get(16)).setP1((Triade)vertices.get(1));
		  ((Edge)arestas.get(17)).setP1((Triade)vertices.get(10));
		  ((Edge)arestas.get(18)).setP1((Triade)vertices.get(3));
		  ((Edge)arestas.get(19)).setP1((Triade)vertices.get(3));
		  ((Edge)arestas.get(20)).setP1((Triade)vertices.get(9));
		  ((Edge)arestas.get(21)).setP1((Triade)vertices.get(9));
		  ((Edge)arestas.get(22)).setP1((Triade)vertices.get(6));
		  ((Edge)arestas.get(23)).setP1((Triade)vertices.get(11));
		  ((Edge)arestas.get(24)).setP1((Triade)vertices.get(11));
		  ((Edge)arestas.get(25)).setP1((Triade)vertices.get(8));
		  ((Edge)arestas.get(26)).setP1((Triade)vertices.get(8));
		  ((Edge)arestas.get(27)).setP1((Triade)vertices.get(4));
		  ((Edge)arestas.get(28)).setP1((Triade)vertices.get(10));
		  ((Edge)arestas.get(29)).setP1((Triade)vertices.get(10));
		  
		  ((Edge)arestas.get(0)).setP2((Triade)vertices.get(0));
		  ((Edge)arestas.get(1)).setP2((Triade)vertices.get(1));
		  ((Edge)arestas.get(2)).setP2((Triade)vertices.get(1));
		  ((Edge)arestas.get(3)).setP2((Triade)vertices.get(0));
		  ((Edge)arestas.get(4)).setP2((Triade)vertices.get(1));
		  ((Edge)arestas.get(5)).setP2((Triade)vertices.get(2));
		  ((Edge)arestas.get(6)).setP2((Triade)vertices.get(3));
		  ((Edge)arestas.get(7)).setP2((Triade)vertices.get(3));
		  ((Edge)arestas.get(8)).setP2((Triade)vertices.get(2));
		  ((Edge)arestas.get(9)).setP2((Triade)vertices.get(3));
		  ((Edge)arestas.get(10)).setP2((Triade)vertices.get(8));
		  ((Edge)arestas.get(11)).setP2((Triade)vertices.get(9));
		  ((Edge)arestas.get(12)).setP2((Triade)vertices.get(9));
		  ((Edge)arestas.get(13)).setP2((Triade)vertices.get(8));
		  ((Edge)arestas.get(14)).setP2((Triade)vertices.get(9));
		  ((Edge)arestas.get(15)).setP2((Triade)vertices.get(10));
		  ((Edge)arestas.get(16)).setP2((Triade)vertices.get(11));
		  ((Edge)arestas.get(17)).setP2((Triade)vertices.get(11));
		  ((Edge)arestas.get(18)).setP2((Triade)vertices.get(10));
		  ((Edge)arestas.get(19)).setP2((Triade)vertices.get(11));
		  ((Edge)arestas.get(20)).setP2((Triade)vertices.get(6));
		  ((Edge)arestas.get(21)).setP2((Triade)vertices.get(7));
		  ((Edge)arestas.get(22)).setP2((Triade)vertices.get(7));
		  ((Edge)arestas.get(23)).setP2((Triade)vertices.get(6));
		  ((Edge)arestas.get(24)).setP2((Triade)vertices.get(7));
		  ((Edge)arestas.get(25)).setP2((Triade)vertices.get(4));
		  ((Edge)arestas.get(26)).setP2((Triade)vertices.get(5));
		  ((Edge)arestas.get(27)).setP2((Triade)vertices.get(5));
		  ((Edge)arestas.get(28)).setP2((Triade)vertices.get(4));
		  ((Edge)arestas.get(29)).setP2((Triade)vertices.get(5));
		  
		  ((Face)faces.get(0)).setE1((Edge)arestas.get(0));
		  ((Face)faces.get(1)).setE1((Edge)arestas.get(3));
		  ((Face)faces.get(2)).setE1((Edge)arestas.get(5));
		  ((Face)faces.get(3)).setE1((Edge)arestas.get(8));
		  ((Face)faces.get(4)).setE1((Edge)arestas.get(10));
		  ((Face)faces.get(5)).setE1((Edge)arestas.get(13));
		  ((Face)faces.get(6)).setE1((Edge)arestas.get(15));
		  ((Face)faces.get(7)).setE1((Edge)arestas.get(18));
		  ((Face)faces.get(8)).setE1((Edge)arestas.get(20));
		  ((Face)faces.get(9)).setE1((Edge)arestas.get(23));
		  ((Face)faces.get(10)).setE1((Edge)arestas.get(25));
		  ((Face)faces.get(11)).setE1((Edge)arestas.get(28));
		  ((Face)faces.get(12)).setE1((Edge)arestas.get(3));
		  ((Face)faces.get(13)).setE1((Edge)arestas.get(0));
		  ((Face)faces.get(14)).setE1((Edge)arestas.get(4));
		  ((Face)faces.get(15)).setE1((Edge)arestas.get(1));
		  ((Face)faces.get(16)).setE1((Edge)arestas.get(14));
		  ((Face)faces.get(17)).setE1((Edge)arestas.get(13));
		  ((Face)faces.get(18)).setE1((Edge)arestas.get(9));
		  ((Face)faces.get(19)).setE1((Edge)arestas.get(6));
		  
		  ((Face)faces.get(0)).setE2((Edge)arestas.get(1));
		  ((Face)faces.get(1)).setE2((Edge)arestas.get(4));
		  ((Face)faces.get(2)).setE2((Edge)arestas.get(6));
		  ((Face)faces.get(3)).setE2((Edge)arestas.get(9));
		  ((Face)faces.get(4)).setE2((Edge)arestas.get(11));
		  ((Face)faces.get(5)).setE2((Edge)arestas.get(14));
		  ((Face)faces.get(6)).setE2((Edge)arestas.get(16));
		  ((Face)faces.get(7)).setE2((Edge)arestas.get(19));
		  ((Face)faces.get(8)).setE2((Edge)arestas.get(21));
		  ((Face)faces.get(9)).setE2((Edge)arestas.get(24));
		  ((Face)faces.get(10)).setE2((Edge)arestas.get(26));
		  ((Face)faces.get(11)).setE2((Edge)arestas.get(29));
		  ((Face)faces.get(12)).setE2((Edge)arestas.get(11));
		  ((Face)faces.get(13)).setE2((Edge)arestas.get(10));
		  ((Face)faces.get(14)).setE2((Edge)arestas.get(16));
		  ((Face)faces.get(15)).setE2((Edge)arestas.get(15));
		  ((Face)faces.get(16)).setE2((Edge)arestas.get(8));
		  ((Face)faces.get(17)).setE2((Edge)arestas.get(5));
		  ((Face)faces.get(18)).setE2((Edge)arestas.get(19));
		  ((Face)faces.get(19)).setE2((Edge)arestas.get(18));
	}
	/**
	 * @return Returns the arestass.
	 */
	public ArrayList getarestas() {
		return arestas;
	}
	/**
	 * @param arestas The arestass to set.
	 */
	public void setarestass(ArrayList arestass) {
		this.arestas = arestas;
	}
	/**
	 * @return Returns the faces.
	 */
	public ArrayList getFaces() {
		return faces;
	}
	/**
	 * @param faces The faces to set.
	 */
	public void setFaces(ArrayList faces) {
		this.faces = faces;
	}
	/**
	 * @return Returns the vertices.
	 */
	public ArrayList getVertices() {
		return vertices;
	}
	/**
	 * @param vertices The vertices to set.
	 */
	public void setVertices(ArrayList vertices) {
		this.vertices = vertices;
	}
	
	public static Triade auxInicIco(double x, double y, double z){
		  Triade aux = new Triade();

		  aux.x = x;
		  aux.y = y;
		  aux.z = z;
		  return aux;
		}

}
