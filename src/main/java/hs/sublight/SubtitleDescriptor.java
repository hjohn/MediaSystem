
package hs.sublight;


import java.nio.ByteBuffer;


public interface SubtitleDescriptor {
	
	String getName();
	

	String getLanguageName();
	

	String getType();
	

	ByteBuffer fetch() throws Exception;
	
}
