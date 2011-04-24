package cscg.model.objects;

import cern.colt.matrix.linalg.Algebra;


/**
 * Operace s body a vektory.
 * @author Tomáš Režnar
 */
public class PointOperations {

	public static final Algebra alg=Algebra.DEFAULT;

	/**
	 * Pøesune bod p o vektor [x,y,z].
	 * @return Pøesunutý bod.
	 */
	public static IPoint3f move(IPoint3f p,float x,float y,float z)
	{
		return new Point3f(p.getX()+x, p.getY()+y, p.getZ()+z);
	}
	/**
	 * Pøesune bod p o vektor [x,y,z].
	 * @return Pøesunutý bod.
	 */
	public static IPoint4f move(IPoint4f p,float x,float y,float z)
	{
		return new Point4f(p.getX()+x, p.getY()+y, p.getZ()+z,p.getW());
	}

	/**
	 * Pøesune bod p o vektor.
	 * @param vector Vector se tøemi prvky.
	 */
	public static IPoint3f move(IPoint3f p,float[] vector)
	{
		return new Point3f(p.getX()+vector[0], p.getY()+vector[1], p.getZ()+vector[2]);
	}

	/**
	 * Pøesune bod p o vektor.
	 * @param vector Vector se tøemi prvky.
	 */
	public static IPoint4f move(IPoint4f p,float[] vector)
	{
		return new Point4f(p.getX()+vector[0], p.getY()+vector[1], p.getZ()+vector[2], p.getW());
	}

	/**
	 * Vypoète vektor z bodu from do bodu to.
	 * @return Pole floatu: [dif x, dif y, dif z].
	 */
	public static float[] directionVector(IPoint3f from,IPoint3f to)
	{
		return new float[]{to.getX()-from.getX(),
			to.getY()-from.getY(),
			to.getZ()-from.getZ()
		};
	}

	/**
	 * Vypoète vzdálenost dvou bodù.
	 */
	public static double distance(IPoint3f from,IPoint3f to)
	{
		float[] vector=directionVector(from, to);
		return sizeVector3(vector);
	}

	/**
	 * Spoète velikost vektoru se tøemi složkami.
	 * @param vector Pole tøí složek.
	 */
	public static double sizeVector3(float[] vector)
	{
		return Math.sqrt(
			Math.pow(vector[0], 2)+
			Math.pow(vector[1], 2)+
			Math.pow(vector[2], 2)
			);
	}
	
	/**
	 * Spoète velikost vektoru se tøemi složkami.
	 * @param vector Pole tøí složek.
	 */
	public static double sizeVector3(double[] vector)
	{
		return Math.sqrt(
			Math.pow(vector[0], 2)+
			Math.pow(vector[1], 2)+
			Math.pow(vector[2], 2)
			);
	}

	/**
	 * Spoète velikost vektoru
	 * @param vector Pole tøí složek.
	 */
	public static double size(IPoint3f vector)
	{
		return Math.sqrt(
			Math.pow(vector.getX(), 2)+
			Math.pow(vector.getY(), 2)+
			Math.pow(vector.getZ(), 2)
			);
	}

	/**
	 * Vytvoøí zrcadlový obraz bodu dle støedového bodu.
	 * @param point Pùvodní bod pro zrcadlení.
	 * @param centerPoint Støedový bod, dle kterého vznikne obraz.
	 * @return Zrcadlený bod.
	 */
	public static IPoint3f mirror(IPoint3f point, IPoint3f centerPoint)
	{
		return new Point3f(
			centerPoint.getX()+centerPoint.getX()-point.getX(),
			centerPoint.getY()+centerPoint.getY()-point.getY(),
			centerPoint.getZ()+centerPoint.getZ()-point.getZ()
		);
	}

	/**
	 * Vytvoøí zrcadlový obraz bodu dle støedového bodu.
	 * @param point Pùvodní bod pro zrcadlení.
	 * @param centerPoint Støedový bod, dle kterého vznikne obraz.
	 * @return Zrcadlený bod.
	 */
	public static IPoint4f mirror(IPoint4f point, IPoint3f centerPoint)
	{
		return new Point4f(
			centerPoint.getX()+centerPoint.getX()-point.getX(),
			centerPoint.getY()+centerPoint.getY()-point.getY(),
			centerPoint.getZ()+centerPoint.getZ()-point.getZ(),
			point.getW()
		);
	}

