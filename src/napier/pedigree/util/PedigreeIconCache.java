package napier.pedigree.util;

import java.awt.Image;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import util.IconCache;
import util.Messages;



public class PedigreeIconCache extends IconCache {

	private final static Logger LOGGER = Logger.getLogger (PedigreeIconCache.class);
	private static final String PED_GRAPHICS_PROPERTIES = "napier.pedigree.graphics";
	public static final String FILE_SEPARATOR = "/"; //System.getProperty ("file.separator");
    public static final String IMAGEDIR = Messages.getString (PED_GRAPHICS_PROPERTIES, "ImageDir").replace(".", FILE_SEPARATOR);

    
	public static Icon makeIconFromFilename (final String fileName) {
		final String fileNamePlusDir = IMAGEDIR + FILE_SEPARATOR + fileName;
    	LOGGER.debug ("icon filename: "+fileNamePlusDir);
    	return makeIconAbs (fileNamePlusDir, fileNamePlusDir);
	}

	public static Icon makeIcon (final String descriptor) {	
		final String propertyString = Messages.getString (PED_GRAPHICS_PROPERTIES, descriptor);
		return makeIconRelPath (propertyString);
	}
	
	public static Icon makeIcon (final Class<?> klass, final String descriptor) {
		final String propertyString = Messages.getString (PED_GRAPHICS_PROPERTIES, klass, descriptor);
		return makeIconRelPath (propertyString);
	}
	
	public static Icon makeIconRelPath (final String relativePath) {
		
		final String fileName = IMAGEDIR + FILE_SEPARATOR + relativePath;
		//System.err.println ("filename: "+fileName);
		final Icon icon = relativePath.charAt(0) == Messages.ERROR_CHAR ? null : makeIconAbs (fileName, relativePath);
		if (icon != null && LOGGER.isDebugEnabled()) {
	    	final Image img = ((ImageIcon)icon).getImage();
	    	if (img instanceof RenderedImage) {
	    		testImageChannelDepths ((RenderedImage)img, relativePath);
	    	}	
		}
		return icon;
	}
	
	public static void testImageChannelDepths (final RenderedImage rImage, final String imageFileName) {
		final SampleModel sModel = rImage.getSampleModel();
    	final int[] sampleSize = sModel.getSampleSize();
    	final StringBuilder sBuilder = new StringBuilder ("File: "+imageFileName+"\t");
    	int last = -1;
    	boolean error = false;
    	for (int n= 0; n < sampleSize.length; n++) {
    		sBuilder.append ("Chan "+n+": "+sampleSize[n]+"\t");
    		if (last != -1 && last != sampleSize[n]) {
    			error = true;
    		}
    		last = sampleSize [n];
    	}
    	if (error) {
    		sBuilder.append (imageFileName+" will cause error in Batik 1.7's PNGImageEncoder");
    	}
    	LOGGER.debug (sBuilder.toString());
	}
}
