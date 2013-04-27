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

import com.metservice.argon.ArgonText;
import com.metservice.gallium.GalliumBoundingBoxF;
import com.metservice.gallium.GalliumPointD;

/**
 * @author roach
 */
abstract class AbstractProjection implements IGalliumProjection {

	public static final int EPSG_UNKNOWN = 0;

	protected static final double EPS10 = 1e-10;
	protected static final double RTD = 180.0 / Math.PI;
	protected static final double DTR = Math.PI / 180.0;

	private void transformNonrectilinear(GalliumBoundingBoxF src, GalliumBoundingBoxF.BuilderD dst)
			throws GalliumProjectionException {
		assert src != null;
		assert dst != null;
		final float rx = src.xLo();
		final float ry = src.yLo();
		final float rw = src.width();
		final float rh = src.height();
		for (int ix = 0; ix < 7; ix++) {
			final double x = rx + rw * ix / 6;
			for (int iy = 0; iy < 7; iy++) {
				final double y = ry + rh * iy / 6;
				final GalliumPointD.Builder out = GalliumPointD.newBuilder();
				transform(x, y, out);
				if (ix == 0 && iy == 0) {
					dst.init(out.y, out.x);
				} else {
					dst.add(out.y, out.x);
				}
			}
		}
	}

	private void transformRectilinear(GalliumBoundingBoxF srcDeg, GalliumBoundingBoxF.BuilderD dst)
			throws GalliumProjectionException {
		assert srcDeg != null;
		assert dst != null;
		final float rx = srcDeg.xLo();
		final float ry = srcDeg.yLo();
		final float rw = srcDeg.width();
		final float rh = srcDeg.height();
		for (int ix = 0; ix < 2; ix++) {
			final double x = rx + rw * ix;
			for (int iy = 0; iy < 2; iy++) {
				final double y = ry + rh * iy;
				final GalliumPointD.Builder out = GalliumPointD.newBuilder();
				transform(x, y, out);
				if (ix == 0 && iy == 0) {
					dst.init(out.y, out.x);
				} else {
					dst.add(out.y, out.x);
				}
			}
		}
	}

	public double getClippingMaxLatitude() {
		return clippingMaxLatitude;
	}

	public final double getClippingMaxLatitudeDegrees() {
		return getClippingMaxLatitude() * RTD;
	}

	public double getClippingMinLatitude() {
		return clippingMinLatitude;
	}

	public final double getClippingMinLatitudeDegrees() {
		return getClippingMinLatitude() * RTD;
	}

	public Ellipsoid getEllipsoid() {
		return oEllipsoid;
	}

	public int getEPSGCode() {
		return EPSG_UNKNOWN;
	}

	public double getEquatorRadius() {
		return a;
	}

	public double getFalseEasting() {
		return falseEasting;
	}

	public double getFalseNorthing() {
		return falseNorthing;
	}

	public double getFromMetres() {
		return fromMetres;
	}

	public double getMaxLongitude() {
		return maxLongitude;
	}

	public final double getMaxLongitudeDegrees() {
		return getMaxLongitude() * RTD;
	}

	public double getMinLongitude() {
		return minLongitude;
	}

	public final double getMinLongitudeDegrees() {
		return getMinLongitude() * RTD;
	}

	public String getPROJ4Description() {
		final AngleFormat angleFormat = new AngleFormat(AngleFormat.ddmmssPattern, false);
		final StringBuffer sb = new StringBuffer();
		sb.append("+proj=" + name() + " +a=" + a);
		if (es != 0) {
			sb.append(" +es=" + es);
		}
		sb.append(" +lon_0=");
		angleFormat.format(projectionLongitude, sb, null);
		sb.append(" +lat_0=");
		angleFormat.format(projectionLatitude, sb, null);
		if (falseEasting != 1) {
			sb.append(" +x_0=" + falseEasting);
		}
		if (falseNorthing != 1) {
			sb.append(" +y_0=" + falseNorthing);
		}
		if (scaleFactor != 1) {
			sb.append(" +k=" + scaleFactor);
		}
		if (fromMetres != 1) {
			sb.append(" +fr_meters=" + fromMetres);
		}
		return sb.toString();
	}

	public String getProjectionDescription() {
		final StringBuilder sb = new StringBuilder();
		if (this instanceof AbstractProjectionCylindrical) {
			sb.append("cylindrical ");
		}
		if (this instanceof AbstractProjectionConic) {
			sb.append("conic ");
		}
		if (this instanceof AbstractProjectionPseudoCylindrical) {
			sb.append("pseudo-cylindrical ");
		}
		if (this instanceof AbstractProjectionAzimuthal) {
			sb.append("azimuthal ");
		}

		if (this.isConformal()) {
			sb.append("conformal");
		}
		if (this.isEqualArea()) {
			sb.append("equal-area");
		}
		return sb.toString();
	}

	public double getProjectionLatitude() {
		return projectionLatitude;
	}

	public final double getProjectionLatitudeDegrees() {
		return getProjectionLatitude() * RTD;
	}

	public double getProjectionLongitude() {
		return projectionLongitude;
	}