	/**
	 * Porovnání bodù dle souøadnic.
	 * @return Vrátí true pokud jsou body na stejných souøadnicích.
	 */
	public static boolean compareCoords(IPoint3f o1,IPoint3f o2)
	{
		if(o1.getX()==o2.getX() && o1.getY()==o2.getY() && o1.getZ()==o2.getZ())
		{
			return true;
		}
		return false;
	}

	/**
	 * Porovnání bodù dle souøadnic a váhy bodu.
	 * @return Vrátí true pokud jsou body na stejných souøadnicích a mají stejnou váhu.
	 */
	public static boolean compareCoordsAndWeight(IPoint4f o1,IPoint4f o2)
	{
		if(PointOperations.compareCoords(o1, o2) && o1.getW()==o2.getW())
		{
			return true;
		}
		return false;
	}

	/**
	 * Spoète støed úseèky zadané dvìmi body.
	 */
	public static IPoint3f pivot(IPoint3f p1,IPoint3f p2)
	{
		return new Point3f(
			(p1.getX()+p2.getX())*0.5f,
			(p1.getY()+p2.getY())*0.5f,
			(p1.getZ()+p2.getZ())*0.5f
			);
	}

	/**
	 * Zjistí jestli jsou všechny body na jedné pøímce.
	 * @return True když jsou na pøímce.
	 */
	public static boolean isLine(IPoint3f p1,IPoint3f p2,IPoint3f p3)
	{
		//pokud jsou 2 body shodné, pak je to pøímka
		if(compareCoords(p1, p2) || compareCoords(p3, p2) || compareCoords(p3, p1))
		{
			return true;
		}
		float direction[]=directionVector(p1, p2);//smìrový vektor pøímky
		float t=(p3.getX()-p1.getX())/direction[0];//parametr t
		//pod p3 leží na pøímce p1p2 pokud po dosazení parametru t vyjde bod p3
		return (p3.getY()==p1.getY()+t*direction[1])
		  && (p3.getZ()==p1.getZ()+t*direction[2]);
	}

	/**
	 * Spoète nejbližší bod na pøímce lineP1 lineP2 k bodu point.
	 * Poèítáno algoritmem spoètení prùseèíku pøímky s rovinou, zadanou bodem point, kolmou k pøímce.
	 * @param lineP1 První bod pøímky.
	 * @param lineP2 Druhý bod pøímky.
	 * @param point Bod od kterého se urèuje nejkratší vzdálenost k pøímce.
	 * @return Vrátí bod na pøímce. Speciální pøípady:<br />
	 * -body lineP1 a lineP2 jsou shodné, pak je vrácen bod o suøadnicích lineP1.<br />
	 * -bod point leží na pøímce=>je vrácen bod point.
	 */
	public static IPoint3f intersection(IPoint3f lineP1,IPoint3f lineP2,IPoint3f point)
	{
		if(compareCoords(lineP1, lineP2)==true)//pøímka neexistuje-je to pouze bod
		{
			return clone(lineP1);
		}
		if(isLine(lineP1, lineP2, point))//bod leží na pøímce
		{
			return clone(point);
		}
		//ostatní pøípady-nutno spoèíst prùseèík
		/*
		 * Matematický postup:
		 * 1)parametrické rovnice pøímky: (smìrový vektor pøímnky je v poli direction)
		 *	x=lineP1.getX()+t*direction[0];
		 *	y=lineP1.getY()+t*direction[1];
		 *	z=lineP1.getZ()+t*direction[2];
		 * 2)Obecná rovnice roviny kolmé k pøímce: (normálový vektor roviny je v poli direction)
		 *	direction[0]*(x-point.getX())+
		 *	direction[1]*(y-point.getY())+
		 *	direction[2]*(z-point.getZ())=0
		 * 3)dosazení rovnic pøímky do rovnice roviny a vyjádøení parametru t:
		 *	t=(
		 *			direction[0]*(-lineP1.getX()+point.getX())+
		 *			direction[1]*(-lineP1.getY()+point.getY())+
		 *			direction[2]*(-lineP1.getZ()+point.getZ())
		 *		)
		 *	/
		 *	(direction[0]*direction[0]+direction[1]*direction[1]+direction[2]*direction[2])
		 * 4)dosazením spoèteného parametru t do rovnice pøímky dostaneme prùseèík roviny s pøímkou což je hledaný bod
		 */
		float[] direction=directionVector(lineP1, lineP2);
		float t=
			(
				direction[0]*(-lineP1.getX()+point.getX())+
				direction[1]*(-lineP1.getY()+point.getY())+
				direction[2]*(-lineP1.getZ()+point.getZ())
			)
			/(direction[0]*direction[0]+direction[1]*direction[1]+direction[2]*direction[2]);
		return new Point3f(
			lineP1.getX()+t*direction[0],
			lineP1.getY()+t*direction[1],
			lineP1.getZ()+t*direction[2]
		);
	}

