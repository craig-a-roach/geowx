/*
 * Copyright 2006 Jerry Huxtable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.metservice.gallium.projection;

import com.metservice.gallium.GalliumBoundingBoxD;
import com.metservice.gallium.GalliumPointD;

/**
 * @author roach
 */
class MapMath {

	private static final double PI = Math.PI;
	public static final double HALFPI = Math.PI / 2.0;
	public static final double QUARTERPI = Math.PI / 4.0;
	public static final double TWOPI = Math.PI * 2.0;
	public static final double RTD = 180.0 / Math.PI;
	public static final double DTR = Math.PI / 180.0;
	public static final double EPS10 = 1.0e-10;
	public static final double EPS11 = 1.0e-11;

	public static final GalliumBoundingBoxD WORLD_BOUNDS_RAD = GalliumBoundingBoxD.newDimensions(-HALFPI, -PI, PI, TWOPI);
	public static final GalliumBoundingBoxD WORLD_BOUNDS_DEG = GalliumBoundingBoxD.newDimensions(-90.0, -180.0, 180.0, 360.0);

	public static final int DONT_INTERSECT = 0;
	public static final int DO_INTERSECT = 1;
	public static final int COLLINEAR = 2;

	private static final int MAX_ITER_PHI2 = 15;
	private static final int MAX_ITER_MLFN = 10;

	private static final double C00 = 1.0;
	private static final double C02 = .25;
	private static final double C04 = .046875;
	private static final double C06 = .01953125;
	private static final double C08 = .01068115234375;
	private static final double C22 = .75;
	private static final double C44 = .46875;
	private static final double C46 = .01302083333333333333;
	private static final double C48 = .00712076822916666666;
	private static final double C66 = .36458333333333333333;
	private static final double C68 = .00569661458333333333;
	private static final double C88 = .3076171875;

	private static final double P00 = .33333333333333333333;
	private static final double P01 = .17222222222222222222;
	private static final double P02 = .10257936507936507936;
	private static final double P10 = .06388888888888888888;
	private static final double P11 = .06640211640211640211;
	private static final double P20 = .01641501294219154443;

	// avoid instable computations with very small numbers: if the
	// angle is very close to the graticule boundary, return +/-PI.
	// Bernhard Jenny, May 25 2010.
	private static double snapLongitude(double rads) {
		if (Math.abs(rads - PI) < 1e-15) return Math.PI;
		if (Math.abs(rads + PI) < 1e-15) return -Math.PI;
		return rads;
	}

	public static double acos(double v) {
		if (Math.abs(v) > 1.) return v < 0.0 ? PI : 0.0;
		return Math.acos(v);
	}

	public static double acosd(double v) {
		return Math.acos(v) * RTD;
	}

	public static GalliumPointD add(GalliumPointD a, GalliumPointD b) {
		if (a == null) throw new IllegalArgumentException("object is null");
		if (b == null) throw new IllegalArgumentException("object is null");
		return new GalliumPointD(a.x + b.x, a.y + b.y);
	}

	public static double asin(double v) {
		if (Math.abs(v) > 1.) return v < 0.0 ? -HALFPI : HALFPI;
		return Math.asin(v);
	}

	public static double asind(double v) {
		return Math.asin(v) * RTD;
	}

	public static double atan2(double y, double x) {
		return Math.atan2(y, x);
	}

	public static double atan2d(double y, double x) {
		return Math.atan2(y, x) * RTD;
	}

	public static double atand(double v) {
		return Math.atan(v) * RTD;
	}

	public static double authlat(double beta, double[] APA) {
		if (APA == null) throw new IllegalArgumentException("object is null");
		final double t = beta + beta;
		return (beta + APA[0] * Math.sin(t) + APA[1] * Math.sin(t + t) + APA[2] * Math.sin(t + t + t));
	}

