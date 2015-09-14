package napier.pedigree.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import napier.pedigree.io.ErrorStrings;
import napier.pedigree.io.PropertyConstants;
import napier.pedigree.model.ErrorCollator;
import napier.pedigree.model.PedigreeGenerationModel;
import napier.pedigree.model.impl.DefaultPedigreeGenerationModel;
import napier.pedigree.swing.layout.BoxLayout2;
import napier.pedigree.swing.layout.JViewport2;
import napier.pedigree.swing.renderers.tooltip.RenderAwareOffspringMultiErrorTextRenderer;
import napier.pedigree.util.HTMLLabel;
import napier.pedigree.util.JCollapsiblePanel;

import org.apache.log4j.Logger;
import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.inheritance.model.HeritablePopulation;
import org.resspecies.model.Individual;

import util.GraphicsUtil;
import util.Messages;

public class JGenerationStack extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2442695130213825997L;
	static final private Logger LOGGER = Logger.getLogger (JGenerationStack.class);
	
	protected final MessageFormat headerFormat = new MessageFormat (Messages.getString (PropertyConstants.TEXTPROPS, "GenerationHeaderText"));
	
	protected List<JGeneration> generationList;
	protected Map<Class<?>, TableCellRenderer> sharedRendererMap = new HashMap <Class<?>, TableCellRenderer> ();
	protected TableCellRenderer toolTipErrorStatRenderer;
	
	protected ErrorCollator errorModel;
	
	protected final Border genBorder = BorderFactory.createMatteBorder (1, 0, 0, 0, 
			UIManager.getDefaults().getColor("controlShadow"));
	
	public JGenerationStack () {	
		this ((HeritablePopulation)null);
	}
	
	
	public JGenerationStack (final HeritablePopulation hPop) {	
		super ();
		generationList = new ArrayList<JGeneration> ();
		//sharedSelectionModel = new RowColumnPedigreeSelectionModel ();
		setModel (hPop);
	}
	
	
	public void setModel (final HeritablePopulation hPop) {
		
		toolTipErrorStatRenderer = null;
		
		if (hPop != null) {
			constructStackFromPopulation (hPop);
		}
	}
	
	
	public void constructStackFromPopulation (final HeritablePopulation hPop) {
		this.removeAll ();
		for (JGeneration generation : generationList) {
			
		}
		generationList.clear ();
		
		final TreeMap<Integer, TreeSet<Individual>> generations = hPop.getReverseGenerationMap();
		final List<TableModel> genTableModels = new ArrayList<TableModel> ();
		final Set<Entry<Integer, TreeSet<Individual>>> genEntries = generations.entrySet();
		final Iterator<Entry<Integer, TreeSet<Individual>>> genIter = genEntries.iterator();
		boolean isFirstGen = true;
		
		while (genIter.hasNext()) {
			final Entry<Integer, TreeSet<Individual>> generation = genIter.next();
			// Last generation has no offspring, ignore
			//System.err.println ("generation: "+genEntry.getKey()+" for "+genEntry.getValue().size()+" Individuals.");

			if (!isFirstGen) {
				final PedigreeGenerationModel genTableModel = new DefaultPedigreeGenerationModel (
					hPop.getFamiliesByGeneration (generation.getKey()),
					generation.getKey().intValue()
				);
				genTableModels.add (genTableModel);
				LOGGER.info ("Polygamous generation: "+genTableModel.isPolygamous());
			}
			isFirstGen = false;
		}
		LOGGER.info (generations.toString());
			

		this.setLayout (new BoxLayout2 (this, BoxLayout.Y_AXIS));
	
				
		for (int genIndex = 0; genIndex < genTableModels.size(); genIndex++) {
			final TableModel tableModel = genTableModels.get (genIndex);
			final JGeneration generation = new JGeneration (tableModel);
			setupGeneration (generation);
			generationList.add (generation);
			final JCollapsiblePanel generationPanel = encloseJGeneration (generation);
			this.add (generationPanel);
		}
		
		this.add (Box.createVerticalGlue());
	}
	
	
	
	
	public JGeneration setupGeneration (final JGeneration generation) {
		
		JGenerationUtils.getInstance().setColumnWidthBounds(generation);
		
		if (toolTipErrorStatRenderer != null) {
			setErrorModelBasedToolTip (generation, toolTipErrorStatRenderer);
		}
		
		return generation;
	}
	
	
	
	public JCollapsiblePanel encloseJGeneration (final JGeneration generation) {
		final JScrollPane jspInd = new JScrollPane ();
		jspInd.setViewport (new JViewport2 ());
		jspInd.setViewportView (generation);
		jspInd.setBorder (genBorder);
		jspInd.setVerticalScrollBarPolicy (ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
	
		generation.setFillsViewportHeight (true);
		final String descriptor = headerFormat.format (new Object[] {
				Integer.valueOf (((PedigreeGenerationModel)generation.getModel()).getGenerationIndex())
		});
		final String htmlDescriptor = HTMLLabel.makeHTMLText (descriptor, Color.gray);
		generation.putClientProperty (JGeneration.HTML_DESCRIPTOR, htmlDescriptor);
		generation.setColumnHeaderView (null);
		
		final JCollapsiblePanel generationPanel = new JCollapsiblePanel ();
		generationPanel.setContents (jspInd);
		//generationPanel.setBorder (histogramLabelBorder);
		generationPanel.setHeader (new JLabel (htmlDescriptor));
		generationPanel.getContents().addComponentListener(
			new GenStackResizer (generation)
		);
		
		return generationPanel;
	}
	

	
	public void setErrorModel (final ErrorCollator newErrorModel) throws Exception {
		if (generationList.isEmpty()) {
			throw new IllegalStateException (ErrorStrings.getInstance().getString ("generationStackSetModelOrderError"));
		}
		
		if (newErrorModel != null && ! newErrorModel.equals (errorModel)) {
			errorModel = newErrorModel;
		
			toolTipErrorStatRenderer = new RenderAwareOffspringMultiErrorTextRenderer (getErrorModel(), new DefaultBoundedRangeModel());
			for (JGeneration generation : generationList) {
				setErrorModelBasedToolTip (generation, toolTipErrorStatRenderer);
			}
		}
	}
	
	protected void setErrorModelBasedToolTip (final JGeneration generation, final TableCellRenderer toolTipErrorStatRenderer) {
		final PedigreeRendererToolTip pedToolTip = (PedigreeRendererToolTip)(generation.createToolTip());
		pedToolTip.addRenderer (HeritableIndividual.class, toolTipErrorStatRenderer);
		pedToolTip.addRenderer (ArrayList.class, toolTipErrorStatRenderer);
	}
	
	
	
	public void setSharedRenderer (final Class<?> klass, final TableCellRenderer renderer) {
		sharedRendererMap.put (klass, renderer);
		for (JGeneration generation : generationList) {
			generation.setDefaultRenderer (klass, renderer);
		}
		if (this.isVisible()) {
			repaint ();
		}
	}
	
	public TableCellRenderer getSharedRenderer (final Class<?> klass) {
		return sharedRendererMap.get (klass);
	}
	
	
	public List<JGeneration> getGenerationList () { return generationList; }
	
	
	public ErrorCollator getErrorModel() {
		return errorModel;
	}

	
	@Override
	public void paint (final Graphics graphics) {
		final Graphics2D g2d = (Graphics2D)graphics;
		//final boolean offscreen = GraphicsUtil.isNonScreenImage ((Graphics2D)graphics);
		//g2d.setRenderingHint (RenderingHints.KEY_ANTIALIASING, 
		//		offscreen ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
		g2d.setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		super.paint (graphics);
	}
	
	
	
	protected class GenStackResizer extends ComponentAdapter {
		
		protected JGeneration generation;
		
		GenStackResizer (final JGeneration generation) {
			super ();
			this.generation = generation;
		}
		
		@Override
		public void componentShown (final ComponentEvent cEvent) {
	    	generation.getPreferredSize();		    	
	    	JGenerationStack.this.revalidate();
	    	printSizes ();
	    }
	    
		@Override
	    public void componentHidden (final ComponentEvent cEvent) {
	    	generation.getPreferredSize();
	    	JGenerationStack.this.revalidate();
	    	printSizes ();
	    }
	    
		@Override
	    public void componentResized (final ComponentEvent cEvent) {
	    	generation.getPreferredSize();
	    	JGenerationStack.this.revalidate();
	    	printSizes ();
	    }
	    
	    public void printSizes () {
	    	if (LOGGER.isDebugEnabled()) {
		    	for (JComponent gen : JGenerationStack.this.getGenerationList()) {
			    	final JComponent[] jcomps = {gen, (JComponent)gen.getParent(), 
			    			(JComponent)gen.getParent().getParent(),
			    			(JComponent)gen.getParent().getParent().getParent(),
			    			JGenerationStack.this};
			    	LOGGER.debug ("JGeneration "+gen.getName());
			    	for (JComponent jcomp : jcomps) {
			    		LOGGER.debug ("comp: "+jcomp.getClass()+", pref: "+jcomp.getPreferredSize()+
			    				", max: "+jcomp.getMaximumSize()+", act: "+jcomp.getSize());
			    	}
		    	}
		    	LOGGER.debug ("=------------");
	    	}
	    }
	}
}
