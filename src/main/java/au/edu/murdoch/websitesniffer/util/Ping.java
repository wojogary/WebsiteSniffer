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

import au.edu.murdoch.websitesniffer.models.IPTest.Type;
import static au.edu.murdoch.websitesniffer.models.IPTest.Type.IPv6;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.SystemUtils;
import org.xbill.DNS.TextParseException;

public class Ping
{
	/**
	 * Ping a given URL using a given ping method.
	 *
	 * @param url    the String value of the URL or IP address to ping
	 * @param ipType the {@link Type} of IP to ping with
	 * @return the int value of the number of milliseconds passed to receive a reply
	 *
	 * @throws IOException
	 */
	public static int ping( final String url, final Type ipType ) throws IOException
	{
		if( url == null )
		{
			throw new NullPointerException();
		}

		final Process process;
		if( SystemUtils.IS_OS_WINDOWS )
		{
			process = new ProcessBuilder( "ping", ipType == IPv6 ? "-6" : "-4", url ).start();
		}
		else
		{
			process = new ProcessBuilder( ipType == IPv6 ? "ping" : "ping6", url, "-c", "4" ).start();
		}

		return processPing( process );
	}

	private static int processPing( final Process process ) throws IOException
	{
		final List<String> pingOutput = readPingOutput( process );
		return getMinimumPing( pingOutput );
	}

	private static List<String> readPingOutput( final Process process ) throws IOException
	{
		final List<String> pingOutput = new ArrayList<>();

		try( final InputStreamReader inputStreamReader = new InputStreamReader( process.getInputStream() );
			 final BufferedReader reader = new BufferedReader( inputStreamReader ) )
		{
			String line;
			while( ( line = reader.readLine() ) != null )
			{
				pingOutput.add( line );
			}
		}

		return pingOutput;
	}

	private static int getMinimumPing( final List<String> pingOutput ) throws TextParseException
	{
		final String lastLine = pingOutput.get( pingOutput.size() - 1 );
		int index = lastLine.indexOf( '=' );

		if( index != -1 )
		{
			index += 2;

			final int endIndex;
			if( SystemUtils.IS_OS_WINDOWS )
			{
				endIndex = lastLine.indexOf( 'm', index );
			}
			else
			{
				endIndex = lastLine.indexOf( '/', index );
			}

			if( endIndex != -1 )
			{
				final float ping = Float.parseFloat( lastLine.substring( index, endIndex ) );
				return (int) ping;
			}
			else
			{
				throw new TextParseException( "Failed to find end index of minimum ping" );
			}
		}
		else
		{
			throw new TextParseException( "Couldn't find '=' in last line of ping!" );
		}
	}
}