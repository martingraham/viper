package napier.pedigree.util;

import java.awt.Color;

public class Spectrum {

	protected Color[] colours;
	
	public Spectrum () {
		this (new Color[0]);
	}
	
	public Spectrum (final Color[] colours) {
		this.colours = colours;
	}
	
	public void setColours (final Color[] newColours) {
		colours = newColours;
	}
	
	public Color getColour (final int index) {
		return (index >= 0 && index < getSize()) ? colours [index] : null;
	}
	
	public int getSize () {
		return colours.length;
	}
}