	public double getProjectionLongitudeDegrees() {
		return getProjectionLongitude() * RTD;
	}

	public double getScaleFactor() {
		return scaleFactor;
	}

	public double getTrueScaleLatitude() {
		return trueScaleLatitude;
	}

	public final double getTrueScaleLatitudeDegrees() {
		return getTrueScaleLatitude() * RTD;
	}

	/**
	 * Returns true if this projection has an inverse
	 */
	public boolean hasInverse() {
		return false;
	}

	public void initialize()
			throws GalliumProjectionException {
		spherical = (e == 0.0);
		one_es = 1.0 - es;
		rone_es = 1.0 / one_es;
		totalScale = a * fromMetres;
		totalFalseEasting = falseEasting * fromMetres;
		totalFalseNorthing = falseNorthing * fromMetres;
	}

	@Override
	public boolean inside(double lonDeg, double latDeg)
			throws GalliumProjectionException {
		lonDeg = MapMath.normalizeLongitude(lonDeg * DTR - projectionLongitude);
		latDeg *= DTR;
		return minLongitude <= lonDeg && lonDeg <= maxLongitude && clippingMinLatitude <= latDeg
				&& latDeg <= clippingMaxLatitude;
	}

	@Override
	public GalliumPointD inverseDegrees(double xMetres, double yMetres)
			throws GalliumProjectionException {
		final GalliumPointD.Builder dst = GalliumPointD.newBuilder();
		transformInverseDegrees(xMetres, yMetres, dst);
		return new GalliumPointD(dst);
	}

	public boolean isConformal() {
		return false;
	}

	public boolean isEqualArea() {
		return false;
	}

	/**
	 * Returns true if lat/long lines form a rectangular grid for this projection. This is generally only the case for
	 * cylindrical projections, but not for oblique cylindrical projections.
	 */
	public boolean isRectilinear() {
		return false;
	}

	public String name() {
		return oqtwName == null ? "None" : oqtwName;
	}

	public boolean parallelsAreParallel() {
		return isRectilinear();
	}

	public abstract GalliumPointD.Builder project(double x, double y, GalliumPointD.Builder dst)
			throws GalliumProjectionException;

	public abstract GalliumPointD.Builder projectInverse(double x, double y, GalliumPointD.Builder dst)
			throws GalliumProjectionException;

	public void setClippingMaxLatitude(double maxLatitude) {
		this.clippingMaxLatitude = maxLatitude;
	}

	public void setClippingMinLatitude(double minLatitude) {
		this.clippingMinLatitude = minLatitude;
	}

	public void setEllipsoid(Ellipsoid ellipsoid) {
		if (ellipsoid == null) throw new IllegalArgumentException("object is null");
		oEllipsoid = ellipsoid;
		a = ellipsoid.equatorialRadiusMetres;
		e = ellipsoid.eccentricity;
		es = ellipsoid.eccentricity2;
	}

	public void setFalseEasting(double projectedUnits) {
		this.falseEasting = projectedUnits;
	}

	public void setFalseNorthing(double projectedUnits) {
		this.falseNorthing = projectedUnits;
	}

	public void setFromMetres(double metresToProjectedUnits) {
		this.fromMetres = metresToProjectedUnits;
	}

	public void setMaxLongitude(double maxLongitude) {
		this.maxLongitude = maxLongitude;
	}

	public final void setMaxLongitudeDegrees(double maxLongitude) {
		setMaxLongitude(DTR * maxLongitude);
	}

	public void setMinLongitude(double minLongitude) {
		this.minLongitude = minLongitude;
	}

	public final void setMinLongitudeDegrees(double minLongitude) {
		setMinLongitude(DTR * minLongitude);
	}

	public void setName(String name) {
		if (name == null || name.length() == 0) throw new IllegalArgumentException("string is null or empty");
		this.oqtwName = ArgonText.oqtw(name);
	}

	public void setProjectionLatitude(double projectionLatitude) {
		this.projectionLatitude = projectionLatitude;
	}

	public final void setProjectionLatitudeDegrees(double projectionLatitude) {
		setProjectionLatitude(DTR * projectionLatitude);
	}

	public void setProjectionLongitude(double projectionLongitude)
			throws GalliumProjectionException {
		this.projectionLongitude = MapMath.normalizeLongitude(projectionLongitude);
	}

	public final void setProjectionLongitudeDegrees(double projectionLongitude)
			throws GalliumProjectionException {
		setProjectionLongitude(DTR * projectionLongitude);
	}

	public void setScaleFactor(double scaleFactor) {
		this.scaleFactor = scaleFactor;
	}

	public void setTrueScaleLatitude(double trueScaleLatitude) {
		this.trueScaleLatitude = trueScaleLatitude;
	}

	public final void setTrueScaleLatitudeDegrees(double trueScaleLatitude) {
		setTrueScaleLatitude(DTR * trueScaleLatitude);
	}

	@Override
	public GalliumPointD transform(double lonDeg, double latDeg)
			throws GalliumProjectionException {
		final GalliumPointD.Builder dst = GalliumPointD.newBuilder();
		transform(lonDeg, latDeg, dst);
		return new GalliumPointD(dst);
	}

