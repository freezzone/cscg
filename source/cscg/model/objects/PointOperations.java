package cscg.model.objects;

import cern.colt.matrix.linalg.Algebra;


/**
 * Operace s body a vektory.
 * @author Tom� Re�nar
 */
public class PointOperations {

	public static final Algebra alg=Algebra.DEFAULT;

	/**
	 * P�esune bod p o vektor [x,y,z].
	 * @return P�esunut� bod.
	 */
	public static IPoint3f move(IPoint3f p,float x,float y,float z)
	{
		return new Point3f(p.getX()+x, p.getY()+y, p.getZ()+z);
	}
	/**
	 * P�esune bod p o vektor [x,y,z].
	 * @return P�esunut� bod.
	 */
	public static IPoint4f move(IPoint4f p,float x,float y,float z)
	{
		return new Point4f(p.getX()+x, p.getY()+y, p.getZ()+z,p.getW());
	}

	/**
	 * P�esune bod p o vektor.
	 * @param vector Vector se t�emi prvky.
	 */
	public static IPoint3f move(IPoint3f p,float[] vector)
	{
		return new Point3f(p.getX()+vector[0], p.getY()+vector[1], p.getZ()+vector[2]);
	}

	/**
	 * P�esune bod p o vektor.
	 * @param vector Vector se t�emi prvky.
	 */
	public static IPoint4f move(IPoint4f p,float[] vector)
	{
		return new Point4f(p.getX()+vector[0], p.getY()+vector[1], p.getZ()+vector[2], p.getW());
	}

	/**
	 * Vypo�te vektor z bodu from do bodu to.
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
	 * Vypo�te vzd�lenost dvou bod�.
	 */
	public static double distance(IPoint3f from,IPoint3f to)
	{
		float[] vector=directionVector(from, to);
		return sizeVector3(vector);
	}

	/**
	 * Spo�te velikost vektoru se t�emi slo�kami.
	 * @param vector Pole t�� slo�ek.
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
	 * Spo�te velikost vektoru se t�emi slo�kami.
	 * @param vector Pole t�� slo�ek.
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
	 * Spo�te velikost vektoru
	 * @param vector Pole t�� slo�ek.
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
	 * Vytvo�� zrcadlov� obraz bodu dle st�edov�ho bodu.
	 * @param point P�vodn� bod pro zrcadlen�.
	 * @param centerPoint St�edov� bod, dle kter�ho vznikne obraz.
	 * @return Zrcadlen� bod.
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
	 * Vytvo�� zrcadlov� obraz bodu dle st�edov�ho bodu.
	 * @param point P�vodn� bod pro zrcadlen�.
	 * @param centerPoint St�edov� bod, dle kter�ho vznikne obraz.
	 * @return Zrcadlen� bod.
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
	 * Porovn�n� bod� dle sou�adnic.
	 * @return Vr�t� true pokud jsou body na stejn�ch sou�adnic�ch.
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
	 * Porovn�n� bod� dle sou�adnic a v�hy bodu.
	 * @return Vr�t� true pokud jsou body na stejn�ch sou�adnic�ch a maj� stejnou v�hu.
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
	 * Spo�te st�ed �se�ky zadan� dv�mi body.
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
	 * Zjist� jestli jsou v�echny body na jedn� p��mce.
	 * @return True kdy� jsou na p��mce.
	 */
	public static boolean isLine(IPoint3f p1,IPoint3f p2,IPoint3f p3)
	{
		//pokud jsou 2 body shodn�, pak je to p��mka
		if(compareCoords(p1, p2) || compareCoords(p3, p2) || compareCoords(p3, p1))
		{
			return true;
		}
		float direction[]=directionVector(p1, p2);//sm�rov� vektor p��mky
		float t=(p3.getX()-p1.getX())/direction[0];//parametr t
		//pod p3 le�� na p��mce p1p2 pokud po dosazen� parametru t vyjde bod p3
		return (p3.getY()==p1.getY()+t*direction[1])
		  && (p3.getZ()==p1.getZ()+t*direction[2]);
	}