	public static double[] authset(double es) {
		double t;
		final double[] APA = new double[3];
		APA[0] = es * P00;
		t = es * es;
		APA[0] += t * P01;
		APA[1] = t * P10;
		t *= es;
		APA[0] += t * P02;
		APA[1] += t * P11;
		APA[2] = t * P20;
		return APA;
	}

	public static double clamp(double lo, double val, double hi) {
		if (val < lo) return lo;
		if (val > hi) return hi;
		return val;
	}

	public static double cosd(double v) {
		return Math.cos(v * DTR);
	}

	public static double cross(double x1, double y1, double x2, double y2) {
		return x1 * y2 - x2 * y1;
	}

	public static double cross(GalliumPointD a, GalliumPointD b) {
		if (a == null) throw new IllegalArgumentException("object is null");
		if (b == null) throw new IllegalArgumentException("object is null");
		return a.x * b.y - b.x * a.y;
	}

	public static double degToRad(double v) {
		return v * DTR;
	}

	public static double distance(double dx, double dy) {
		return Math.sqrt(dx * dx + dy * dy);
	}

	public static double distance(GalliumPointD a, GalliumPointD b) {
		if (a == null) throw new IllegalArgumentException("object is null");
		if (b == null) throw new IllegalArgumentException("object is null");
		return distance(a.x - b.x, a.y - b.y);
	}

	// For negative angles, d should be negative, m & s positive.
	public static double dmsToDeg(double d, double m, double s) {
		if (d >= 0) return (d + m / 60 + s / 3600);
		return (d - m / 60 - s / 3600);
	}

	// For negative angles, d should be negative, m & s positive.
	public static double dmsToRad(double d, double m, double s) {
		if (d >= 0.0) return (d + m / 60.0 + s / 3600.0) * DTR;
		return (d - m / 60.0 - s / 3600.0) * DTR;
	}

	public static double dot(GalliumPointD a, GalliumPointD b) {
		if (a == null) throw new IllegalArgumentException("object is null");
		if (b == null) throw new IllegalArgumentException("object is null");
		return a.x * b.x + a.y * b.y;
	}

	public static double[] enfn(double es) {
		double t;
		final double[] en = new double[5];
		en[0] = C00 - es * (C02 + es * (C04 + es * (C06 + es * C08)));
		en[1] = es * (C22 - es * (C04 + es * (C06 + es * C08)));
		en[2] = (t = es * es) * (C44 - es * (C46 + es * C48));
		en[3] = (t *= es) * (C66 - es * C68);
		en[4] = t * es * C88;
		return en;
	}

	public static double frac(double v) {
		return v - trunc(v);
	}

	public static double geocentricLatitude(double lat, double flatness) {
		final double f = 1.0 - flatness;
		return Math.atan((f * f) * Math.tan(lat));
	}

	public static double geographicLatitude(double lat, double flatness) {
		final double f = 1.0 - flatness;
		return Math.atan(Math.tan(lat) / (f * f));
	}

	public static double greatCircleDistance(double lon1, double lat1, double lon2, double lat2) {
		final double dlat = Math.sin((lat2 - lat1) / 2.0);
		final double dlon = Math.sin((lon2 - lon1) / 2.0);
		final double r = Math.sqrt(dlat * dlat + Math.cos(lat1) * Math.cos(lat2) * dlon * dlon);
		return 2.0 * Math.asin(r);
	}

	public static double hypot(double xin, final double yin) {
		double x = xin;
		double y = yin;
		if (x < 0.0) {
			x = -x;
		} else if (x == 0.0) return y < 0.0 ? -y : y;
		if (y < 0.0) {
			y = -y;
		} else if (y == 0.0) return x;
		if (x < y) {
			x /= y;
			return y * Math.sqrt(1.0 + x * x);
		} else {
			y /= x;
			return x * Math.sqrt(1.0 + y * y);
		}
	}

