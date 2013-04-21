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

import com.metservice.gallium.GalliumPointD.Builder;

/**
 * @author roach
 */
class ProjectionMercator extends AbstractProjectionCylindrical {

	/**
	 * Returns the ESPG code for this projection, or 0 if unknown.
	 */
	@Override
	public int getEPSGCode() {
		return 9804;
	}

	@Override
	public boolean hasInverse() {
		return true;
	}

	@Override
	public boolean isConformal() {
		return true;
	}

	@Override
	public Builder project(double lam, double phi, Builder dst) {
		if (spherical) {
			dst.x = scaleFactor * lam;
			dst.y = scaleFactor * Math.log(Math.tan(MapMath.QUARTERPI + 0.5 * phi));
		} else {
			dst.x = scaleFactor * lam;
			dst.y = -scaleFactor * Math.log(MapMath.tsfn(phi, Math.sin(phi), e));
		}
		return dst;
	}

	@Override
	public Builder projectInverse(double x, double y, Builder dst)
			throws GalliumProjectionException {
		if (spherical) {
			dst.y = MapMath.HALFPI - 2. * Math.atan(Math.exp(-y / scaleFactor));
			dst.x = x / scaleFactor;
		} else {
			dst.y = MapMath.phi2(Math.exp(-y / scaleFactor), e);
			dst.x = x / scaleFactor;
		}
		return dst;
	}

	@Override
	public String toString() {
		return "Mercator";
	}

	public ProjectionMercator() {
		clippingMinLatitude = MapMath.degToRad(-85.0);
		clippingMaxLatitude = MapMath.degToRad(85.0);
	}
}
