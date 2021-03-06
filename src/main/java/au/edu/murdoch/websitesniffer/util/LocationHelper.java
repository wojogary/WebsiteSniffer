/* 
 * Copyright (C) 2016 Jordan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package au.edu.murdoch.websitesniffer.util;

import au.edu.murdoch.websitesniffer.core.Main;
import au.edu.murdoch.websitesniffer.models.Location;
import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.sql.SQLException;

public class LocationHelper
{
	private static final File mLocationDatabase = new File( Main.getLocationDatabase() );
	private static final CHMCache mCache = new CHMCache();
	private static LocationHelper mInstance;

	public static LocationHelper getInstance() throws IOException
	{
		if( mInstance == null )
		{
			mInstance = new LocationHelper();
		}

		return mInstance;
	}

	public Location getLocationForHost() throws IOException, SQLException, GeoIp2Exception, NullPointerException
	{
		final URL url = new URL( "http://checkip.amazonaws.com" );
		try( final BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( url.openStream() ) ) )
		{
			return getLocationByIP( InetAddress.getByName( bufferedReader.readLine() ) );
		}
	}

	public Location getLocationByIP( final InetAddress address ) throws IOException, GeoIp2Exception, SQLException, NullPointerException
	{
		if( address == null )
		{
			throw new NullPointerException( "IP is null." );
		}

		try( final DatabaseReader mReader = new DatabaseReader.Builder( mLocationDatabase ).withCache( mCache ).build() )
		{
			final CityResponse response = mReader.city( address );

			final String city = response.getCity().getName();
			final String country = response.getCountry().getName();
			final double latitude = response.getLocation().getLatitude();
			final double longitude = response.getLocation().getLongitude();

			//If the location is already in the database, get its id
			Location location = DatabaseHelper.getInstance().getLocation( city, country );
			if( location == null )
			{
				//Location not found; insert it
				DatabaseHelper.getInstance().insertLocation( city, country, latitude, longitude );
				location = DatabaseHelper.getInstance().getLocation( city, country );
			}

			return location;
		}
	}
}
