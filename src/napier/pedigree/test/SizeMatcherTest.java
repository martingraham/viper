package napier.pedigree.test;

import java.awt.Dimension;

import napier.pedigree.swing.app.AppUtils;


public class SizeMatcherTest {
	
	static public void main (final String[] args) {
		new SizeMatcherTest ();
	}
	
	public SizeMatcherTest () {
		final String[] candidates = {"1", "1024", "1024 by 768", "1024x367", "1280y1024", "678 789", "xxd345x789x765", "1024.768"};
		final Dimension dim = new Dimension ();
		for (String cand : candidates) {
			AppUtils.getInstance().populateDimension2 (cand, dim);
		}
	}
}
