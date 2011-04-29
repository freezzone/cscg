package cscg.model.objects;

import cern.colt.matrix.linalg.Algebra;


/**
 * Operace s body a vektory.
 * @author Tomáš Režnar
 */
public class PointOperations {

	public static final Algebra alg=Algebra.DEFAULT;

	/**
	 * Přesune bod p o vektor [x,y,z].
	 * @return Přesunutý bod.
	 */
	public static IPoint3f move(IPoint3f p,float x,float y,float z)
	{
		return new Point3f(p.getX()+x, p.getY()+y, p.getZ()+z);
	}
	/**
	 * Přesune bod p o vektor [x,y,z].
	 * @return Přesunutý bod.
	 */
	public static IPoint4f move(IPoint4f p,float x,float y,float z)
	{
		return new Point4f(p.getX()+x, p.getY()+y, p.getZ()+z,p.getW());
	}

	/**
	 * Přesune bod p o vektor.
	 * @param vector Vector se třemi prvky.
	 */
	public static IPoint3f move(IPoint3f p,float[] vector)
	{
		return new Point3f(p.getX()+vector[0], p.getY()+vector[1], p.getZ()+vector[2]);
	}

	/**
	 * Přesune bod p o vektor.
	 * @param vector Vector se třemi prvky.
	 */
	public static IPoint4f move(IPoint4f p,float[] vector)
	{
		return new Point4f(p.getX()+vector[0], p.getY()+vector[1], p.getZ()+vector[2], p.getW());
	}

	/**
	 * Vypočte vektor z bodu from do bodu to.
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
	 * Vypočte vzdálenost dvou bodů.
	 */
	public static double distance(IPoint3f from,IPoint3f to)
	{
		float[] vector=directionVector(from, to);
		return sizeVector3(vector);
	}

	/**
	 * Spočte velikost vektoru se třemi složkami.
	 * @param vector Pole tří složek.
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
	 * Spočte velikost vektoru se třemi složkami.
	 * @param vector Pole tří složek.
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
	 * Spočte velikost vektoru
	 * @param vector Pole tří složek.
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
	 * Vytvoří zrcadlový obraz bodu dle středového bodu.
	 * @param point Původní bod pro zrcadlení.
	 * @param centerPoint Středový bod, dle kterého vznikne obraz.
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
	 * Vytvoří zrcadlový obraz bodu dle středového bodu.
	 * @param point Původní bod pro zrcadlení.
	 * @param centerPoint Středový bod, dle kterého vznikne obraz.
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
	 * Porovnání bodů dle souřadnic.
	 * @return Vrátí true pokud jsou body na stejných souřadnicích.
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
	 * Porovnání bodů dle souřadnic a váhy bodu.
	 * @return Vrátí true pokud jsou body na stejných souřadnicích a mají stejnou váhu.
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
	 * Spočte střed úsečky zadané dvěmi body.
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
	 * Zjistí jestli jsou všechny body na jedné přímce.
	 * @return True když jsou na přímce.
	 */
	public static boolean isLine(IPoint3f p1,IPoint3f p2,IPoint3f p3)
	{
		//pokud jsou 2 body shodné, pak je to přímka
		if(compareCoords(p1, p2) || compareCoords(p3, p2) || compareCoords(p3, p1))
		{
			return true;
		}
		float direction[]=directionVector(p1, p2);//směrový vektor přímky
		float t=(p3.getX()-p1.getX())/direction[0];//parametr t
		//pod p3 leží na přímce p1p2 pokud po dosazení parametru t vyjde bod p3
		return (p3.getY()==p1.getY()+t*direction[1])
		  && (p3.getZ()==p1.getZ()+t*direction[2]);
	}

	/**
	 * Spočte nejbližší bod na přímce lineP1 lineP2 k bodu point.
	 * Počítáno algoritmem spočtení průsečíku přímky s rovinou, zadanou bodem point, kolmou k přímce.
	 * @param lineP1 První bod přímky.
	 * @param lineP2 Druhý bod přímky.
	 * @param point Bod od kterého se určuje nejkratší vzdálenost k přímce.
	 * @return Vrátí bod na přímce. Speciální případy:<br />
	 * -body lineP1 a lineP2 jsou shodné, pak je vrácen bod o suřadnicích lineP1.<br />
	 * -bod point leží na přímce=>je vrácen bod point.
	 */
	public static IPoint3f intersection(IPoint3f lineP1,IPoint3f lineP2,IPoint3f point)
	{
		if(compareCoords(lineP1, lineP2)==true)//přímka neexistuje-je to pouze bod
		{
			return clone(lineP1);
		}
		if(isLine(lineP1, lineP2, point))//bod leží na přímce
		{
			return clone(point);
		}
		//ostatní případy-nutno spočíst průsečík
		/*
		 * Matematický postup:
		 * 1)parametrické rovnice přímky: (směrový vektor přímnky je v poli direction)
		 *	x=lineP1.getX()+t*direction[0];
		 *	y=lineP1.getY()+t*direction[1];
		 *	z=lineP1.getZ()+t*direction[2];
		 * 2)Obecná rovnice roviny kolmé k přímce: (normálový vektor roviny je v poli direction)
		 *	direction[0]*(x-point.getX())+
		 *	direction[1]*(y-point.getY())+
		 *	direction[2]*(z-point.getZ())=0
		 * 3)dosazení rovnic přímky do rovnice roviny a vyjádření parametru t:
		 *	t=(
		 *			direction[0]*(-lineP1.getX()+point.getX())+
		 *			direction[1]*(-lineP1.getY()+point.getY())+
		 *			direction[2]*(-lineP1.getZ()+point.getZ())
		 *		)
		 *	/
		 *	(direction[0]*direction[0]+direction[1]*direction[1]+direction[2]*direction[2])
		 * 4)dosazením spočteného parametru t do rovnice přímky dostaneme průsečík roviny s přímkou což je hledaný bod
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
	 * Normalizace vektoru se třemi složkami.
	 * @param vector Vektor jehož délka je nenulová, musí mít tři složky.
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
	 * Rotace vektoru se třemi složkami kolem osy zadané vektorem.
	 * @param axis Vektor osy rotace. Pokud nebude normalizovaný, dojde kromě rotace taky ke zvětšení.
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

		//výpočet rotace dle Rodrigues' věty o rotaci
		float[] v=vectorMatrix;
		float[] vRot=new float[3];

		vRot[0]=v[0]*cos;
		vRot[1]=v[1]*cos;
		vRot[2]=v[2]*cos;

		float[] vXAxis=new float[]{//vektorový součin axis x v
			axisMatrix[1]*v[2]-axisMatrix[2]*v[1],
			axisMatrix[2]*v[0]-axisMatrix[0]*v[2],
			axisMatrix[0]*v[1]-axisMatrix[1]*v[0]
		};
		vRot[0]+=sin*vXAxis[0];
		vRot[1]+=sin*vXAxis[1];
		vRot[2]+=sin*vXAxis[2];

		double axisScalarV=axisMatrix[0]*v[0]
			+axisMatrix[1]*v[1]
			+axisMatrix[2]*v[2];//skalární součin vektorů axis a v
		vRot[0]+=(1-cos)*axisMatrix[0]*axisScalarV;
		vRot[1]+=(1-cos)*axisMatrix[1]*axisScalarV;
		vRot[2]+=(1-cos)*axisMatrix[2]*axisScalarV;

		return vRot;
	}

	/**
	 * Změna veliskosti vectoru
	 * @param vector Pole velikost 3.
	 * @param scale Násobitel změny.
	 * @return Změněný vektor.
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