	public GalliumPointD.Builder transform(double srcXdeg, double srcYdeg, GalliumPointD.Builder dst)
			throws GalliumProjectionException {
		if (dst == null) throw new IllegalArgumentException("object is null");
		double x = srcXdeg * DTR;
		if (projectionLongitude != 0.0) {
			x = MapMath.normalizeLongitude(x - projectionLongitude);
		}
		project(x, srcYdeg * DTR, dst);
		dst.x = (totalScale * dst.x) + totalFalseEasting;
		dst.y = (totalScale * dst.y) + totalFalseNorthing;
		return dst;
	}

	public GalliumBoundingBoxF transform(GalliumBoundingBoxF srcDeg)
			throws GalliumProjectionException {
		if (srcDeg == null) throw new IllegalArgumentException("object is null");
		final GalliumBoundingBoxF.BuilderD bb = GalliumBoundingBoxF.newBuilderD();
		if (isRectilinear()) {
			transformRectilinear(srcDeg, bb);
		} else {
			transformNonrectilinear(srcDeg, bb);
		}
		return GalliumBoundingBoxF.newInstance(bb);
	}

	public GalliumPointD.Builder transformInverseDegrees(double srcXmetres, double srcYmetres, GalliumPointD.Builder dst)
			throws GalliumProjectionException {
		transformInverseRadians(srcXmetres, srcYmetres, dst);
		dst.x *= RTD;
		dst.y *= RTD;
		return dst;
	}

	public GalliumPointD.Builder transformInverseRadians(double srcXmetres, double srcYmetres, GalliumPointD.Builder dst)
			throws GalliumProjectionException {
		if (dst == null) throw new IllegalArgumentException("object is null");
		final double x = (srcXmetres - totalFalseEasting) / totalScale;
		final double y = (srcYmetres - totalFalseNorthing) / totalScale;
		projectInverse(x, y, dst);
		if (dst.x < -Math.PI) {
			dst.x = -Math.PI;
		} else if (dst.x > Math.PI) {
			dst.x = Math.PI;
		}
		if (projectionLongitude != 0) {
			dst.x = MapMath.normalizeLongitude(dst.x + projectionLongitude);
		}
		return dst;
	}

	public GalliumPointD.Builder transformInverseRadians(GalliumPointD srcMetres, GalliumPointD.Builder dst)
			throws GalliumProjectionException {
		if (srcMetres == null) throw new IllegalArgumentException("object is null");
		return transformInverseRadians(srcMetres.x, srcMetres.y, dst);
	}

	protected AbstractProjection() {
		setEllipsoid(Ellipsoid.SPHERE);
	}

	/**
	 * The minimum latitude of the bounds of this projection
	 */
	protected double clippingMinLatitude = -Math.PI / 2;
	/**
	 * The minimum longitude of the bounds of this projection. This is relative to the projection centre.
	 */
	protected double minLongitude = -Math.PI;
	/**
	 * The maximum latitude of the bounds of this projection
	 */
	protected double clippingMaxLatitude = Math.PI / 2;
	/**
	 * The maximum longitude of the bounds of this projection. This is relative to the projection centre.
	 */
	protected double maxLongitude = Math.PI;
	/**
	 * The latitude of the centre of projection
	 */
	protected double projectionLatitude = 0.0;
	/**
	 * The longitude of the centre of projection
	 */
	protected double projectionLongitude = 0.0;
	/**
	 * The projection scale factor
	 */
	protected double scaleFactor = 1.0;
	/**
	 * The false Easting of this projection
	 */
	protected double falseEasting = 0;
	/**
	 * The false Northing of this projection
	 */
	protected double falseNorthing = 0;
	/**
	 * The latitude of true scale. Only used by specific projections.
	 */
	protected double trueScaleLatitude = 0.0;
	/**
	 * The equator radius
	 */
	protected double a = 0;
	/**
	 * The eccentricity
	 */
	protected double e = 0;
	/**
	 * The eccentricity squared
	 */
	protected double es = 0;
	/**
	 * 1-(eccentricity squared)
	 */
	protected double one_es = 0;
	/**
	 * 1/(1-(eccentricity squared))
	 */
	protected double rone_es = 0;
	/**
	 * The ellipsoid used by this projection
	 */
	protected Ellipsoid oEllipsoid;
	/**
	 * True if this projection is using a sphere (es == 0)
	 */
	protected boolean spherical = false;

	/**
	 * True if this projection is geocentric
	 */
	protected boolean geocentric = false;
	/**
	 * The name of this projection
	 */
	protected String oqtwName = null;
	/**
	 * Conversion factor from metres to whatever units the projection uses.
	 */
	protected double fromMetres = 1;

	/**
	 * The total scale factor = Earth radius * units
	 */
	private double totalScale = 0.0;
	/**
	 * falseEasting, adjusted to the appropriate units using fromMetres
	 */
	private double totalFalseEasting = 0.0;

	/**
	 * falseNorthing, adjusted to the appropriate units using fromMetres
	 */
	private double totalFalseNorthing = 0.0;

}
