package napier.pedigree.swing.app;

import io.DataPrep;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.RowFilter;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import model.graph.GraphModel;
import napier.pedigree.io.ErrorStrings;
import napier.pedigree.io.LoadDualFileAction;
import napier.pedigree.io.LoadGenotypeAction;
import napier.pedigree.io.LoadPedigreeAction;
import napier.pedigree.io.ParsingErrorDialog;
import napier.pedigree.io.PropertyConstants;
import napier.pedigree.io.SaveAction;
import napier.pedigree.model.ErrorCollator;
import napier.pedigree.model.ModelRowConstants;
import napier.pedigree.model.PedigreeGenerationModel;
import napier.pedigree.model.PedigreeSelectionModel;
import napier.pedigree.model.PopCheckerWrapper;
import napier.pedigree.model.categoriser.Categoriser;
import napier.pedigree.model.filter.IndividualFilter;
import napier.pedigree.model.filter.MarkerFilter;
import napier.pedigree.model.filter.HistogramValueFilter;
import napier.pedigree.model.filter.impl.AtomicIndividualFilters;
import napier.pedigree.model.filter.impl.AtomicMarkerFilters;
import napier.pedigree.model.filter.impl.CompoundMarkerFilters;
import napier.pedigree.model.filter.impl.MarkerByMarkerFilter;
import napier.pedigree.model.impl.DefaultErrorCollator;
import napier.pedigree.model.impl.DefaultPedigreeSelectionModel;
import napier.pedigree.model.impl.DefaultPedigreeGenerationModel;
import napier.pedigree.model.impl.DirectCheckErrorCollator;
import napier.pedigree.model.impl.MultiCoreErrorCollator;
import napier.pedigree.model.impl.PopulateOffspring;
import napier.pedigree.model.sort.TableMultiComparator;
import napier.pedigree.swing.AbstractErrorHistogram;
import napier.pedigree.swing.AbstractHistogramListener;
import napier.pedigree.swing.HistoryPanel;
import napier.pedigree.swing.IndividualErrorHistogram;
import napier.pedigree.swing.GraphFrame;
import napier.pedigree.swing.JGeneration;
import napier.pedigree.swing.JGenerationStack;
import napier.pedigree.swing.JGenerationUtils;
import napier.pedigree.swing.JIndividualTable;
import napier.pedigree.swing.JMarkerTable;
import napier.pedigree.swing.JMaskTable;
import napier.pedigree.swing.MarkerErrorHistogram;
import napier.pedigree.swing.app.actions.AbstractPostSelectionAction;
import napier.pedigree.swing.app.actions.DetailViewAction;
import napier.pedigree.swing.app.actions.FamilyMaskAction;
import napier.pedigree.swing.app.actions.FamilyUnmaskAction;
import napier.pedigree.swing.app.actions.IndividualToggleMaskAction;
import napier.pedigree.swing.app.actions.IndividualToggleNullFatherAction;
import napier.pedigree.swing.app.actions.IndividualToggleNullMotherAction;
import napier.pedigree.swing.app.actions.SelectFamilyAction;
import napier.pedigree.swing.app.actions.SelectIndividualAction;
import napier.pedigree.swing.app.maskers.AllErroredGenotypesMasker;
import napier.pedigree.swing.app.maskers.IndividualMasker;
import napier.pedigree.swing.app.maskers.MarkerMasker;
import napier.pedigree.swing.app.maskers.unused.DualFilteredGenotypeMasker;
import napier.pedigree.swing.app.maskers.unused.MarkerFilteredGenotypeMasker;
import napier.pedigree.swing.layout.ModifiedFlowLayout;
import napier.pedigree.swing.renderers.base.AbstractErrorRenderer;
import napier.pedigree.swing.renderers.base.ErrorColourableRenderer;
import napier.pedigree.swing.renderers.base.MultipleItemsRenderer;
import napier.pedigree.swing.renderers.errortable.IndTableStringRenderer;
import napier.pedigree.swing.renderers.errortable.MarkerTableStringRenderer;
import napier.pedigree.swing.renderers.histogram.AbstractColourScaleRenderer;
import napier.pedigree.swing.renderers.list.GenerationSortListCellRenderer;
import napier.pedigree.undo.HistoryModel;
import napier.pedigree.undo.Memento;
import napier.pedigree.undo.impl.DefaultPedigreeHistoryModel;
import napier.pedigree.util.HTMLLabel;
import napier.pedigree.util.HiderButton;
import napier.pedigree.util.JCollapsiblePanel;
import napier.pedigree.util.ModelScopeWindow;
import napier.pedigree.util.PedigreeIconCache;

import org.apache.log4j.Logger;
import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.inheritance.model.HeritablePopulation;
import org.resspecies.inheritance.model.SNPMarker;
import org.resspecies.model.Individual;
import org.resspecies.parsing.GenotypeFileParser;

import swingPlus.shared.MyFrame;
import swingPlus.shared.SortWidget;
import util.GraphicsUtil;
import util.Messages;
import util.swing.HTMLDialogLaunchAction;
import util.swing.HTMLScrollPane;
import util.swing.TabbedMoveHandler;