	/**
	 * Spo�te nejbli��� bod na p��mce lineP1 lineP2 k bodu point.
	 * Po��t�no algoritmem spo�ten� pr�se��ku p��mky s rovinou, zadanou bodem point, kolmou k p��mce.
	 * @param lineP1 Prvn� bod p��mky.
	 * @param lineP2 Druh� bod p��mky.
	 * @param point Bod od kter�ho se ur�uje nejkrat�� vzd�lenost k p��mce.
	 * @return Vr�t� bod na p��mce. Speci�ln� p��pady:<br />
	 * -body lineP1 a lineP2 jsou shodn�, pak je vr�cen bod o su�adnic�ch lineP1.<br />
	 * -bod point le�� na p��mce=>je vr�cen bod point.
	 */
	public static IPoint3f intersection(IPoint3f lineP1,IPoint3f lineP2,IPoint3f point)
	{
		if(compareCoords(lineP1, lineP2)==true)//p��mka neexistuje-je to pouze bod
		{
			return clone(lineP1);
		}
		if(isLine(lineP1, lineP2, point))//bod le�� na p��mce
		{
			return clone(point);
		}
		//ostatn� p��pady-nutno spo��st pr�se��k
		/*
		 * Matematick� postup:
		 * 1)parametrick� rovnice p��mky: (sm�rov� vektor p��mnky je v poli direction)
		 *	x=lineP1.getX()+t*direction[0];
		 *	y=lineP1.getY()+t*direction[1];
		 *	z=lineP1.getZ()+t*direction[2];
		 * 2)Obecn� rovnice roviny kolm� k p��mce: (norm�lov� vektor roviny je v poli direction)
		 *	direction[0]*(x-point.getX())+
		 *	direction[1]*(y-point.getY())+
		 *	direction[2]*(z-point.getZ())=0
		 * 3)dosazen� rovnic p��mky do rovnice roviny a vyj�d�en� parametru t:
		 *	t=(
		 *			direction[0]*(-lineP1.getX()+point.getX())+
		 *			direction[1]*(-lineP1.getY()+point.getY())+
		 *			direction[2]*(-lineP1.getZ()+point.getZ())
		 *		)
		 *	/
		 *	(direction[0]*direction[0]+direction[1]*direction[1]+direction[2]*direction[2])
		 * 4)dosazen�m spo�ten�ho parametru t do rovnice p��mky dostaneme pr�se��k roviny s p��mkou co� je hledan� bod
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
	 * Klonov�n� bodu.
	 */
	public static IPoint3f clone(IPoint3f p)
	{
		return new Point3f(p.getX(), p.getY(), p.getZ());
	}
	/**
	 * Klonov�n� bodu.
	 */
	public static IPoint4f clone(IPoint4f p)
	{
		return new Point4f(p.getX(), p.getY(), p.getZ(),p.getW());
	}
	/**
	 * Normalizace vektoru se t�emi slo�kami.
	 * @param vector Vektor jeho� d�lka je nenulov�, mus� m�t t�i slo�ky.
	 * @return Normalizovan� vektor jeho� velikost je rovna 1.
	 * @throws IllegalArgumentException Pokud je vektor nulov� nebo nem� spr�vnou d�lku.
	 */
	public static float[] normalizeVector3(float[] vector) throws IllegalArgumentException
	{
		if(vector==null || vector.length!=3)
		{
			throw new IllegalArgumentException("Vektor nem� 3 slo�ky.");
		}
		float size=(float)sizeVector3(vector);
		if(size==0.0f)
		{
			throw new IllegalArgumentException("Vektor je nulov� d�lky.");
		}
		return new float[]{
			vector[0]/size,
			vector[1]/size,
			vector[2]/size
		};
	}
	/**
	 * Normalizace vektoru.
	 * @param vector Vektor jeho� d�lka je nenulov�.
	 * @return Normalizovan� vektor jeho� velikost je rovna 1.
	 * @throws IllegalArgumentException Pokud je vektor nulov�.
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
			throw new IllegalArgumentException("Vektor je nulov� d�lky.");
		}
		return new Point3f(
			vector.getX()/size,
			vector.getY()/size,
			vector.getZ()/size
		);
	}
	/**
	 * Rotace vektoru se t�emi slo�kami kolem osy zadan� vektorem.
	 * @param axis Vektor osy rotace. Pokud nebude normalizovan�, dojde krom� rotace taky ke zv�t�en�.
	 * @param angle �hel rotace ve stupn�ch.
	 * @param vector Rotovan� vektor.
	 * @return Vr�t� rotovan� vektor v poli velikosti 3.
	 */
	public static float[] axisRotationVector3(IPoint3f axis,double angle,IPoint3f vector)
	{
		float[] vectorMatrix=new float[]{vector.getX(),vector.getY(),vector.getZ()};
		angle=Math.toRadians(angle);
		float cos=(float)Math.cos(angle);
		float sin=(float)Math.sin(angle);
		float[] axisMatrix = new float[]{axis.getX(),axis.getY(),axis.getZ()};//osa rotace

		//v�po�et rotace dle Rodrigues' v�ty o rotaci
		float[] v=vectorMatrix;
		float[] vRot=new float[3];

		vRot[0]=v[0]*cos;
		vRot[1]=v[1]*cos;
		vRot[2]=v[2]*cos;

		float[] vXAxis=new float[]{//vektorov� sou�in axis x v
			axisMatrix[1]*v[2]-axisMatrix[2]*v[1],
			axisMatrix[2]*v[0]-axisMatrix[0]*v[2],
			axisMatrix[0]*v[1]-axisMatrix[1]*v[0]
		};
		vRot[0]+=sin*vXAxis[0];
		vRot[1]+=sin*vXAxis[1];
		vRot[2]+=sin*vXAxis[2];

		double axisScalarV=axisMatrix[0]*v[0]
			+axisMatrix[1]*v[1]
			+axisMatrix[2]*v[2];//skal�rn� sou�in vektor� axis a v
		vRot[0]+=(1-cos)*axisMatrix[0]*axisScalarV;
		vRot[1]+=(1-cos)*axisMatrix[1]*axisScalarV;
		vRot[2]+=(1-cos)*axisMatrix[2]*axisScalarV;

		return vRot;
	}

	/**
	 * Zm�na veliskosti vectoru
	 * @param vector Pole velikost 3.
	 * @param scale N�sobitel zm�ny.
	 * @return Zm�n�n� vektor.
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
