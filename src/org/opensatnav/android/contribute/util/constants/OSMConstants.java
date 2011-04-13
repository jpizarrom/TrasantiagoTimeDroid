/* 
This file is part of OpenSatNav.

    OpenSatNav is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    OpenSatNav is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with OpenSatNav.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Copyright 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opensatnav.android.contribute.util.constants;


public interface OSMConstants {
	// ===========================================================
	// Final Fields
	// ===========================================================
	
	

	public static final String OSM_CREATOR_INFO = "OpenSatNav";
	
	/**
	   * The number of distance readings to smooth to get a stable signal.
	   */
	  public static final int DISTANCE_SMOOTHING_FACTOR = 25;

	  /**
	   * The number of elevation readings to smooth to get a somewhat accurate
	   * signal.
	   */
	  public static final int ELEVATION_SMOOTHING_FACTOR = 25;

	  /**
	   * The number of grade readings to smooth to get a somewhat accurate signal.
	   */
	  public static final int GRADE_SMOOTHING_FACTOR = 5;

	  /**
	   * The number of speed reading to smooth to get a somewhat accurate signal.
	   */
	  public static final int SPEED_SMOOTHING_FACTOR = 25;

	  /**
	   * Maximum number of track points displayed by the map overlay.
	   */
	  public static final int MAX_DISPLAYED_TRACK_POINTS = 10000;

	  /**
	   * Maximum number of track points ever loaded at once from the provider into
	   * memory.
	   * With a recording frequency of 2 seconds, 15000 corresponds to 8.3 hours.
	   */
	  public static final int MAX_LOADED_TRACK_POINTS = 20000;

	  /**
	   * Maximum number of way points displayed by the map overlay.
	   */
	  public static final int MAX_DISPLAYED_WAYPOINTS_POINTS = 128;

	  /**
	   * Maximum number of way points that will be loaded at one time.
	   */
	  public static final int MAX_LOADED_WAYPOINTS_POINTS = 10000;

	  /**
	   * Any time segment where the distance traveled is less than this value will
	   * not be considered moving.
	   */
	  public static final double MAX_NO_MOVEMENT_DISTANCE = 2;

	  /**
	   * Anything faster than that (in meters per second) will be considered moving.
	   */
	  public static final double MAX_NO_MOVEMENT_SPEED = 0.224;

	  /**
	   * Ignore any acceleration faster than this.
	   * Will ignore any speeds that imply accelaration greater than 2g's
	   * 2g = 19.6 m/s^2 = 0.0002 m/ms^2 = 0.02 m/(m*ms)
	   */
	  public static final double MAX_ACCELERATION = 0.02;

	public static final String MIN_REQUIRED_ACCURACY = "50";

	// ===========================================================
	// Methods
	// ===========================================================
}
