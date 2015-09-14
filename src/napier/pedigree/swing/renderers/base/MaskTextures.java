package napier.pedigree.swing.renderers.base;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;

import util.GraphicsUtil;


public final class MaskTextures {

	private static final MaskTextures INSTANCE = new MaskTextures ();
	
	private MaskTextures () {}

	public static MaskTextures getInstance() { return INSTANCE; }
	
	
	public TexturePaint[] makeTextures (final Color[] textureColours) {
		final TexturePaint[] textures = new TexturePaint [textureColours.length];
		final Rectangle rect = new Rectangle (0, 0, 4, 2);
		
		for (int n = 0; n < textures.length; n++) {
			final BufferedImage bufImg = new BufferedImage (rect.width, rect.height, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D big = bufImg.createGraphics();
			//System.err.println ("big: "+((AlphaComposite)big.getComposite()).getRule());

			big.setBackground (GraphicsUtil.NULLCOLOUR);
			big.clearRect (0, 0, bufImg.getWidth(), bufImg.getHeight());
		    big.setColor (textureColours[n]);
		    big.fillRect (0, 0, bufImg.getWidth() / 2, bufImg.getHeight() / 2);
		    big.fillRect (bufImg.getWidth() / 2, bufImg.getHeight() / 2, bufImg.getWidth() / 2, bufImg.getHeight() / 2);
		    textures [n] = new TexturePaint (bufImg, rect);
		}
		return textures;
	}
}