	/**
	 * Klonování bodu.
	 */
	public static IPoint3f clone(IPoint3f p)
	{
		return new Point3f(p.getX(), p.getY(), p.getZ());
	}
	/**
	 * Klonování bodu.
	 */
	public static IPoint4f clone(IPoint4f p)
	{
		return new Point4f(p.getX(), p.getY(), p.getZ(),p.getW());
	}
	/**
	 * Normalizace vektoru se tøemi složkami.
	 * @param vector Vektor jehož délka je nenulová, musí mít tøi složky.
	 * @return Normalizovaný vektor jehož velikost je rovna 1.
	 * @throws IllegalArgumentException Pokud je vektor nulový nebo nemá správnou délku.
	 */
	public static float[] normalizeVector3(float[] vector) throws IllegalArgumentException
	{
		if(vector==null || vector.length!=3)
		{
			throw new IllegalArgumentException("Vektor nemá 3 složky.");
		}
		float size=(float)sizeVector3(vector);
		if(size==0.0f)
		{
			throw new IllegalArgumentException("Vektor je nulové délky.");
		}
		return new float[]{
			vector[0]/size,
			vector[1]/size,
			vector[2]/size
		};
	}
	/**
	 * Normalizace vektoru.
	 * @param vector Vektor jehož délka je nenulová.
	 * @return Normalizovaný vektor jehož velikost je rovna 1.
	 * @throws IllegalArgumentException Pokud je vektor nulový.
	 */
	public static IPoint3f normalize(IPoint3f vector) throws IllegalArgumentException
	{
		if(vector==null)
		{
			throw new IllegalArgumentException("Vektor je null.");
		}
		float size=(float)size(vector);
		if(size==0.0f)
		{
			throw new IllegalArgumentException("Vektor je nulové délky.");
		}
		return new Point3f(
			vector.getX()/size,
			vector.getY()/size,
			vector.getZ()/size
		);
	}
	/**
	 * Rotace vektoru se tøemi složkami kolem osy zadané vektorem.
	 * @param axis Vektor osy rotace. Pokud nebude normalizovaný, dojde kromì rotace taky ke zvìtšení.
	 * @param angle Úhel rotace ve stupních.
	 * @param vector Rotovaný vektor.
	 * @return Vrátí rotovaný vektor v poli velikosti 3.
	 */
	public static float[] axisRotationVector3(IPoint3f axis,double angle,IPoint3f vector)
	{
		float[] vectorMatrix=new float[]{vector.getX(),vector.getY(),vector.getZ()};
		angle=Math.toRadians(angle);
		float cos=(float)Math.cos(angle);
		float sin=(float)Math.sin(angle);
		float[] axisMatrix = new float[]{axis.getX(),axis.getY(),axis.getZ()};//osa rotace

		//výpoèet rotace dle Rodrigues' vìty o rotaci
		float[] v=vectorMatrix;
		float[] vRot=new float[3];

		vRot[0]=v[0]*cos;
		vRot[1]=v[1]*cos;
		vRot[2]=v[2]*cos;

		float[] vXAxis=new float[]{//vektorový souèin axis x v
			axisMatrix[1]*v[2]-axisMatrix[2]*v[1],
			axisMatrix[2]*v[0]-axisMatrix[0]*v[2],
			axisMatrix[0]*v[1]-axisMatrix[1]*v[0]
		};
		vRot[0]+=sin*vXAxis[0];
		vRot[1]+=sin*vXAxis[1];
		vRot[2]+=sin*vXAxis[2];

		double axisScalarV=axisMatrix[0]*v[0]
			+axisMatrix[1]*v[1]
			+axisMatrix[2]*v[2];//skalární souèin vektorù axis a v
		vRot[0]+=(1-cos)*axisMatrix[0]*axisScalarV;
		vRot[1]+=(1-cos)*axisMatrix[1]*axisScalarV;
		vRot[2]+=(1-cos)*axisMatrix[2]*axisScalarV;

		return vRot;
	}

	/**
	 * Zmìna veliskosti vectoru
	 * @param vector Pole velikost 3.
	 * @param scale Násobitel zmìny.
	 * @return Zmìnìný vektor.
	 */
	public static float[] scaleVector3(float[] vector, float scale)
	{
		return new float[]{
			vector[0]*scale,
			vector[1]*scale,
			vector[2]*scale
		};
	}
}