	public static int intersectSegments(GalliumPointD aStart, GalliumPointD aEnd, GalliumPointD bStart, GalliumPointD bEnd,
			GalliumPointD.Builder p) {
		if (aStart == null) throw new IllegalArgumentException("object is null");
		if (aEnd == null) throw new IllegalArgumentException("object is null");
		if (bStart == null) throw new IllegalArgumentException("object is null");
		if (bEnd == null) throw new IllegalArgumentException("object is null");
		if (p == null) throw new IllegalArgumentException("object is null");
		double a1, a2, b1, b2, c1, c2;
		double r1, r2, r3, r4;
		double denom, offset, num;

		a1 = aEnd.y - aStart.y;
		b1 = aStart.x - aEnd.x;
		c1 = aEnd.x * aStart.y - aStart.x * aEnd.y;
		r3 = a1 * bStart.x + b1 * bStart.y + c1;
		r4 = a1 * bEnd.x + b1 * bEnd.y + c1;

		if (r3 != 0 && r4 != 0 && sameSigns(r3, r4)) return DONT_INTERSECT;

		a2 = bEnd.y - bStart.y;
		b2 = bStart.x - bEnd.x;
		c2 = bEnd.x * bStart.y - bStart.x * bEnd.y;
		r1 = a2 * aStart.x + b2 * aStart.y + c2;
		r2 = a2 * aEnd.x + b2 * aEnd.y + c2;

		if (r1 != 0 && r2 != 0 && sameSigns(r1, r2)) return DONT_INTERSECT;

		denom = a1 * b2 - a2 * b1;
		if (denom == 0) return COLLINEAR;

		offset = denom < 0 ? -denom / 2 : denom / 2;

		num = b1 * c2 - b2 * c1;
		p.x = (num < 0 ? num - offset : num + offset) / denom;

		num = a2 * c1 - a1 * c2;
		p.y = (num < 0 ? num - offset : num + offset) / denom;

		return DO_INTERSECT;
	}

	public static double inv_mlfn(final double arg, final double es, final double[] en) {
		final double k = 1.0 / (1.0 - es);
		double phi = arg;
		for (int i = MAX_ITER_MLFN; i > 0; i--) {
			final double s = Math.sin(phi);
			final double t = 1.0 - es * s * s;
			final double tt = (mlfn(phi, s, Math.cos(phi), en) - arg) * (t * Math.sqrt(t)) * k;
			if (Math.abs(tt) < EPS11) return phi;
			phi -= tt;
		}
		return phi;
	}

	public static double longitudeDistance(double l1, double l2) {
		return Math.min(Math.abs(l1 - l2), ((l1 < 0) ? l1 + PI : PI - l1) + ((l2 < 0) ? l2 + PI : PI - l2));
	}

	public static double mlfn(double phi_in, double sphi_in, double cphi_in, double[] en) {
		if (en == null) throw new IllegalArgumentException("object is null");
		final double cphi = cphi_in * sphi_in;
		final double sphi = sphi_in * sphi_in;
		return en[0] * phi_in - cphi * (en[1] + sphi * (en[2] + sphi * (en[3] + sphi * en[4])));
	}

	public static double msfn(double sinphi, double cosphi, double es) {
		return cosphi / Math.sqrt(1.0 - es * sinphi * sinphi);
	}

	public static GalliumPointD multiply(GalliumPointD a, GalliumPointD b) {
		if (a == null) throw new IllegalArgumentException("object is null");
		if (b == null) throw new IllegalArgumentException("object is null");
		return new GalliumPointD(a.x * b.x, a.y * b.y);
	}

	public static void negate(GalliumPointD.Builder a) {
		if (a == null) throw new IllegalArgumentException("object is null");
		a.x = -a.x;
		a.y = -a.y;
	}

	public static void normalize(GalliumPointD.Builder a) {
		if (a == null) throw new IllegalArgumentException("object is null");
		final double d = distance(a.x, a.y);
		a.x /= d;
		a.y /= d;
	}