public class PedigreeFrame extends MyFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4425253253522758414L;
	
	static final private Logger LOGGER = Logger.getLogger (PedigreeFrame.class);
	public final static String MAIN_WINDOW = "MainWindow";	// Swing name for main interface window
	
	
	protected Preferences prefs;	// For storage/extraction of user and configuration data
	
	protected GenotypeFileParser genotypeParser;			// Current data set
	protected ErrorCollator errorModel;						// Current error model
	protected HistoryModel<PedigreeFrame> hModel;			// Current history model
	protected PedigreeSelectionModel sharedSelectionModel;	// Current selection model
	
	protected JGenerationStack generationStack;				// Panel of generations

	protected IndividualErrorHistogram individualErrorgram, filteredIndividualErrorgram;	// Ind histograms
	protected MarkerErrorHistogram markerErrorgram, filteredMarkerErrorgram;				// Marker histograms
	//protected MarkerErrorHistogramPanel markerErrorPanel;
	
	protected JMarkerTable markerTable;				// Marker table
	protected JIndividualTable individualTable;		// Individual table
	protected JMaskTable maskedIndividualsTable;	// Masked individuals table
	
	protected PropertyAwareHTMLScrollPane htmlPane;	// Data set summary line
	protected DetailFrame detailFrame;				// Frame holding detail (zoom) table
	protected JPanel histogramsPanel;				// Panel holding all histograms
	protected HistoryPanel hPanel;					// Panel holding history details
	protected JSplitPane histToSandwichSplit, sandwichToTablesSplit;	// Split panes to arrange components
	
	// All these controls go along the top of the interface (if we do add them)
	protected JButton saveButton, loadDualButton, loadPedigreeButton, loadGenotypeButton;
	protected JComboBox familyRenderComboBox, genderRenderComboBox;
	protected JComboBox offspringSplitComboBox, errorgramRenderComboBox;
	protected ColourComboBox errorColourComboBox, incompleteColourComboBox, selectionColourComboBox;
	protected JButton aboutButton, helpFileButton;
	
	// These go on the top of the generation stack panel
	protected SortWidget offspringSortButton, generationSortButton;
	
	// These go beneath the generation stack panel
	protected JButton recalculateButton, restoreToLastButton, maskRemainingGenotypesButton;
	
	// These are attached to the marker/individual tables (except most of them aren't used now)
	protected JButton resetFocusMarkerButton;
	protected JCheckBox showGenotypeInTableButton, expandIndToMarkersButton, expandMarkerToIndsButton;

	// Numerous other controls are placed but not referenced globally here
	
	protected GenerationListSelectionListener gListener;
	
	protected Color firstLevelLabelColour = Color.darkGray;
	protected Color secondLevelLabelColour = Color.gray;
		
	static private final Dimension MAXCOMBOBOXSIZE = new Dimension (200, 24);
	protected static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder (0, 5, 0, 4);
	protected static final Border DEBUG_BORDER = BorderFactory.createLineBorder (Color.red, 1);

	
	
	public PedigreeFrame () {
		super ();
		prefs = Preferences.userNodeForPackage (PedigreeFrame.class);

		final Dimension frameSize = new Dimension ();
		AppUtils.getInstance().populateDimension ("mainWindow", frameSize);
		frameSize.setSize (
			prefs.getInt ("Width", frameSize.width), 
			prefs.getInt ("Height", frameSize.height)
		);
		setSize (frameSize);
		this.setName (MAIN_WINDOW);
		
		this.addWindowListener (new ClosingTasks ());
	}
	
	
	/**
	 * Preferred set-up point
	 */
	public void launch () {
		constructInterface ();			// Piece interface together
		connectActionsToInterface ();	// Add actions that don't need to be repopulated on a change of data set
	}
	
	
	/**
	 * Builds the interface components and hierarchy
	 */
	public void constructInterface () {
		
		this.setTitle (getLabelString ("mainWindowTitle"));
		final AppUtils appUtils = AppUtils.getInstance();
		
		sharedSelectionModel = new DefaultPedigreeSelectionModel ();	// Make shared selection model
		
		// Make JGenerationStack (main item in interface)
		generationStack = new JGenerationStack ();
		final JScrollPane stackScrollPane = new JScrollPane (generationStack);
		
		// Detail frame for currently selected family
		detailFrame = new DetailFrame ();
		
		// Widget that controls sort ordering of offspring
		offspringSortButton = new SortWidget ();
		decorateButton (offspringSortButton, "OffspringSort", false);	
		
		// Widget that controls sort ordering of families
		generationSortButton = new SortWidget ();
		decorateButton (generationSortButton, "GenerationSort", false);
		generationSortButton.getSortList().setCellRenderer (new GenerationSortListCellRenderer ());
		
		// ComboBox widgets for selecting rendering & styling choices
		familyRenderComboBox = new JComboBox ();		
		genderRenderComboBox = new JComboBox ();
		offspringSplitComboBox = new JComboBox ();
		errorgramRenderComboBox = new JComboBox ();
		
		final JComboBox[] comboBoxes = {familyRenderComboBox, genderRenderComboBox, offspringSplitComboBox, errorgramRenderComboBox};
		for (JComboBox comboBox : comboBoxes) {
			comboBox.setAlignmentX (0.0f);
			comboBox.setMaximumSize (MAXCOMBOBOXSIZE);
		}
			
		
		// ColourComboBox widgets for selecting colours for error/selection/mask information
		errorColourComboBox = new ColourComboBox ();
		incompleteColourComboBox = new ColourComboBox ();	
		selectionColourComboBox = new ColourComboBox ();
		selectionColourComboBox.setColourModel (new Color[] {Color.orange, Color.yellow, new Color (128, 255, 64), new Color (164, 192, 255)}, 
				Messages.getString(PropertyConstants.TEXTPROPS, "SelectionColourComboBoxLabels").split("\\|"));			
	
		// Html ticker that goes at the bottom of the screen
		htmlPane = new PropertyAwareHTMLScrollPane ();
		htmlPane.setHorizontalScrollBarPolicy (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		htmlPane.setVerticalScrollBarPolicy (ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		htmlPane.setAlignmentX (0.0f);
		htmlPane.setPreferredSize (new Dimension (htmlPane.getPreferredSize().width, 32));
		htmlPane.setHTMLURL ("/napier/pedigree/swing/app/", "summary.html");
		htmlPane.format (htmlPane.getDefaultStrings());
		
		// MarkerTable and associated controls
		markerTable = new JMarkerTable ();
		markerTable.setAutoResizeMode (JTable.AUTO_RESIZE_ALL_COLUMNS);
		markerTable.getTableHeader().setReorderingAllowed (true);
		markerTable.getTableHeader().setResizingAllowed (true);
		markerTable.setPreferredScrollableViewportSize (new Dimension (200, markerTable.getPreferredScrollableViewportSize().height));

		resetFocusMarkerButton = new JButton ();
		decorateButton (resetFocusMarkerButton, "clearFocusMarker");
		resetFocusMarkerButton.setEnabled (false);

		expandMarkerToIndsButton = new JCheckBox ();
		decorateButton (expandMarkerToIndsButton, "expandMarkerToInds");
		
		final JPanel markerTablePanel = new JPanel (new BorderLayout());
		final JPanel markerTableButtonPanel = new JPanel (new GridLayout (0, 1));
		markerTableButtonPanel.add (resetFocusMarkerButton);
		if (isPropertyTrue ("showExpandMarkerButton")) {
			markerTableButtonPanel.add (expandMarkerToIndsButton);
		}
		markerTablePanel.add (markerTableButtonPanel, BorderLayout.NORTH);
		final JScrollPane markerTablePane = new JScrollPane (markerTable);
		markerTablePanel.add (markerTablePane, BorderLayout.CENTER);
	
			
		// IndividualTable and associated controls
		individualTable = new JIndividualTable ();
		individualTable.setAutoResizeMode (JTable.AUTO_RESIZE_ALL_COLUMNS);
		individualTable.getTableHeader().setReorderingAllowed (true);
		individualTable.getTableHeader().setResizingAllowed (true);
		individualTable.setPreferredScrollableViewportSize (new Dimension (200, individualTable.getPreferredScrollableViewportSize().height));
		
		showGenotypeInTableButton = new JCheckBox ();
		decorateButton (showGenotypeInTableButton, "showGenotypeInTable");
		//showGenotypeInTableButton.setEnabled (false);
		
		expandIndToMarkersButton = new JCheckBox ();
		decorateButton (expandIndToMarkersButton, "expandIndToMarkers");
		
		final JPanel individualTablePanel = new JPanel (new BorderLayout());
		final JPanel indTableButtonPanel = new JPanel (new GridLayout (0, 1));
		if (isPropertyTrue ("showFocusGenotypeButton")) {
			indTableButtonPanel.add (showGenotypeInTableButton);
		}
		if (isPropertyTrue ("showExpandIndividualButton")) {
			indTableButtonPanel.add (expandIndToMarkersButton);
		}
		individualTablePanel.add (indTableButtonPanel, BorderLayout.NORTH);
		final JScrollPane individualTablePane = new JScrollPane (individualTable);
		individualTablePanel.add (individualTablePane, BorderLayout.CENTER);
		
					
		// Masked Individuals Table and Tab Pane
		maskedIndividualsTable = new JMaskTable ();
		maskedIndividualsTable.setAutoResizeMode (JTable.AUTO_RESIZE_ALL_COLUMNS);
		maskedIndividualsTable.getTableHeader().setReorderingAllowed (true);
		maskedIndividualsTable.getTableHeader().setResizingAllowed (true);
		maskedIndividualsTable.setPreferredScrollableViewportSize (new Dimension (200, maskedIndividualsTable.getPreferredScrollableViewportSize().height));
		
		final JLabel clearIndividualsAdvice = this.makeHTMLLabel ("clearAllMaskedIndividuals", Color.darkGray);
		
		final JPanel maskedIndividualsTablePanel = new JPanel (new BorderLayout());
		maskedIndividualsTablePanel.add (clearIndividualsAdvice, BorderLayout.NORTH);
		final JScrollPane maskedIndividualsTablePane = new JScrollPane (maskedIndividualsTable);
		maskedIndividualsTablePanel.add (maskedIndividualsTablePane, BorderLayout.CENTER);
		
		
		// Make error histograms
		UIManager.getDefaults().put ("Slider.tickColor", Color.lightGray);
				
		individualErrorgram = new IndividualErrorHistogram ();		
		individualErrorgram.setPreferredSize (new Dimension (individualErrorgram.getPreferredSize().width, 48));
				
		filteredIndividualErrorgram = new IndividualErrorHistogram ();
		
		markerErrorgram = new MarkerErrorHistogram();
		markerErrorgram.setPreferredSize (new Dimension (markerErrorgram.getPreferredSize().width, 48));		
		
		filteredMarkerErrorgram = new MarkerErrorHistogram ();
				
		final AbstractErrorHistogram[] histograms = getHistograms ();
		for (AbstractErrorHistogram histogram : histograms) {
			histogram.putClientProperty ("Slider.paintThumbArrowShape", Boolean.TRUE);
		}
		
		// Decides if histogram sliders reset to zero after being moved (i.e. snaps back)
		final boolean[] snapbacks = {false, false, false, false};
		for (int n = 0; n < Math.min (snapbacks.length, histograms.length); n++) {
			histograms[n].setSnapback (snapbacks[n]);
		}

				
		// Collapsible histogram panels
		histogramsPanel = new JPanel();
		histogramsPanel.setLayout (new BoxLayout (histogramsPanel, BoxLayout.Y_AXIS));
		
		final Map<AbstractErrorHistogram, String> histogramPrefixMap = new LinkedHashMap<AbstractErrorHistogram, String> (); 
		histogramPrefixMap.put (markerErrorgram, "markerErrorHistogram");
		histogramPrefixMap.put (filteredMarkerErrorgram, "filteredMarkerErrorHistogram");
		if (isPropertyTrue ("showIndividualMaskingHistogram")) {
			histogramPrefixMap.put (individualErrorgram, "indErrorHistogram");
		}
		histogramPrefixMap.put (filteredIndividualErrorgram, "filteredIndErrorHistogram");

		for (Entry<AbstractErrorHistogram, String> histoEntry : histogramPrefixMap.entrySet()) {
			final AbstractErrorHistogram histogram = histoEntry.getKey();
			final String prefix = histoEntry.getValue();
			final JCollapsiblePanel errorHistogramPanel = makeHistogramPanel (histogram, prefix);
			histogramsPanel.add (errorHistogramPanel);
		}
		
		
		// Toolbar for sandwich view specific options
		final JToolBar sandwichToolBar = new JToolBar ();
		final JLabel sandLabel = makeHTMLLabel ("sandwichView", secondLevelLabelColour);
		sandwichToolBar.add (sandLabel);
		sandwichToolBar.add (Box.createRigidArea (new Dimension (5, 5)));
		final HiderButton hider = new HiderButton ();
		sandwichToolBar.add (hider);
		sandwichToolBar.addSeparator ();
		//hider.setBorder (UIManager.getDefaults().getBorder ("button.border"));
		addSandwichOptions (sandwichToolBar);
		sandwichToolBar.setFloatable (false);

		// Toolbar for recalculation and clearing remaining errored genotypes
		final JToolBar recalcBar = new JToolBar ();
		recalcBar.add (Box.createGlue());
		
		recalculateButton = new JButton ();
		restoreToLastButton = new JButton ();
		maskRemainingGenotypesButton = new JButton ();
		
		final Map<AbstractButton, String> buttonPrefixMap = new LinkedHashMap <AbstractButton, String> ();
		buttonPrefixMap.put (recalculateButton, "recalculate");
		buttonPrefixMap.put (restoreToLastButton, "restoreToLast");
		buttonPrefixMap.put (maskRemainingGenotypesButton, "maskRemainingGenotypes");
		for (Entry<AbstractButton, String> buttonEntry : buttonPrefixMap.entrySet()) {
			final AbstractButton button = buttonEntry.getKey();
			decorateButton (button, buttonEntry.getValue());
			recalcBar.add (button);
			recalcBar.add (Box.createGlue());
		}
		

		// History Panel
		hPanel = new HistoryPanel ();
		final JCollapsiblePanel historyCollapsePanel = new JCollapsiblePanel ();
		final JLabel histoLabel = makeHTMLLabel ("history", secondLevelLabelColour);
		historyCollapsePanel.setHeader (histoLabel);
		final Border histogramLabelBorder = BorderFactory.createMatteBorder (1, 0, 0, 0, 
				UIManager.getDefaults().getColor("controlShadow"));
		historyCollapsePanel.setBorder (histogramLabelBorder);
		historyCollapsePanel.setContents (hPanel);
		
			
		// Agglomerate generation stack, sandwich options and recalc bar into one component
		final JPanel genStackPanel = new JPanel ();
		genStackPanel.setLayout (new BorderLayout ());
		genStackPanel.add (sandwichToolBar, BorderLayout.NORTH);
		genStackPanel.add (stackScrollPane, BorderLayout.CENTER);
		genStackPanel.add (recalcBar, BorderLayout.SOUTH);
			
		
		// Make a Split pane between the sandwich view and the history view
		histToSandwichSplit = new JSplitPane (JSplitPane.VERTICAL_SPLIT, genStackPanel, historyCollapsePanel);
		histToSandwichSplit.setOneTouchExpandable (true);
		histToSandwichSplit.setResizeWeight (1.0);
		//histToSandwichSplit.setDividerLocation (0.75);
		
		
		// Right-hand side tabbed pane section
		final JTabbedPane jtp = new JTabbedPane ();
		jtp.add (getLabelString ("markerTableTabText"), markerTablePanel);
		jtp.add (getLabelString ("individualTableTabText"), individualTablePanel);
		jtp.add (getLabelString ("maskedIndividualsTableTabText"), maskedIndividualsTablePanel);
		
		final TabbedMoveHandler tearOffAdapter = new TabbedMoveHandler (this);
		tearOffAdapter.setAllowPaletteDialogs (true);
		tearOffAdapter.addToTabbedPane (jtp);
		//jtp.add (getLabelString ("detailTableTabText"), detailScroll);
		
		
		// Split pane between sandwich/history split pane and tabbed pane
		sandwichToTablesSplit = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, histToSandwichSplit, jtp);
		sandwichToTablesSplit.setOneTouchExpandable (true);
		sandwichToTablesSplit.setResizeWeight (1.0);
		//sandwichToTablesSplit.setDividerLocation (0.75);
		
		
		// Final top level split between histograms and everything underneath
		final JPanel innerAppPanel = new JPanel (new BorderLayout (5, 1));
		innerAppPanel.add (histogramsPanel, BorderLayout.NORTH);
		innerAppPanel.add (sandwichToTablesSplit, BorderLayout.CENTER);
		
		final JPanel outerAppPanel = new JPanel (new BorderLayout ());
		outerAppPanel.setBorder (BorderFactory.createEmptyBorder (3, 3, 3, 3));
		outerAppPanel.add (htmlPane, BorderLayout.NORTH);
		outerAppPanel.add (innerAppPanel, BorderLayout.CENTER);
		
		getContentPane().add (outerAppPanel, BorderLayout.CENTER);
		
				
		// Add options to a set of tool bars, which are then added to a panel
		final Container[] optionContainers = makeOptionComponents ();
		final JPanel multiToolBarPanel = new JPanel (new ModifiedFlowLayout (FlowLayout.LEFT, 5, 2));
		multiToolBarPanel.add (((BorderLayout)getContentPane().getLayout()).getLayoutComponent(BorderLayout.NORTH));
		for (Container contain : optionContainers) {
			multiToolBarPanel.add (contain);
		}
		
		// Add file options to a tool bar
		final JToolBar fileBar = new JToolBar (getLabelString ("fileMenuText"));
		fileBar.add (AppUtils.getInstance().makeLabel ("fileOptions", Color.darkGray, true));
		fileBar.add (Box.createHorizontalStrut(5));
		
		loadDualButton = new JButton (new LoadDualFileAction (KeyStroke.getKeyStroke (KeyEvent.VK_L, KeyEvent.ALT_MASK), "LoadDual", this));
		loadPedigreeButton = new JButton (new LoadPedigreeAction (KeyStroke.getKeyStroke (KeyEvent.VK_P, KeyEvent.ALT_MASK), "LoadPedigree", this));
		loadGenotypeButton = new JButton (new LoadGenotypeAction (KeyStroke.getKeyStroke (KeyEvent.VK_G, KeyEvent.ALT_MASK), "LoadGenotype", this));
		saveButton = new JButton (new SaveAction (KeyStroke.getKeyStroke (KeyEvent.VK_S, KeyEvent.ALT_MASK), "Save", this));
		//final JButton[] buttons = {loadDualButton, loadPedigreeButton, saveButton};		
		final JButton[] buttons = {loadPedigreeButton, loadGenotypeButton, saveButton};
		for (JButton button : buttons) {
			button.setIcon (null);
			fileBar.add (button);
			fileBar.add (Box.createHorizontalStrut(5));
		}
		loadGenotypeButton.setEnabled (false);
		
		multiToolBarPanel.add (fileBar, 0);		
		multiToolBarPanel.add (Box.createHorizontalGlue());

		
		// Add about and help buttons to separate panel
		aboutButton = new JButton ();
		this.decorateButton (aboutButton, "about", true);
		aboutButton.setVerticalAlignment (SwingConstants.TOP);
		
		helpFileButton = new JButton ();
		this.decorateButton (helpFileButton, "help", true);
		helpFileButton.setVerticalAlignment (SwingConstants.TOP);
		
		final JPanel viperButtonPanel = new JPanel ();
		viperButtonPanel.add (aboutButton);
		viperButtonPanel.add (helpFileButton);
		viperButtonPanel.setLayout (new BoxLayout (viperButtonPanel, BoxLayout.X_AXIS));
		
		// Put the panel holding all these tool bars at the top of the screen
		final JPanel topPanel = new JPanel ();
		topPanel.setLayout (new BoxLayout (topPanel, BoxLayout.X_AXIS));
		topPanel.add (multiToolBarPanel);
		topPanel.add (viperButtonPanel);
		getContentPane().add (topPanel, BorderLayout.NORTH);
		//getContentPane().add (multiToolBarPanel, BorderLayout.NORTH);
		
		
		// Set preference keys
	    appUtils.setPrefKeyString (familyRenderComboBox, "FamilyRenderChoice");
	    appUtils.setPrefKeyString (genderRenderComboBox, "GenderRenderChoice");
	    appUtils.setPrefKeyString (offspringSplitComboBox, "SplitOffspringChoice");
	    appUtils.setPrefKeyString (errorgramRenderComboBox, "ErrorgramRenderChoice");
	    appUtils.setPrefKeyString (histToSandwichSplit, "HistSandwichSplitPosition");
	    appUtils.setPrefKeyString (sandwichToTablesSplit, "SandwichTablesSplitPosition");

	    // Show it
		setVisible (true);	
	}
	
	
	
	/**
	 * Adds actions to the interface components we've previously initialised
	 * At this stage, we're not adding actions that require in-depth knowledge of a data set,
	 * just actions that are aware there will be a data set
	 */
	public void connectActionsToInterface () {
		
		final AppUtils appUtils = AppUtils.getInstance();
		
		/*
		 * ActionListeners for widgets.
		 * ActionListeners that are cumbersome (i.e. > 15 lines or so)
		 * are described as inner classes listed at the end of the PedigreeFrame (this) class.
		 * Shorter action listeners are just incorporated as anonymous classes.
		 */
			
		// Add action that repaints resorted offspring after offspring sort action
		offspringSortButton.addListSelectionListener (
			new ListSelectionListener () {
				public void valueChanged (final ListSelectionEvent lsEvent) {
					final JList source = (JList) lsEvent.getSource ();
		            if (lsEvent.getFirstIndex() == source.getModel().getSize() && lsEvent.getLastIndex() == source.getModel().getSize()
		                      && !lsEvent.getValueIsAdjusting()) {
		            	globalRepaint ();
		            }
				}	
			}			
		);
		
		
		// Add action that sorts columns on parent sort action
		gListener = new GenerationListSelectionListener ();
		generationSortButton.addListSelectionListener (gListener);
		
		
		// Action that resets offspring renderers in JGenerations upon new renderer choice in familyRenderComboBox
		// Also disables offspring sort button if offspring aren't shown individually
		final ActionListener aggregatorComboListener = new ActionListener () {
			@Override
			public void actionPerformed (final ActionEvent aEvent) {
				final JComboBox comboBox = (JComboBox)aEvent.getSource();
				final TableCellRenderer tcr = (TableCellRenderer)comboBox.getSelectedItem();
				generationStack.setSharedRenderer (AbstractCollection.class, tcr);
				final boolean isSpaceFillingRenderer = (tcr instanceof MultipleItemsRenderer);
				//if (isSpaceFillingRenderer) {
					detailFrame.getDetailTable().setDefaultRenderer (AbstractCollection.class, tcr);
					detailFrame.getDetailTable().repaint();
				//}
				offspringSortButton.setEnabled (isSpaceFillingRenderer);		
			}
		};
		familyRenderComboBox.addActionListener (aggregatorComboListener);
		
		
		// Action that resets parent renderers in JGenerations upon new choice in genderRenderComboBox
		// NOTE: genderRenderComboBox is currently not used or displayed
		final ActionListener genderRenderComboListener = new ActionListener () {
			@Override
			public void actionPerformed (final ActionEvent aEvent) {
				final JComboBox comboBox2 = (JComboBox)aEvent.getSource();
				final TableCellRenderer tcr = (TableCellRenderer)comboBox2.getSelectedItem();
				generationStack.setSharedRenderer (HeritableIndividual.class, tcr);
				detailFrame.getDetailTable().setDefaultRenderer (HeritableIndividual.class, tcr);
				detailFrame.getDetailTable().repaint();
			}
		};
		genderRenderComboBox.addActionListener (genderRenderComboListener);
		
		
		// Add listener to offspringSplitComboBox that recategorises families upon a selection
		offspringSplitComboBox.addActionListener (new SplitRowListener ());
		
		
		// Add listener to errogramRendererComboBox that controls errorgram style (control currently omitted from interface)
		final ActionListener errorgramRenderComboListener = new ActionListener () {
			@Override
			public void actionPerformed (final ActionEvent aEvent) {
				final JComboBox comboBox2 = (JComboBox)aEvent.getSource();
				final AbstractColourScaleRenderer histoRenderer = (AbstractColourScaleRenderer)comboBox2.getSelectedItem();
				final AbstractErrorHistogram[] histograms = {markerErrorgram, filteredMarkerErrorgram, individualErrorgram, filteredIndividualErrorgram};
				for (AbstractErrorHistogram histogram : histograms) {
					histogram.setRenderer (histoRenderer);
				}
			}
		};
		errorgramRenderComboBox.addActionListener (errorgramRenderComboListener);
				
		
		// Listeners for ColourComboBox es that take values previously stored in Preferences nodes as their default
		final ActionListener colourComboListener = errorColourComboBox.new WindowColourUpdateListener () {
			@Override
			public void updateRenderers (final Color chosenColour) {
				if (errorColourComboBox.getColourableRenderers() != null) {
					for (ErrorColourableRenderer colourableRenderer : errorColourComboBox.getColourableRenderers()) {
						colourableRenderer.setErrorColourScale (colourableRenderer.makeColourScale (chosenColour, 4));
					}
				}

			}
		};
		errorColourComboBox.addActionListener (colourComboListener);
		appUtils.setPrefKeyString (errorColourComboBox, "ErrorColourChoice");
		errorColourComboBox.setSelectedIndex (prefs.getInt (appUtils.getPrefKeyString (errorColourComboBox), 0));
		
			
		final ActionListener inColourComboListener = incompleteColourComboBox.new WindowColourUpdateListener () {
			@Override
			public void updateRenderers (final Color chosenColour) {
				if (incompleteColourComboBox.getColourableRenderers() != null) {
					for (ErrorColourableRenderer colourableRenderer : incompleteColourComboBox.getColourableRenderers()) {
						colourableRenderer.setIncompleteColourScale (colourableRenderer.makeColourScale (chosenColour, 5));
					}
				}
			}
		};
		incompleteColourComboBox.addActionListener (inColourComboListener);
		appUtils.setPrefKeyString (incompleteColourComboBox, "IncompleteColourChoice");
		incompleteColourComboBox.setSelectedIndex (prefs.getInt (appUtils.getPrefKeyString (incompleteColourComboBox), 0));
		
		
		final ActionListener selectionColourComboListener = selectionColourComboBox.new WindowColourUpdateListener () {
			@Override
			public void updateRenderers (final Color chosenColour) {
				AbstractErrorRenderer.setSelectedColour (chosenColour);
			}
		};
		selectionColourComboBox.addActionListener (selectionColourComboListener);
		appUtils.setPrefKeyString (selectionColourComboBox, "SelectionColourChoice");
		selectionColourComboBox.setSelectedIndex (prefs.getInt (appUtils.getPrefKeyString (selectionColourComboBox), 0));
		
			
		// Action Listener that controls what happens when a selection is made in the markerTable (a focus marker is chosen)
		markerTable.getSelectionModel().addListSelectionListener (
			new ListSelectionListener () {
				@Override
				public void valueChanged (final ListSelectionEvent lse) {
					// Only when non adjusting and selection made in first column (as viewed)
					if (!lse.getValueIsAdjusting() && !markerTable.isCellEditable (0, markerTable.getSelectedColumn())) {
						LOGGER.debug ("lse: "+lse+"\nselected row: "+markerTable.getSelectedRow());
						final int selectedRow = markerTable.getSelectedRow();
						final SNPMarker marker = selectedRow >= 0 ? markerTable.getMarker (selectedRow) : null;
						resetFocusMarkerButton.setEnabled (marker != null);
						showGenotypeInTableButton.setEnabled (marker != null);
						final int lastMinHeight = getRendererMinHeight (HeritableIndividual.class);
						markerTable.getErrorModel().getPopCheckerContext().setFocusMarker (marker);
						markerTable.getErrorModel().filter();
						PedigreeFrame.this.adjustRowHeights (lastMinHeight);
						PedigreeFrame.this.globalRepaint();
						LOGGER.debug ("Selected marker: "+(marker == null ? "null" : marker.getName()));
					}
				}	
			}
		);
		
		
		// Reset focus marker information with this listener
		resetFocusMarkerButton.addActionListener (
			new ActionListener () {
				@Override
				public void actionPerformed (final ActionEvent aEvent) {
					// This causes the ListSelectionListener above to be called with a selectedRow of -1
					if (markerTable.getSelectedColumn() != 0) {
						// Set selected column to zero, so it doesn't fall foul of conditions in valueChanged above
						// Necessary as hand masking markers set the selected column to the last column in the table rather than the first
						markerTable.getColumnModel().getSelectionModel().setSelectionInterval (0, 0);
					}
					markerTable.clearSelection();
				}
			}
		);
		
		
		// Action listener that controls what happens when individual selected in the indvidualTable
		individualTable.getSelectionModel().addListSelectionListener (
			new ListSelectionListener () {
				@Override
				public void valueChanged (final ListSelectionEvent lse) {
					if (!lse.getValueIsAdjusting()) {
						LOGGER.debug ("lse: "+lse+"\nselected row: "+individualTable.getSelectedRow());
						final int selectedRow = individualTable.getSelectedRow();
						final Individual ind = selectedRow >= 0 ? individualTable.getIndividual (selectedRow) : null;
						PedigreeFrame.this.globalRepaint();
						LOGGER.debug ("Selected individual: "+(ind == null ? "null" : ind.getName()));
					}
				}	
			}
		);
		
		
		// When changing the slider value in the filtered individual errogram, redraw the whole interface (this controls greying out)
		filteredIndividualErrorgram.addChangeListener (
			new AbstractHistogramListener () {
				@Override
				public void doStuff (final AbstractErrorHistogram histo, final int curBottomValue, final int curTopValue) {
					globalRepaint ();
					individualTable.repaint();
				}
			}	
		);
		
		
		// Add listeners to the recalculation toolbar's buttons
		recalculateButton.addActionListener (
			new ActionListener () {
				@Override
				public void actionPerformed (final ActionEvent aEvent) {
					errorModel.recalculate();	
				}	
			}
		);
			
		restoreToLastButton.addActionListener (
			new ActionListener () {
				@Override
				public void actionPerformed (final ActionEvent aEvent) {
					hModel.restoreHistoryState ((Memento<PedigreeFrame>)hModel.getHistoryModelTree().getLastSelected());	
				}	
			}
		);
		
		maskRemainingGenotypesButton.addActionListener (
			new ActionListener () {
				@Override
				public void actionPerformed (final ActionEvent aEvent) {
					final AllErroredGenotypesMasker masker = new AllErroredGenotypesMasker (errorModel);
					masker.mask ();
					errorModel.recalculate();
				}	
			}
		);
		
		
		// Add listeners to the about and help buttons
		aboutButton.addActionListener(
				new HTMLDialogLaunchAction (KeyEvent.VK_1, "About",
						DataPrep.getInstance().getRelativeURL (getLabelString ("aboutFile")), 450)
		);
			
		helpFileButton.addActionListener(
			new ActionListener () {
				@Override
				public void actionPerformed (final ActionEvent aEvent) {
					final String helpFileURLString = getLabelString ("helpFile");
					try {
						if (Desktop.isDesktopSupported()) {
							Desktop.getDesktop().browse (new URI (helpFileURLString));
						}
					} catch (final IOException e1) {
						LOGGER.error (ErrorStrings.getInstance().getString("HelpFileOpenError", new String[] {helpFileURLString}), e1);
					} catch (final URISyntaxException e1) {
						LOGGER.error (ErrorStrings.getInstance().getString("HelpFileURIError", new String[] {helpFileURLString}), e1);
					}
				}	
			}
		);
		
		
		histToSandwichSplit.setDividerLocation (prefs.getDouble (appUtils.getPrefKeyString (histToSandwichSplit), 0.75));
		sandwichToTablesSplit.setDividerLocation (prefs.getDouble (appUtils.getPrefKeyString (sandwichToTablesSplit), 0.75));
		
		loadPedigreeButton.doClick();
	}
	
	
	

	
	public void setModel (final GenotypeFileParser newGenotypeParser) {
		
		if (newGenotypeParser != genotypeParser && newGenotypeParser != null) { 	
			
			// Before anything else, kill any extra windows like pedigree views	
			if (errorModel != null) {
				SwingUtilities.invokeLater(
					new Runnable () {

						@Override
						public void run() {
							final Window[] windows = Window.getWindows();
							for (Window window : windows) {
								if (window instanceof ModelScopeWindow) {
									LOGGER.debug ("Window: "+window);
									final WindowEvent windowClosing = new WindowEvent (window, WindowEvent.WINDOW_CLOSING);
									LOGGER.debug ("windowClosing: "+windowClosing);
									final WindowEvent windowClosing2 = new WindowEvent (window, WindowEvent.WINDOW_STATE_CHANGED);
									Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent (windowClosing);
									Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent (windowClosing2);
								}
							}
							
						}
						
					}
				);
			}

			
			final String phaseText[] = getLabelString("loadingBarText").split("\\|");

			final SwingWorker<Void, Void> swork = new SwingWorker<Void, Void> () {
				@Override
			    public Void doInBackground() {	
					setProgress (1);
					newGenotypeParser.parseFilesForGenotypedPopulation();	// Load file into genotype parser

                    //////////////////////////////////////////////////////////////
                    //ESSENTIAL TOO SET FIRING OF INFFERED EVENTS ON IF REQUIRED//
                    //////////////////////////////////////////////////////////////
                    
                    //this is actually the default state - only firing inheritance error events
                    newGenotypeParser.getGenotypedPopulation().setReporting (true, false);

					setProgress (2);

					// Stuff done outside EDT, which means an active progress bar can be updating
					if (newGenotypeParser.getFatalErrorCount() == 0) {

						// Tidy up. Remove all property listeners from old error model.
						if (errorModel != null) {
							final PropertyChangeListener[] allListeners = errorModel.getPropertyChangeSupport().getPropertyChangeListeners();
							for (PropertyChangeListener listener : allListeners) {
								errorModel.getPropertyChangeSupport().removePropertyChangeListener (listener);
							}
							
							errorModel.setModel (null);	// clear connections/listeners to model
						}
						
						// Tidy up. Remove all JGeneration's listening on PedigreeSelectionModel's graph
				    	final List<JGeneration> allJGens = new ArrayList<JGeneration> (generationStack.getGenerationList());
				    	allJGens.add (detailFrame.getDetailTable());
						
				    	if (sharedSelectionModel != null) {
					    	final GraphModel gModel = sharedSelectionModel.getSelectedGraph();
					    	if (gModel != null) {
						    	for (JGeneration generation : allJGens) {
									gModel.removeGraphModelListener (generation);
						    	}
					    	}
				    	}
				    	
				    	// Tidy up. Remove all JGeneration mouse listeners.
				    	// Essential, as detailTable in particular has a life cycle that is beyond the range of individual data models, so
				    	// it was hoarding mouseListeners (to ActionListPopupMenu's) with references to old data models that led to memory leaks.
				    	for (JGeneration generation : allJGens) {
				    		final MouseListener[] allMListeners = generation.getMouseListeners();
							for (MouseListener mListener : allMListeners) {
								generation.removeMouseListener (mListener);
							}
				    	}
				    	
				    	final JTable[] tables = {markerTable, individualTable};
				    	for (JTable table : tables) {
				    		final MouseListener[] allMListeners = table.getMouseListeners();
				    		for (MouseListener mListener : allMListeners) {
				    			if (mListener instanceof ActionListPopupMenu) {
				    				table.removeMouseListener (mListener);
				    			}
							}
				    	}
						
						// Tidy up. Clear old selection state.
						if (sharedSelectionModel != null) {
							sharedSelectionModel.clearSelection();
						}
						
						
						// Make new models for new data set
						final HeritablePopulation hPop = newGenotypeParser.getGenotypedPopulation();
                        
                        hModel = new DefaultPedigreeHistoryModel ();
                        hModel.setOriginator (PedigreeFrame.this);
                        hPanel.setHistoryModel (hModel);
                        
                        //errorModel = new DefaultErrorCollator ();
                        errorModel = new DirectCheckErrorCollator ();
                        //errorModel = new MultiCoreErrorCollator ();
                        errorModel.setModel (hPop);
                        errorModel.getPropertyChangeSupport().addPropertyChangeListener (new RepaintListener ());
                        //errorModel.getPropertyChangeSupport().addPropertyChangeListener (ErrorCollator.STORE_HISTORY, hModel);
			
						PopulateOffspring.getInstance().populateAllOffspring (hPop);
						genotypeParser = newGenotypeParser;
					} else {
						new ParsingErrorDialog().showTheseErrors (PedigreeFrame.this, newGenotypeParser.getParsingErrors(), 10);
					}

					return null;
			    }
				
		        /**
		         * Executed in EDT
		         */
		        @Override
		        public void done() {
		        	populateInterfaceWithModel (newGenotypeParser);
		        }
			};
			
			// Run the SwingWorker which loads the data and updates the interface as above
			// The EDTUpdatingTask blocks user input and provides a visual update cue until
			// the task is finished.
			new EDTUpdatingTask (this, swork, phaseText).doIt();
		}
	}
	
	
	
	/**
	 * Populate the interface with data from the GenotypeFileParser and contained HeritablePopulation
	 * Some ActionListeners that depend on handles to a model are also added
	 * @param newGenotypeParser
	 */
	protected void populateInterfaceWithModel (final GenotypeFileParser newGenotypeParser) {

        setCursor (null);	// Turn off the wait cursor
		
		if (newGenotypeParser.getFatalErrorCount() == 0) {
			final AppUtils appUtils = AppUtils.getInstance ();
			final PropertyChangeSupport errorModelPropSupport = errorModel.getPropertyChangeSupport();
			
			// Get heritable population object.
			final HeritablePopulation hPop = genotypeParser.getGenotypedPopulation();
			
			// Set-up Generation Stack
			generationStack.setModel (hPop);
			try {
				sharedSelectionModel.setErrorModel (errorModel);
				generationStack.setErrorModel (errorModel);
			} catch (final Exception exc) {
				LOGGER.error (exc);
			}
			
			  	
	    	// Update detail table with current data
	    	final JGeneration detailTable = detailFrame.getDetailTable();
	    	final int detailGenIndex = 1;
			detailTable.setModel (new DefaultPedigreeGenerationModel (hPop.getFamiliesByGeneration (Integer.valueOf (detailGenIndex)), detailGenIndex));
			generationStack.setupGeneration (detailTable);
					
			// Make a list of all JGeneration components, including the detail view 
	    	final List<JGeneration> allJGens = new ArrayList<JGeneration> (generationStack.getGenerationList());
	    	allJGens.add (detailTable);
	    	
	    	for (JGeneration gen : allJGens) {
	    		gen.setPedigreeSelection (sharedSelectionModel);
	    	}
	    		    	
	    	
			// Construct sorters specific to this data set
			final SorterHolder sorterObj = new SorterHolder ();
			sorterObj.make (errorModel, allJGens);
			
			// Attach them to all JGeneration tooltip renderers
			JGenerationUtils.getInstance().setOffspringSort (allJGens, sorterObj.getOffspringSorters());
						
			// Construct render objects, mostly TableCellRenderers
			final RendererHolder renderObj = new RendererHolder ();
			renderObj.make (errorModel, sorterObj, filteredIndividualErrorgram.getModel());

			// Update widgets in options panel
			offspringSortButton.setSortListModel (sorterObj.getOffspringSorters().getComparatorList());
			generationSortButton.setSortListModel (sorterObj.getGenerationSorters().getComparatorList());
			gListener.setSorterHolder (sorterObj);
			
			familyRenderComboBox.setModel (new DefaultComboBoxModel (renderObj.getFamilyRenderers()));
			familyRenderComboBox.setSelectedIndex (prefs.getInt (appUtils.getPrefKeyString (familyRenderComboBox), 1));		
			
			offspringSplitComboBox.setModel (new DefaultComboBoxModel (renderObj.getCategorisers()));
			offspringSplitComboBox.setSelectedIndex (prefs.getInt (appUtils.getPrefKeyString (offspringSplitComboBox), 0));
					
			errorColourComboBox.setColourableRenderers (renderObj.getColourableRenderers());
			errorColourComboBox.setSelectedIndex (errorColourComboBox.getSelectedIndex ());
			
			incompleteColourComboBox.setColourableRenderers (renderObj.getColourableRenderers());
			incompleteColourComboBox.setSelectedIndex (incompleteColourComboBox.getSelectedIndex ());
			
			// NOT USED NOW
			errorgramRenderComboBox.setModel (new DefaultComboBoxModel (renderObj.getErrorgramRenderers()));
			errorgramRenderComboBox.setSelectedIndex (1); //prefs.getInt (appUtils.getPrefKeyString (errorgramRenderComboBox), 1));	
			
			// NOT USED NOW
			genderRenderComboBox.setModel (new DefaultComboBoxModel (renderObj.getGenderRenderers()));
			genderRenderComboBox.setSelectedIndex (0); //prefs.getInt (appUtils.getPrefKeyString (genderRenderComboBox), 1));
	
			GraphFrame.setRenderers (renderObj.getGraphRenderers());

	
	    	// Add context menus to generation tables
	    	final ActionListPopupMenu rightClickMenu = makeRightClickPopupMenu (errorModel);
	    	final ActionListPopupMenu leftClickMenu = makeLeftClickPopupMenu (sharedSelectionModel, detailTable);
	    	
	    	for (JGeneration generation : allJGens) {
	    		generation.addMouseListener (rightClickMenu);
	    		generation.addMouseListener (leftClickMenu);
	    	}
	    	  	
	    	
	    	// Repopulate the histograms
	    	final Map<Object, MarkerFilter> markerFilters = new HashMap<Object, MarkerFilter> ();
	    	markerFilters.put (markerTable, new MarkerByMarkerFilter ());
	    	repopulateHistograms (errorModel, markerFilters);
	    		    	
	    	
	    	// Refill the marker table
	    	final MarkerFilter markerTableFilter = (MarkerFilter)markerErrorgram.getHistogramValueFilter();
	    	markerTable.setErrorModel (errorModel);
	    	markerTable.setDefaultRenderer (Integer.class, renderObj.getMarkerErrorTableRenderer());
	    	markerTable.setDefaultRenderer (SNPMarker.class, new MarkerTableStringRenderer (errorModel, markerTableFilter,
	    			expandMarkerToIndsButton.getModel()));
	    	// row sorter is set with bespoke comparator for SNPMarkers in JMarkerTable's setModel method
			resetFocusMarkerButton.setEnabled (false);
			markerTable.setMarkerFilter ((MarkerByMarkerFilter) markerFilters.get (markerTable));
			markerTable.setOverallMarkerFilter (markerFilters.get ("AllMarkerFilter"));
			//markerTable.addMouseListener (makeMaskMarkerPopupMenu (errorModel));			
		
			
	    	// Refill the individual table
			final IndividualFilter cutoffFilter2 = (IndividualFilter)individualErrorgram.getHistogramValueFilter();
			TableCellRenderer individualTableStringRenderer = new IndTableStringRenderer (errorModel, 
					filteredIndividualErrorgram.getModel(), cutoffFilter2, 
					showGenotypeInTableButton.getModel(), expandIndToMarkersButton.getModel());
			individualTable.setErrorModel (errorModel);
			individualTable.setDefaultRenderer (Integer.class, renderObj.getIndErrorTableRenderer());
			individualTable.setDefaultRenderer (Individual.class, individualTableStringRenderer);
			individualTable.setRowSorter (new TableRowSorter<TableModel> (individualTable.getModel()));
			showGenotypeInTableButton.setEnabled (false);
			individualTable.setPedigreeSelection (sharedSelectionModel);
			individualTable.addMouseListener (rightClickMenu);
			
			
			// Refill the masked individuals table
	    	maskedIndividualsTable.setErrorModel (errorModel);
			maskedIndividualsTable.setDefaultRenderer (Individual.class, individualTableStringRenderer);
	    	maskedIndividualsTable.setRowSorter (new TableRowSorter<TableModel> (maskedIndividualsTable.getModel()));
	    	maskedIndividualsTable.setPedigreeSelection (sharedSelectionModel);
	    	
	    	
	    	// Fill in the htmlPane
	    	errorModelPropSupport.addPropertyChangeListener (htmlPane);
	    	htmlPane.propertyChange (
	    		new PropertyChangeEvent (errorModel, ErrorCollator.FILTER, Boolean.TRUE, Boolean.FALSE)
	    	);
	    	
	    	
			// Make default generation sorting according to males
	    	for (JGeneration generation : allJGens) { 				         			
	    		final TableMultiComparator genRowComp = sorterObj.copyTableMultiComparator (sorterObj.getGenerationSorters(), generation);
	    		generation.sortColumns (genRowComp);
	    	}
	    	
	    	
	    	// Attach enabling of recalc toolbar buttons via a property listener to the error model
	    	final PropertyChangeListener enableListener = new PropertyChangeListener () {
	    		
	    		public void propertyChange (final PropertyChangeEvent evt) {
					if (evt.getPropertyName().equals (ErrorCollator.RECALC_NEEDED)) {
						final boolean enable = ((Boolean)evt.getNewValue()).booleanValue();
						recalculateButton.setEnabled (enable);
						restoreToLastButton.setEnabled (enable);
						saveButton.setEnabled (!enable);
						recalculateButton.setBackground (enable ? Color.orange : GraphicsUtil.NULLCOLOUR);
						recalculateButton.setForeground (enable ? Color.red : saveButton.getForeground());
						recalculateButton.setOpaque (true);
					}	
				}
	    	};	
	    	errorModelPropSupport.addPropertyChangeListener (ErrorCollator.RECALC_NEEDED, enableListener);
	    	
	    	
	    	// Restructures the JGenerations (i.e. repopulates them with families that may have changed due to null sire/dam relationships)
	    	final PropertyChangeListener restructuringListener = new PropertyChangeListener () {
	    		public void propertyChange (final PropertyChangeEvent evt) {
					if (evt.getPropertyName().equals (ErrorCollator.RESTRUCTURED)) {
						final List<JGeneration> generations = generationStack.getGenerationList();
						JGenerationUtils.getInstance().redoPedigreeFamilies (generations, errorModel.getPopCheckerContext().getPopulation());
					}	
				}
	    	};	
	    	errorModelPropSupport.addPropertyChangeListener (ErrorCollator.RESTRUCTURED, restructuringListener);
	    	   	
	    	// Fire recalc event
	    	errorModelPropSupport.firePropertyChange (ErrorCollator.RECALC_NEEDED, true, false);
	    	
	    	// Prompts a history state to get stored when errorModel emits a STORE_HISTORY valued PropertyChangeEvent
	    	errorModelPropSupport.addPropertyChangeListener (ErrorCollator.STORE_HISTORY, new HistoryListener ());
	    	// Store an initial empty history state
	    	errorModelPropSupport.firePropertyChange (ErrorCollator.STORE_HISTORY, Boolean.FALSE, Boolean.TRUE);
	    	
	    	
	    	loadGenotypeButton.setEnabled (true); // If something's been loaded, there must be an active pedigree so allow genotype loading
	    	PedigreeFrame.this.repaint();
		}
		//else {
		//	new ParsingErrorDialog().showTheseErrors (this, newGenotypeParser.getParsingErrors(), 10);
		//}
	}
	
		
	
	/**
	 * Construct a new right click menu object for individual maskings and breaking parent relationships
	 */
	public ActionListPopupMenu makeRightClickPopupMenu (final ErrorCollator errorModel) {
    	final ActionListPopupMenu rightClickMenu = new ActionListIndividualPopupMenu (MouseEvent.BUTTON3);
    	final JLabel title2 = new JLabel (getLabelString ("rightMousePopupTitle"));
    	title2.setBorder (ActionListPopupMenu.INDENT_BORDER);
    	rightClickMenu.add (title2);
    	rightClickMenu.add (new JSeparator ());
    	rightClickMenu.add (new IndividualToggleMaskAction (makeKeyStroke (KeyEvent.VK_M, KeyEvent.CTRL_MASK), "toggleMask", errorModel));
    	rightClickMenu.add (new FamilyMaskAction (makeKeyStroke (KeyEvent.VK_F, KeyEvent.CTRL_MASK), "maskFamily", errorModel));
    	rightClickMenu.add (new FamilyUnmaskAction (makeKeyStroke (KeyEvent.VK_U, KeyEvent.CTRL_MASK), "unmaskFamily", errorModel));
    	rightClickMenu.add (new IndividualToggleNullFatherAction (makeKeyStroke (KeyEvent.VK_S, KeyEvent.CTRL_MASK), "toggleNullFather", errorModel));
    	rightClickMenu.add (new IndividualToggleNullMotherAction (makeKeyStroke (KeyEvent.VK_D, KeyEvent.CTRL_MASK), "toggleNullMother", errorModel));
    	return rightClickMenu;
	}
	
	
	/**
	 * Construct a new left click menu object for selections on individuals
	 */
	public ActionListPopupMenu makeLeftClickPopupMenu (final PedigreeSelectionModel pedigreeSelection, final JGeneration detailTable) {
    	final ActionListPopupMenu leftClickMenu = new ActionListIndividualPopupMenu (MouseEvent.BUTTON1);
    	final JLabel title2 = new JLabel (getLabelString ("leftMousePopupTitle"));
    	title2.setBorder (ActionListPopupMenu.INDENT_BORDER);
    	leftClickMenu.add (title2);
    	leftClickMenu.add (new JSeparator ());
    	leftClickMenu.add (new SelectIndividualAction (makeKeyStroke (KeyEvent.VK_I, KeyEvent.CTRL_MASK), "selectIndFamily", pedigreeSelection, 1, 0, true));
    	leftClickMenu.add (new SelectIndividualAction (makeKeyStroke (KeyEvent.VK_A, KeyEvent.CTRL_MASK), "selectAncestors", pedigreeSelection, 0, Integer.MAX_VALUE, false));
    	leftClickMenu.add (new SelectIndividualAction (makeKeyStroke (KeyEvent.VK_D, KeyEvent.CTRL_MASK), "selectDescendants", pedigreeSelection, Integer.MAX_VALUE, 0, false));
    	leftClickMenu.add (new SelectFamilyAction (makeKeyStroke (KeyEvent.VK_F, KeyEvent.CTRL_MASK), "selectAllFamily", pedigreeSelection, Integer.MAX_VALUE, 1, true));
    	leftClickMenu.add (new AbstractPostSelectionAction (makeKeyStroke (KeyEvent.VK_C, KeyEvent.CTRL_MASK), "clear", pedigreeSelection) {
			public void doAction (final PedigreeSelectionSource famSelSource) {
    			this.selectionModel.clearSelection();
    			individualTable.clearSelection();
    			globalRepaint();
    		}		
    	});
    	leftClickMenu.add (new AbstractPostSelectionAction (makeKeyStroke (KeyEvent.VK_G, KeyEvent.CTRL_MASK), "nodeLinkView", pedigreeSelection) {
			public void doAction (final PedigreeSelectionSource famSelSource) {
				final GraphFrame gFrame = new GraphFrame ();
				gFrame.setModel (this.selectionModel, filteredIndividualErrorgram.getModel());
			}
    	});
    	leftClickMenu.add (new DetailViewAction (makeKeyStroke (KeyEvent.VK_Z, KeyEvent.CTRL_MASK), "detailTable", detailTable));
    	return leftClickMenu;
	}
	
	
	
	/**
	 * Construct a new right click menu object for marker masking
	 */
	public ActionListPopupMenu makeMaskMarkerPopupMenu (final ErrorCollator errorModel) {
    	final ActionListMarkerPopupMenu rightClickMenu = new ActionListMarkerPopupMenu (MouseEvent.BUTTON3) {
    		public void mousePressed (final MouseEvent mEvent) {
    			//System.err.println ("PRESSED!");
    		}
    	};
    	final JLabel title2 = new JLabel (Messages.getString ("napier.pedigree.text", "maskMarkerPopupTitle"));
    	title2.setBorder (ActionListPopupMenu.INDENT_BORDER);
    	rightClickMenu.add (title2);
    	rightClickMenu.add (new JSeparator ());
    	rightClickMenu.add (new IndividualToggleMaskAction (KeyStroke.getKeyStroke (KeyEvent.VK_M, KeyEvent.CTRL_MASK), "toggleMaskMarker", errorModel));
    	return rightClickMenu;
	}
	
	
	public KeyStroke makeKeyStroke (final int keyCode, final int modifiers) {
		return KeyStroke.getKeyStroke (keyCode, modifiers);
	}
	

	
	/**
	 * Repopulate the histograms
	 */
	public void repopulateHistograms (final ErrorCollator errorModel, final Map<Object, MarkerFilter> filterMap) {
    	
		final AbstractErrorHistogram[] histograms = getHistograms();
		
		// Remove old listeners. They cause trouble in the model resetting.
    	final Set<Class<?>> listenerTypes = new HashSet<Class<?>> ();
    	listenerTypes.addAll (Arrays.asList (IndividualMasker.class, MarkerFilteredGenotypeMasker.class, DualFilteredGenotypeMasker.class, MarkerMasker.class));
    	for (AbstractErrorHistogram histogram : histograms) {
			final ChangeListener[] cListeners = histogram.getChangeListeners();
			for (ChangeListener cListener : cListeners) {
				if (listenerTypes.contains (cListener.getClass())) {
					histogram.removeChangeListener (cListener);
				}
			}
		}
    	
    	//final int errors = errorModel.getInitialAllErrorMap().getMarkerMap().size();
		for (AbstractErrorHistogram histogram : histograms) {
			histogram.setData (errorModel);
		}
		
		individualErrorgram.setValue (individualErrorgram.getMaximum ());
		filteredIndividualErrorgram.setValue (filteredIndividualErrorgram.getMinimum());
		markerErrorgram.setValue (markerErrorgram.getMaximum());
		filteredMarkerErrorgram.setValue (filteredMarkerErrorgram.getMinimum());
		filteredMarkerErrorgram.setExtent (0);
    	
    	// Set which histograms use original data and which use filtered data
    	final boolean useFilteredData[] = {false, false, false, true};
    	for (int index = 0; index < histograms.length; index++) {
    		histograms[index].setUseFilteredData (useFilteredData [index]);
    	}
    	
    	// Reset marker filters
    	final MarkerFilter cutoffFilter = new AtomicMarkerFilters.CutoffMarkerFilter (errorModel, markerErrorgram.getModel(), false);
       	final MarkerFilter inverseCutoffFilter = new AtomicMarkerFilters.InverseCutoffMarkerFilter (errorModel, filteredMarkerErrorgram.getModel(), false);
       	final MarkerFilter compoundMarkerFilter = CompoundMarkerFilters.andFilter (
       		Arrays.asList (new MarkerFilter[] {cutoffFilter, inverseCutoffFilter, filterMap.get (markerTable)})
       	);
    	final MarkerFilter singleMarkerFilter = new AtomicMarkerFilters.SpecificMarkerFilter (errorModel.getPopCheckerContext());

    	filterMap.put ("AllMarkerFilter", compoundMarkerFilter);

    	errorModel.setFilter (singleMarkerFilter);
    	errorModel.filter ();
   	
    	// Reattach filters and listeners to histograms
    	final HistogramValueFilter[] filters = {(HistogramValueFilter)compoundMarkerFilter,
    			(HistogramValueFilter)compoundMarkerFilter,
    			//new AtomicMarkerFilters.InverseCutoffMarkerFilter (errorModel, filteredMarkerErrorgram.getModel(), true),
    			new AtomicIndividualFilters.CutoffIndividualFilter (errorModel, individualErrorgram.getModel(), true),
    			new AtomicIndividualFilters.InverseCutoffIndividualFilter (errorModel, filteredIndividualErrorgram.getModel(), true)
    	};
    	for (int index = 0; index < histograms.length; index++) {
    		histograms[index].setHistogramValueFilter (filters [index]);
    	}
    	
    	errorModel.getPropertyChangeSupport().addPropertyChangeListener (filteredIndividualErrorgram);
    	errorModel.getPropertyChangeSupport().addPropertyChangeListener (filteredMarkerErrorgram);

    	individualErrorgram.addChangeListener (new IndividualMasker (errorModel, filteredIndividualErrorgram));
		markerErrorgram.addChangeListener (new MarkerMasker (errorModel, filteredMarkerErrorgram, compoundMarkerFilter));
		filteredMarkerErrorgram.addChangeListener (new MarkerMasker (errorModel, null, compoundMarkerFilter));	
		
		//final DualFilteredGenotypeMasker genMasker = new DualFilteredGenotypeMasker (errorModel, null);
		//genMasker.putHistogramFilter (filteredMarkerErrorgram, filteredMarkerErrorgram.getPrimitiveFilter());
		//genMasker.putHistogramFilter (filteredIndividualErrorgram, filteredIndividualErrorgram.getPrimitiveFilter());
		
		//filteredMarkerErrorgram.addChangeListener (genMasker);	
		//filteredIndividualErrorgram.addChangeListener (genMasker); // andy & Trev don't want masking just colouring	
	}
	
	
	
	// Some get methods for returning useful info
	
	public GenotypeFileParser getModel () {
		return genotypeParser;
	}
	
	
	public ErrorCollator getErrorCollator () {
		return errorModel;
	}
	
	
	// Also used further up in this class
	public AbstractErrorHistogram[] getHistograms () {
		return new AbstractErrorHistogram[] {markerErrorgram, filteredMarkerErrorgram, individualErrorgram, filteredIndividualErrorgram};
	}
	
	
	public JMarkerTable getMarkerTable () {
		return markerTable;
	}
	
	
	/**
	 * Redraw everything, including stuff in separate windows
	 */
	public void globalRepaint () {
		generationStack.repaint();
		final Window[] windows = Window.getWindows();
		for (Window window : windows) {
			if (!window.getName().equals (MAIN_WINDOW)) {
				window.repaint();
			}
		}
	}
	
	
	// Utility methods for setting up components with data from property files

	public boolean isPropertyTrue (final String propertyKey) {
		return "true".equalsIgnoreCase (getLabelString (propertyKey));
	}
	
	public String getLabelString (final String propertyKey) {
		return AppUtils.getInstance().getLabelString (propertyKey);
	}
	
	public void decorateButton (final AbstractButton button, final String propertyPrefix, final boolean showText) {
		AppUtils.getInstance().decorateButton (button, propertyPrefix, showText);
	}
	
	public void decorateButton (final AbstractButton button, final String propertyPrefix) {
		decorateButton (button, propertyPrefix, true);
	}
	
	public HTMLLabel makeHTMLLabel (final String propertyPrefix, final Color textColour) {
		return makeHTMLLabel (propertyPrefix, textColour, true);
	}
	
	public HTMLLabel makeHTMLLabel (final String propertyPrefix, final Color textColour, final boolean showText) {
		return AppUtils.getInstance().makeHTMLLabel (propertyPrefix, textColour, showText);
	}
	
	public JLabel makeLabel (final String propertyPrefix, final Color textColour) {
		return makeLabel (propertyPrefix, textColour, true);
	}
	
	public JLabel makeLabel (final String propertyPrefix, final Color textColour, final boolean showText) {
		return AppUtils.getInstance().makeLabel (propertyPrefix, textColour, showText);
	}
	
	/**
	 * Add a histogram to a collapsible panel
	 * Set up a control to decide whether the histogram provokes calculations during dragevents or afterwards
	 * @param histogram
	 * @param prefix
	 * @return a JCollapsiblePanel component
	 */
	public JCollapsiblePanel makeHistogramPanel (final AbstractErrorHistogram histogram, final String prefix) {
		final JCollapsiblePanel errorHistogramPanel = new JCollapsiblePanel ();
		
		final JCheckBox continuousUpdateOption = new JCheckBox ("", histogram.isContinuousUpdate());
		continuousUpdateOption.setToolTipText (getLabelString ("continuousUpdateOptionTooltip"));
		continuousUpdateOption.addActionListener(
			new ActionListener () {
				@Override
				public void actionPerformed (final ActionEvent aEvent) {
					histogram.setContinuousUpdate (continuousUpdateOption.isSelected());	
				}	
			}
		);
		continuousUpdateOption.setAlignmentX (0.5f);
		
		final Icon continuousIcon = PedigreeIconCache.makeIcon ("continuousIcon");
		final JLabel iconLabel = new JLabel (continuousIcon);
		iconLabel.setAlignmentX (0.5f);
		
		final Box checkBoxPanel = new Box (BoxLayout.Y_AXIS);
		checkBoxPanel.setToolTipText (getLabelString ("continuousUpdateOptionTooltip"));
		checkBoxPanel.add (iconLabel);
		checkBoxPanel.add (continuousUpdateOption);
		checkBoxPanel.setBorder (BorderFactory.createMatteBorder (0, 1, 0, 0, Color.lightGray));
		
		final JPanel histoSecondLevelPanel = new JPanel (new BorderLayout ());
		histoSecondLevelPanel.add (histogram, BorderLayout.CENTER);
		histoSecondLevelPanel.add (checkBoxPanel, BorderLayout.EAST);
		
		final JLabel histoLabel = makeHTMLLabel (prefix, secondLevelLabelColour);
		errorHistogramPanel.setHeader (histoLabel);
		
		final Border histogramLabelBorder = BorderFactory.createMatteBorder (1, 0, 0, 0, 
				UIManager.getDefaults().getColor("controlShadow"));
		errorHistogramPanel.setBorder (histogramLabelBorder);
		errorHistogramPanel.setContents (histoSecondLevelPanel);
		
		return errorHistogramPanel;
	}
	

	
	protected void addSandwichOptions (final JToolBar sandwichToolBar) {
		sandwichToolBar.setName (getLabelString ("sandwichBarName"));
		
		//optionContainer.setAlignmentX (0.0f);
		final JLabel sortLabel = makeLabel ("sortOptions", Color.darkGray, true);
		sortLabel.setBorder (EMPTY_BORDER);
		sortLabel.setIcon (null);
		sandwichToolBar.add (sortLabel);
		
		sandwichToolBar.add (offspringSortButton);
		sandwichToolBar.add (generationSortButton);
		sandwichToolBar.add (Box.createHorizontalStrut(5));

		//final JLabel displayLabel = makeLabel ("renderOptions", Color.darkGray, true);
		//displayLabel.setBorder (EMPTY_BORDER);
		//displayLabel.setIcon (null);
		//sandwichToolBar.add (displayLabel);
		
		final Map<String, JComponent> labelMap = new LinkedHashMap <String, JComponent> ();
		labelMap.put ("families", familyRenderComboBox);
		labelMap.put ("offspringSplit", offspringSplitComboBox);
		final Set<String> showText = new HashSet <String> ();

		showText.add ("offspringSplit");
		showText.add ("families");
		addComponents (labelMap, showText, sandwichToolBar);
		
		sandwichToolBar.add (Box.createGlue());
	}
	
	
	
	protected Container[] makeOptionComponents () {
		
		final JToolBar colourBar = new JToolBar ();
		colourBar.setName (getLabelString ("colourBarName"));
		final JLabel colourLabel = makeLabel ("colourOptions", Color.darkGray, true);
		colourLabel.setBorder (EMPTY_BORDER);
		colourLabel.setIcon (null);
		colourBar.add (colourLabel);
		
		final Map<String, JComponent> labelMap = new LinkedHashMap <String, JComponent> ();	
		labelMap.put ("errorColourSchemeB", errorColourComboBox);
		labelMap.put ("incompleteColourSchemeB", incompleteColourComboBox);
		labelMap.put ("selectionColourScheme", selectionColourComboBox);
		
		final Set<String> showText = new HashSet <String> ();
		showText.add ("errorColourSchemeB");
		showText.add ("incompleteColourSchemeB");
		
		addComponents (labelMap, showText, colourBar);
		
		return new Container[] {colourBar};
	}
	
	
	
	protected void addComponents (final Map<String, JComponent> labelMap, final Set<String> showText, final Container optionContainer) {
		final Iterator<Entry<String, JComponent>> entries = labelMap.entrySet().iterator();
		while (entries.hasNext ()) {
			final Entry<String, JComponent> entry = entries.next ();
			final JLabel label = makeHTMLLabel (entry.getKey(), secondLevelLabelColour, showText.contains (entry.getKey()));
			label.setBorder (EMPTY_BORDER);
			optionContainer.add (label);
			optionContainer.add (entry.getValue());
			optionContainer.add (Box.createRigidArea (new Dimension (10, 10)));
			entry.getValue().setToolTipText (label.getToolTipText());
		}
	}
	
	
	/**
	 * Get minimum height of a TableCellRenderer if it's a JComponent
	 * Return -1 otherwise;
	 * @param klass
	 * @return
	 */
	protected int getRendererMinHeight (final Class<?> klass) {
		final TableCellRenderer tcr = generationStack.getSharedRenderer (klass);
		int minRowHeight = -1;
		
		if (tcr instanceof JComponent) {
			final JComponent renderer = (JComponent)tcr;
			minRowHeight = renderer.getMinimumSize().height;
		}
		
		return minRowHeight;
	}
	
	
	
	/**
	 * Basically adjusts the sire / dam row heights to accommodate genotype data
	 * @param marker
	 */
	protected void adjustRowHeights (final int previousMinHeight) {
		
		// Make a list of all JGeneration components, including the detail view 
    	final List<JGeneration> allJGens = new ArrayList<JGeneration> (generationStack.getGenerationList());
    	allJGens.add (detailFrame.getDetailTable());
		   	
		final int rows[] = {ModelRowConstants.SIRE, ModelRowConstants.DAM};
		final int minRowHeight = getRendererMinHeight (HeritableIndividual.class);
			
		if (minRowHeight > 0) {
			for (JGeneration jgen : allJGens) {
				//System.err.println ("b: "+jgen.getPreferredSize());
				for (int index = 0; index < rows.length; index++) {

					final int vprow = jgen.convertRowIndexToView (rows[index]);
					final int curRowHeight = jgen.getRowHeight (vprow);
					
	    			if (curRowHeight < minRowHeight) {
	    				jgen.setRowHeight (vprow, minRowHeight);
	    			}
	    			else if (curRowHeight == previousMinHeight && 
	    					(getErrorCollator().getPopCheckerContext().getFocusMarker() == null)) {
	    				jgen.setRowHeight (vprow, minRowHeight);
	    			}
				}
				//System.err.println ("a: "+jgen.getPreferredSize());
				//jgen.setSize (jgen.getWidth(), jgen.getPreferredSize().height);
			}
			generationStack.revalidate();
		}
	}
	
	
	
	
	class GenerationListSelectionListener implements ListSelectionListener {

		SorterHolder sortObj;
		
		@Override
		public void valueChanged (final ListSelectionEvent lsEvent) {
			final JList source = (JList) lsEvent.getSource ();
			if (lsEvent.getFirstIndex() == source.getModel().getSize() && lsEvent.getLastIndex() == source.getModel().getSize()
                      && !lsEvent.getValueIsAdjusting()) {
				
				// Make a list of all JGeneration components, including the detail view 
		    	final Collection<JGeneration> allJGens = new ArrayList<JGeneration> (generationStack.getGenerationList());
		    	allJGens.add (detailFrame.getDetailTable());
		    	
		    	// Update the sorting for these components
            	for (JGeneration generation : allJGens) {	

            		final TableMultiComparator genRowComp = sortObj.copyTableMultiComparator (sortObj.getGenerationSorters(), generation);
            		generation.sortColumns (genRowComp);
            		//generation.getPreferredSize();
            	}
            	
            	SwingUtilities.invokeLater (
            		new Runnable () {

						@Override
						public void run() {
							for (JGeneration generation : allJGens) {	
			            		generation.getPreferredSize();
			            	}
							generationStack.revalidate();
						}
            			
            		}
            	);	
            }
		}
		
		public void setSorterHolder (final SorterHolder newSortObj) {
			sortObj = newSortObj;
		}
	}
	
	
	/**
	 * Class that attaches itself to an ErrorCollator so
	 * when a recalculation of the HeritablePopulation occurs
	 * everything is repainted at the finish
	 * @author cs22
	 */
	class RepaintListener implements PropertyChangeListener {
		@Override
		public void propertyChange (final PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals (ErrorCollator.REPAINT_NEEDED)) {
				globalRepaint ();
			}
		}
	}
	

	
	class HistoryListener implements PropertyChangeListener {
		@Override
		public void propertyChange (final PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals (ErrorCollator.STORE_HISTORY)) {
				hModel.addHistoryState (PedigreeFrame.this);
			}
		}
	}

	
	/**
	 * Extended HTMLScrollPane that updates in response to PropertyEvents
	 * @author cs22
	 */
	class PropertyAwareHTMLScrollPane extends HTMLScrollPane {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1148207944744438597L;

		String[] defaults = {"-", "-", "N/A", "N/A", "-", "-"};
		
		public void propertyChange (final PropertyChangeEvent evt) {
			super.propertyChange (evt);
			if ("filter".equals (evt.getPropertyName()) && evt.getSource() instanceof ErrorCollator) {
				String[] initialArguments = null;
				
				if (genotypeParser == null) {
					initialArguments = getDefaultStrings();
				} else {
					final PopCheckerWrapper popContext = errorModel.getPopCheckerContext();
					final SNPMarker focusMarker = popContext.getFocusMarker();
					final String activeMarkers = (focusMarker == null
						? Integer.toString (popContext.getPopulation().getActiveMarkers().size())
						: "<span style=\"background-color:yellow\">"+focusMarker.getName()+"</span>");
						//: "<span style=\"background-color:"+ColorUtilities.toHTMLHexString(AbstractErrorRenderer.getSelectedColour())+"\">"+focusMarker.getName()+"</span>");
					initialArguments = new String[] {
						Integer.toString (genotypeParser.getMarkers().size()),
						Integer.toString (genotypeParser.getGenotypedPopulation().getIndividuals().size()),
						genotypeParser.getGenotypeFile().getName(),
						genotypeParser.getPedigreeFile().getName(),
						activeMarkers,
						Integer.toString (((ErrorCollator)evt.getSource()).getCurrentAllErrorTotal())
					};
				}
				htmlPane.format (initialArguments);
			}
		}
		
		public String[] getDefaultStrings () {
			return defaults;
		}
	}
	
	
	
	/**
	 * Class that responds to choices in the offspring split combo box.
	 * It uses the chosen Categoriser object to prompt the current PedigreeGenerationModels
	 * in each of the JGeneration components to add rows to the end of the table model.
	 * If the choice is the identity categoriser, these rows are hidden and the standard
	 * offspring row shown, and vice versa, using one of two RowFilter instances.
	 * @author cs22
	 */
	class SplitRowListener implements ActionListener {
		
		@Override
		public void actionPerformed (final ActionEvent aEvent) {
			final JComboBox comboBox2 = (JComboBox)aEvent.getSource();
			final Categoriser<HeritableIndividual> newCategoriser = (Categoriser<HeritableIndividual>)comboBox2.getSelectedItem();
			
			if (generationStack != null) {
				// Make a list of all JGeneration components, including the detail view 
		    	final List<JGeneration> allJGens = new ArrayList<JGeneration> (generationStack.getGenerationList());
		    	allJGens.add (detailFrame.getDetailTable());
		    	final RowFilter activeRowFilter = ((comboBox2.getSelectedIndex() > 0) ? 
		    			OffspringFilters.SHOW_MULTIPLE_OFFSPRING_ROWS : OffspringFilters.SHOW_SINGLE_OFFSPRING_ROW);
		    	
		    	for (JGeneration generation : allJGens) {
		    		if (comboBox2.getSelectedIndex() > 0) {
		    			final TableModel tModel = generation.getModel();
			    		if (tModel instanceof PedigreeGenerationModel) {
			    			final PedigreeGenerationModel pedGenModel = (PedigreeGenerationModel)tModel;
			    			pedGenModel.setCategoriser (newCategoriser);
			    		}
		    		}
		    	}	
		    	
		    	for (JGeneration generation : allJGens) {
		    		generation.filter (activeRowFilter);
		    	}
		    	
		    	for (JGeneration generation : allJGens) {
		    		generation.redoRowHeights ();
		    	}
			}
		}
	}
	
	
	
	class ClosingTasks extends WindowAdapter {
	    
		/**
	     * Invoked when a window is in the process of being closed.
	     * The close operation can be overridden at this point.
	     */	
		@Override
	    public void windowClosing (final WindowEvent winEvent) {
			LOGGER.debug ("windowclosing saving prefs");
			final AppUtils appUtils = AppUtils.getInstance();

			if (errorModel != null) {
				prefs.putInt (appUtils.getPrefKeyString (familyRenderComboBox), familyRenderComboBox.getSelectedIndex());
				prefs.putInt (appUtils.getPrefKeyString (genderRenderComboBox), genderRenderComboBox.getSelectedIndex());
				prefs.putInt (appUtils.getPrefKeyString (offspringSplitComboBox), offspringSplitComboBox.getSelectedIndex());
				prefs.putInt (appUtils.getPrefKeyString (errorgramRenderComboBox), errorgramRenderComboBox.getSelectedIndex());
			}
			prefs.putInt (appUtils.getPrefKeyString (errorColourComboBox), errorColourComboBox.getSelectedIndex());
			prefs.putInt (appUtils.getPrefKeyString (incompleteColourComboBox), incompleteColourComboBox.getSelectedIndex());
			prefs.putInt (appUtils.getPrefKeyString (selectionColourComboBox), selectionColourComboBox.getSelectedIndex());
			prefs.putDouble (appUtils.getPrefKeyString (histToSandwichSplit), appUtils.getDividerLocationAsRatio (histToSandwichSplit));
			prefs.putDouble (appUtils.getPrefKeyString (sandwichToTablesSplit), appUtils.getDividerLocationAsRatio (sandwichToTablesSplit));
			prefs.putInt ("Width", PedigreeFrame.this.getWidth());
			prefs.putInt ("Height", PedigreeFrame.this.getHeight());
			
			try {
				prefs.flush();
			} catch (BackingStoreException bse) {
				LOGGER.error (ErrorStrings.getInstance().getString ("preferencesSavingError"));
			}
			
			detailFrame.dispose();
			
			final Window[] windows = Window.getWindows();
			for (Window window : windows) {
				if (window instanceof ModelScopeWindow) {
					final WindowEvent windowClosing = new WindowEvent (window, WindowEvent.WINDOW_CLOSING);
					Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent (windowClosing);
					LOGGER.debug ("Window: "+window);
				}
			}
	    }
	}
}