	public static double normalizeAngle(double angle)
			throws ProjectionException {
		if (Double.isInfinite(angle) || Double.isNaN(angle)) throw new ProjectionException("Infinite angle");
		while (angle > TWOPI) {
			angle -= TWOPI;
		}
		while (angle < 0.0) {
			angle += TWOPI;
		}
		return angle;
	}

	public static double normalizeLatitude(final double rads)
			throws ProjectionException {
		if (Double.isInfinite(rads) || Double.isNaN(rads)) throw new ProjectionException("Infinite latitude");
		double na = rads;
		while (na > HALFPI) {
			na -= PI;
		}
		while (na < -HALFPI) {
			na += PI;
		}
		return na;
	}

	public static double normalizeLongitude(final double rads)
			throws ProjectionException {
		if (Double.isInfinite(rads) || Double.isNaN(rads)) throw new ProjectionException("Infinite longitude");
		double na = snapLongitude(rads);
		while (na > PI) {
			na -= TWOPI;
		}
		while (na < -PI) {
			na += TWOPI;
		}
		return na;
	}

	public static GalliumPointD perpendicular(GalliumPointD a) {
		return new GalliumPointD(-a.y, a.x);
	}

	public static double phi2(final double ts, final double e)
			throws ProjectionException {
		final double eccnth = 0.5 * e;
		double phi = HALFPI - 2.0 * Math.atan(ts);
		for (int i = 0; i <= MAX_ITER_PHI2; i++) {
			final double con = e * Math.sin(phi);
			final double tsf = Math.pow((1.0 - con) / (1.0 + con), eccnth);
			final double phicon = HALFPI - 2.0 * Math.atan(ts * tsf);
			final double dphi = phicon - phi;
			if (Math.abs(dphi) <= EPS10) {
				break;
			}
			if (i == MAX_ITER_PHI2) {
				final String m = "Could not converge to " + phi + " ...currently " + phicon + " after " + i + " iterations";
				throw new ProjectionException(m);
			}
			phi = phicon;
		}
		return phi;
	}

	public static double qsfn(final double sinphi, final double e, final double one_es) {
		double con;
		if (e >= 1.0e-7) {
			con = e * sinphi;
			return (one_es * (sinphi / (1.0 - con * con) - (0.5 / e) * Math.log((1.0 - con) / (1.0 + con))));
		} else
			return (sinphi + sinphi);
	}

	public static double radToDeg(double v) {
		return v * RTD;
	}

	public static boolean sameSigns(double a, double b) {
		return a < 0.0 == b < 0.0;
	}

	public static boolean sameSigns(int a, int b) {
		return a < 0 == b < 0;
	}

	public static double sind(double v) {
		return Math.sin(v * DTR);
	}

	public static double sphericalAzimuth(double lat0, double lon0, double lat, double lon) {
		final double diff = lon - lon0;
		final double coslat = Math.cos(lat);
		return Math.atan2(coslat * Math.sin(diff), (Math.cos(lat0) * Math.sin(lat) - Math.sin(lat0) * coslat * Math.cos(diff)));
	}

	public static double sqrt(double v) {
		return v < 0.0 ? 0.0 : Math.sqrt(v);
	}

	public static GalliumPointD subtract(GalliumPointD a, GalliumPointD b) {
		return new GalliumPointD(a.x - b.x, a.y - b.y);
	}

	public static double takeSign(double a, double b) {
		a = Math.abs(a);
		if (b < 0) return -a;
		return a;
	}

	public static int takeSign(int a, int b) {
		a = Math.abs(a);
		if (b < 0) return -a;
		return a;
	}

	public static double tand(double v) {
		return Math.tan(v * DTR);
	}

	public static double trunc(double v) {
		return v < 0.0 ? Math.ceil(v) : Math.floor(v);
	}

	public static double tsfn(double phi_in, double sinphi_in, double e_in) {
		final double sinphi = sinphi_in * e_in;
		return (Math.tan(0.5 * (HALFPI - phi_in)) / Math.pow((1.0 - sinphi) / (1.0 + sinphi), 0.5 * e_in));
	}
}
