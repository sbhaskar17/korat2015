package korat;

import java.util.Arrays;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import korat.config.ConfigLoader;
import korat.config.ConfigManager;
import korat.testing.impl.CannotFindClassUnderTest;
import korat.testing.impl.CannotFindFinitizationException;
import korat.testing.impl.CannotFindPredicateException;
import korat.testing.impl.CannotInvokeFinitizationException;
import korat.testing.impl.CannotInvokePredicateException;
import korat.testing.impl.KoratTestException;
import korat.testing.impl.TestCradle;
import korat.utils.io.GraphPaths;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * Korat Main Class
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 */
public class Korat extends JFrame {

	/**
	 * Loader of Korat Application
	 * 
	 * @param args -
	 *            program arguments are listed below. <p/>
	 * 
	 * Arguments: <table cellspacing="3" cellpadding="3">
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--args &lt;arg-list&gt;</code></td>
	 * <td>mandatory</td>
	 * <td>comma separated list of finitization parameters, ordered as in
	 * corresponding finitization method.</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--class &lt;fullClassName&gt;</code></td>
	 * <td>mandatory</td>
	 * <td>name of test case class</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--config &lt;fileName&gt;</code></td>
	 * <td>optional</td>
	 * <td>name of the config file to be used</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--cvDelta</code></td>
	 * <td>optional</td>
	 * <td>use delta file format when storing candidate vectors to disk</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--cvEnd &lt;num&gt;</code></td>
	 * <td>optional</td>
	 * <td>set the end candidate vector to &lt;num&gt;-th vector from cvFile</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--cvExpected &lt;num&gt;</code></td>
	 * <td>optional</td>
	 * <td>expected number of total explored vectors</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--cvFile &lt;filename&gt;</code></td>
	 * <td>optional</td>
	 * <td>name of the candidate-vectors file</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--cvFullFormatRatio &lt;num&gt;</code></td>
	 * <td>optional</td>
	 * <td>the ratio of full format vectors (if delta file format is used)</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--cvStart &lt;num&gt;</code></td>
	 * <td>optional</td>
	 * <td>set the start candidate vector to &lt;num&gt;-th vector from cvFile</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--cvWrite</code></td>
	 * <td>optional</td>
	 * <td>write all explored candidate vectors to file</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--cvWriteNum &lt;num&gt;</code></td>
	 * <td>optional</td>
	 * <td>write only &lt;num&gt; equi-distant vectors to disk</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--excludePackages &lt;packages&gt;</code></td>
	 * <td>optional</td>
	 * <td>comma separated list of packages to be excluded from instrumentation</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--finitization &lt;finMethodName&gt;</code></td>
	 * <td>optional</td>
	 * <td>set the name of finitization method. If ommited, default name
	 * fin&lt;ClassName&gt; is used.</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--help</code></td>
	 * <td>optional</td>
	 * <td>print help message.</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--listeners &lt;listenerClasses&gt;</code></td>
	 * <td>optional</td>
	 * <td>comma separated list of full class names that implement
	 * <code>ITestCaseListener</code> interface.</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--maxStructs</code> &lt;num&gt;</td>
	 * <td>optional</td>
	 * <td>stop execution after finding &lt;num&gt; invariant-passing
	 * structures</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--predicate &lt;predMethodName&gt;</code></td>
	 * <td>optional</td>
	 * <td>set the name of predicate method. If ommited, default name "repOK"
	 * will be used</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--print</code></td>
	 * <td>optional</td>
	 * <td>print the generated structure to the console</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--printCandVects</code></td>
	 * <td>optional</td>
	 * <td>print candidate vector and accessed field list during the search.</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--progress &lt;threshold&gt;</code></td>
	 * <td>optional</td>
	 * <td>print status of the search after exploration of &lt;threshold&gt;
	 * candidates </td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--serialize &lt;filename&gt;</code></td>
	 * <td>optional</td>
	 * <td>seralize the invariant-passing test cases to the specified file. If
	 * filename contains absolute path, use quotes. </td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--visualize</code> </td>
	 * <td>optional</td>
	 * <td>visualize the generated data structures</td>
	 * </tr>
	 * 
	 * </table>
	 * 
	 * <i>Example command line :: </i> <br/> java korat.Korat --class
	 * korat.examples.binarytree.BinaryTree --args 3,3,3
	 * 
	 */

	private static final long serialVersionUID = 1L;
	    
    public static double FSM_RESET_PROBABILITY= 0.0;
	public static boolean JSONLoaded = false;
	public static String JSONLoadedDir;
	public static String JSON_DIR= "viz_json";

	runPanel runpanel;
	graphPanel graphpanel;
	textPanel textpanel;
	consolePanel consolepanel;
	helpPanel helppanel;
	
	public GraphPaths gp;

	
	public Korat() {	
		this.setTitle("Korat 2015"); // PAR
		this.setSize(800, 600);

		// Creates menu bar for JFrame
		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);
		this.setLayout(new FlowLayout());

		// Add drop down menu to the menu bar
		JMenu filemenu = new JMenu("File");
		JMenu runmenu = new JMenu("Run");
		JMenu outputmenu = new JMenu("Ouput");
		JMenu helpmenu = new JMenu("Help");
		menuBar.add(filemenu);
		menuBar.add(runmenu);
		menuBar.add(outputmenu);
		menuBar.add(helpmenu);

		// Add menu items to drop down menu
		JMenuItem loaditem = new JMenuItem("Load File");
		JMenuItem saveitem = new JMenuItem("Save File");
		JMenuItem exititem = new JMenuItem("Exit");
		filemenu.add(loaditem);
		filemenu.add(saveitem);
		filemenu.add(exititem);  

		JMenuItem graphitem = new JMenuItem("Graph");
		JMenuItem textitem = new JMenuItem("Text");
		JMenuItem consoleitem = new JMenuItem("Console");
		outputmenu.add(graphitem);
		outputmenu.add(textitem);
		outputmenu.add(consoleitem);

		JMenuItem runitem = new JMenuItem("Run");	
		runmenu.add(runitem);

		JMenuItem helpitem = new JMenuItem("Help");
		helpmenu.add(helpitem);

		runpanel = new runPanel();
		graphpanel = new graphPanel();
		textpanel = new textPanel();
		consolepanel = new consolePanel();
		helppanel = new helpPanel();

		this.add(runpanel);
		this.add(graphpanel);
		this.add(textpanel);
		this.add(consolepanel);		
		this.add(helppanel);

		runpanel.setVisible(false);
		graphpanel.setVisible(false);
		textpanel.setVisible(false);
		consolepanel.setVisible(false);
		helppanel.setVisible(false);
		this.setVisible(true);

		// Add listeners
		ActionListener loadactionlistener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {	 
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.setDialogTitle("Choose folder to Load Korat JSON from.");
				fc.setAcceptAllFileFilterUsed(false);
				
			    if(fc.showOpenDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
		        	String dir = (fc.getSelectedFile().getAbsolutePath()+"/").replace("\\", "/");
			    	//System.out.println("ARPTWO: "+fc.getSelectedFile());
		        	
					JSONLoaded = true;
					JSONLoadedDir = dir;
					gp.freadPathsJson();
			    }				
				
			}
		}; 

		ActionListener saveactionlistener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.setDialogTitle("Choose folder to Save Korat JSON to.");
				fc.setAcceptAllFileFilterUsed(false);

			    if(fc.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
		        	String dir = (fc.getSelectedFile().getAbsolutePath()+"/").replace("\\", "/");
			    	//System.out.println("ARPTWO: "+JSON_HOME+":"+JSON_FILEPATH);
					gp.fsavePathsJson(dir);
			    }
			}
		}; 

			
		ActionListener exitactionlistener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		}; 

		ActionListener runactionlistener= new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				runpanel.setVisible(true);
				graphpanel.setVisible(false);
				textpanel.setVisible(false);
				consolepanel.setVisible(false);
				helppanel.setVisible(false);
			}
		};

		ActionListener graphactionlistener= new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				runpanel.setVisible(false);
				graphpanel.setVisible(true);
				textpanel.setVisible(false);
				consolepanel.setVisible(false);
				helppanel.setVisible(false);
				
				graphpanel.jbinfo.setText("Loading ...");
				graphpanel.paintGraph(-1); // plain graph
				graphpanel.jbinfo.setText("Path# " + "-");
				graphpanel.pidx=-1;
				graphpanel.jpgraphinfo.setText(graphpanel.graphInfo(graphpanel.pidx));
			}
		};

		ActionListener textactionlistener= new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				runpanel.setVisible(false);
				graphpanel.setVisible(false);
				textpanel.setVisible(true);
				consolepanel.setVisible(false);
				helppanel.setVisible(false);
			}
		};

		ActionListener consoleactionlistener= new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				runpanel.setVisible(false);
				graphpanel.setVisible(false);
				textpanel.setVisible(false);
				consolepanel.setVisible(true);
				helppanel.setVisible(false);
			}
		};

		ActionListener helpactionlistener= new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				runpanel.setVisible(false);
				graphpanel.setVisible(false);
				textpanel.setVisible(false);
				consolepanel.setVisible(false);
				helppanel.setVisible(true);
			}
		};
		
		exititem.addActionListener(exitactionlistener);  
		loaditem.addActionListener(loadactionlistener);  
		saveitem.addActionListener(saveactionlistener);  
		runitem.addActionListener(runactionlistener);
		graphitem.addActionListener(graphactionlistener);
		textitem.addActionListener(textactionlistener);
		consoleitem.addActionListener(consoleactionlistener);    
		helpitem.addActionListener(helpactionlistener);  
		
		gp = new GraphPaths();
	}
	
		

	public static void main(String[] args) {
		StringBuffer sb = new StringBuffer();

		for (String s: args) {
			sb.append(s);
			sb.append(" ");
		}

		if (sb.toString().contains("--gui")) {
			Korat js = new Korat();
		    js.setExtendedState(js.getExtendedState() | JFrame.MAXIMIZED_BOTH);
			js.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
		else {
			(new runKorat(args)).runnow();
		}
	}
}

class helpPanel extends JPanel  {
	private static final long serialVersionUID = 1L;
	JTextArea jta;
	String msg;
	
	public helpPanel() {
		this.setLayout(new GridBagLayout());
		this.setBorder(new TitledBorder(new LineBorder(Color.black, 2), "Help"));
		
        msg = new String("Korat is a tool for constraint-based generation of structurally complex test inputs for Java programs. \n" + 
        				"More information can be found at the website: http://korat.sourceforge.net/index.html \n" + 
        				"\n" +
        				"File: \n" +
        				"Load File - Load Graphs from Korat Json format file.\n" +
        				"Save File - Save Graphs as Korat Json format file.\n" +
        				"Exit - Quit application.\n" +
        				"\n" +
        				"Run:\n" +
        				"Execute Korat model.\n" +
        				"\n" +
        				"Output:\n" +
        				"Graph - View Korat Graph output.\n" +
        				"Text - View Korat text output.\n" +
        				"Console - View Korat error output.\n" +
        				"\n" +
        				"Help:\n" +	
        				"This information.\n" +
        				"\n" +
        				"@2015 Enhancements by Kilnagar Bhaskar, Univ. of Texas at Austin."
        		);
        	
        jta = new JTextArea(msg);
        jta.setEditable(false);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(10,10,10,10);  // padding
		this.add(jta, c);
	}
	
}

class runPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	JCheckBoxMenuItem cbprint, cbviz;
	JButton jbrun, jbclear;
	JCheckBox[] checkboxes;
	JTextField[] textfields;
	JLabel[] texfieldlabels;
	runPanel rp;

	GridBagConstraints c;
	JCheckBox jcbfsm;
	JTextField jtffsm;
	JLabel jlfsm;

	String[] soutputs = {"--print", "--printCandVects", "--cvDelta", "--cvWrite", "--visualize"};
	String[] coutputs = {"--args", "--class", "--config", "--cvEnd", "--cvExpected",
			"--cvFile", "--cvFullFormatRatio", "--cvStart", "--cvWriteNum",
			"--excludePackages", "--finitization", "--listeners",
			"--maxStructs", "--predicate", "--progress", "--serialize"};
	String[] cparams = {"<arg-list>", "<fullClassName>", "<fileName>", "<num>", "<num>",
			"<filename>", "<num>", "<num>", "<num>",
			"<packages>", "<finMethodName>", "<listenerClasses>",
			"<num>", "<predMethodName>", "<threshold>", "<filename>"};

	//	String[] coutputs = {"--args <arg-list>", "--class <fullClassName>", "--config <fileName>", "--cvEnd <num>", "--cvExpected <num>",
	//			"--cvFile <filename>", "--cvFullFormatRatio <num>", "--cvStart <num>", "--cvWriteNum <num>",
	//			"--excludePackages <packages>", "--finitization <finMethodName>", "--listeners <listenerClasses>",
	//			"--maxStructs <num>", "--predicate <predMethodName>", "--progress <threshold>", "--serialize <filename>"};

	/*
	--args <arg-list>
	--class <fullClassName>
	--config <fileName>
	--cvDelta
	--cvEnd <num>
	--cvExpected <num>
	--cvFile <filename>
	--cvFullFormatRatio <num>
	--cvStart <num>
	--cvWrite
	--cvWriteNum <num>
	--excludePackages <packages>
	--finitization <finMethodName>
	--help
	--listeners <listenerClasses>
	--maxStructs <num>
	--predicate <predMethodName>
	--print
	--printCandVects
	--progress <threshold>
	--serialize <filename>
	--visualize
	 */

	public runPanel() {	
		rp=this;
		
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));


		JPanel pclassic = new JPanel();
		pclassic.setBorder(new TitledBorder(new LineBorder(Color.black, 2), "Run"));

		pclassic.setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;

		checkboxes = new JCheckBox[soutputs.length];
		for(int i = 0; i < soutputs.length; i++) {
			checkboxes[i] = new JCheckBox(soutputs[i]);
			pclassic.add(checkboxes[i], c);
			c.gridy ++;
		}

		c.gridx ++;
		c.gridy = 0;
		textfields = new JTextField[coutputs.length];
		texfieldlabels = new JLabel[coutputs.length];
		for(int i = 0; i < coutputs.length; i++) {
			textfields[i] = new JTextField(10);
			textfields[i].setText(cparams[i]);
			texfieldlabels[i] = new JLabel(coutputs[i]);
			texfieldlabels[i].setLabelFor(textfields[i]);
			pclassic.add(texfieldlabels[i], c);		
			c.gridx++;
			pclassic.add(textfields[i], c);
			c.gridx--;
			c.gridy++;

		}

		ActionListener jbrunlistener= new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				Korat.FSM_RESET_PROBABILITY= jcbfsm.isEnabled() ? Double.parseDouble(jtffsm.getText())/100.0 : 0.0;
				new GraphPaths().fInit(); 
				String runcmd = rp.getSelection() + " --gui";
				//System.out.println (runcmd);			
				(new runKorat(runcmd.split(" "))).runnow();
				//				System.out.println (rp.getSelection());			
				//				(new runKorat(rp.getSelection().split(" "))).runnow();
			}
		};

		ActionListener jbclearlistener= new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				clearAll();
			}
		};

		
		c.gridx += 2;
		c.gridy = 0;
		c.insets = new Insets(0,20,0,0);  // padding
		jbrun = new JButton ("Run");
		pclassic.add(jbrun, c);
		c.gridy += 2;
		jbrun.addActionListener(jbrunlistener);
		jbclear = new JButton ("Clear");
		pclassic.add(jbclear, c);
		jbclear.addActionListener(jbclearlistener);
		this.add(pclassic,c);

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 3;
        c.gridx = 0;
        c.gridy = 17;
        c.insets = new Insets(0, 10, 0, 0);
        c.weightx = 1;
		JPanel pfsm = new JPanel();
		pfsm.setBorder(new TitledBorder(new LineBorder(Color.black, 2), "FSM"));
		pfsm.setMinimumSize(new Dimension(40,100));
		jcbfsm = new JCheckBox ("Random reset");
		jtffsm = new JTextField (5);
		jtffsm.setText("0.0");
		pfsm.add (jcbfsm);
		jlfsm = new JLabel("Probability(%):");
		pfsm.add(jlfsm);
		pfsm.add (jtffsm);
		jtffsm.setVisible(false);
		jlfsm.setVisible(false);
		this.add(pfsm,c);
		
		ItemListener jcbfsmlistener= new ItemListener() {	
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					jtffsm.setVisible(true);
					jlfsm.setVisible(true);
				} else {
					jtffsm.setVisible(false);
					jlfsm.setVisible(false);
				}
			}	
		};

		jcbfsm.addItemListener(jcbfsmlistener);

		InputVerifier jtffsmverifier= new InputVerifier() {
		    @Override
		    public boolean verify(JComponent input) {
		        String text = ((JTextField) input).getText();
		        Double value = new Double(text);
		        if (value < 0.0 || value > 100.0)
		        	return false;
		        else
		        	return true;
		        }
		};
		
		jtffsm.setInputVerifier(jtffsmverifier);

	}


	public void clearAll () {
		for(int i = 0; i < soutputs.length; i++) {
			checkboxes[i].setSelected(false);
		}   	
		for(int i = 0; i < coutputs.length; i++) {
			textfields[i].setText(cparams[i]);
		}  
	}


	public String getSelection() {
		StringBuilder sb = new StringBuilder(1);
		for(int i = 0; i < soutputs.length; i++) {
			if(checkboxes[i].isSelected())
				sb.append(" "+checkboxes[i].getText());
		}

		for(int i = 0; i < coutputs.length; i++) {
			if(!textfields[i].getText().contains("<")) {
				sb.append(" "+texfieldlabels[i].getText());
				sb.append(" "+textfields[i].getText());
			}
		}

		return sb.toString();
	}
}

class runKorat {
	String[] args;

	runKorat (String[] a) {
		this.args = a;
	}

	public void runnow () {
		TestCradle testCradle = TestCradle.getInstance();
		ConfigManager config = ConfigManager.getInstance();
		config.parseCmdLine(args);

		korat.Korat.JSONLoaded = false;
		korat.Korat.JSONLoadedDir = korat.Korat.JSON_DIR;

		System.out.print("\nStart of Korat Execution for " + config.className
				+ " (" + config.predicate + ", ");
		System.out.println(Arrays.toString(config.args) + ")\n");

		try {

			long t1 = System.currentTimeMillis();
			testCradle.start(config.className, config.args);
			long t2 = System.currentTimeMillis();

			long dt1 = t2 - t1;
			System.out.println("\nEnd of Korat Execution");
			System.out.println("Overall time: " + dt1 / 1000.0 + " s.");
			config.clear();

		} catch (CannotFindClassUnderTest e) {

			System.err.println("!!! Korat cannot find class under test:");
			System.err.println("        <class_name> = " + e.getFullClassName());
			System.err.println("    Use -"
					+ ConfigLoader.CLZ.getSwitches()
					+ " switch to provide full class name of the class under test.");

		} catch (CannotFindFinitizationException e) {

			System.err.println("!!! Korat cannot find finitization method for the class under test:");
			System.err.println("        <class> = " + e.getCls().getName());
			System.err.println("        <finitization> = " + e.getMethodName());
			System.err.println("    Use -"
					+ ConfigLoader.FINITIZATION.getSwitches()
					+ " switch to provide custom finitization method name.");

		} catch (CannotFindPredicateException e) {

			System.err.println("!!! Korat cannot find predicate method for the class under test:");
			System.err.println("        <class> = " + e.getCls().getName());
			System.err.println("        <predicate> = " + e.getMethodName());
			System.err.println("    Use -"
					+ ConfigLoader.PREDICATE.getSwitches()
					+ " switch to provide custom predicate method name.");

		} catch (CannotInvokeFinitizationException e) {

			System.err.println("!!! Korat cannot invoke finitization method:");
			System.err.println("        <class> = " + e.getCls().getName());
			System.err.println("        <finitization> = " + e.getMethodName());
			System.err.println();
			System.err.println("    Stack trace:");
			e.printStackTrace(System.err);

		} catch (CannotInvokePredicateException e) {

			System.err.println("!!! Korat cannot invoke predicate method:");
			System.err.println("      <class> = " + e.getCls().getName());
			System.err.println("      <predicate> = " + e.getMethodName());
			System.err.println();
			System.err.println("    Stack trace:");
			e.printStackTrace(System.err);

		} catch (KoratTestException e) {

			System.err.println("!!! A korat exception occured:");
			System.err.println();
			System.err.println("    Stack trace:");
			e.printStackTrace(System.err);

		}

	}

}


class graphPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	public static int ANIMATION_SPEED = 1000*2;
	public int pidx = 0;
	
	GridBagConstraints c;
	Container container;
	public JLabel jbinfo;
	JButton jbprev, jbnext;
	VisualizationViewer<Object, Object> vv;
	FRLayout<Object, Object> layout;
	GraphZoomScrollPane jsp;
	DirectedSparseMultigraph<Object, Object> Graph;
	GraphPaths GraphPaths;
	public JTextArea jpgraphinfo;

	public graphPanel() {
		JCheckBox jcbanimate;

		this.setLayout(new GridBagLayout());
		this.setBorder(new TitledBorder(new LineBorder(Color.black, 2), "Graph"));


		JPanel jauto = new JPanel();
		jauto.setBorder(new LineBorder(Color.black, 2));

		jcbanimate = new JCheckBox ("Animate");
		jauto.add(jcbanimate);
		
		JPanel jpnav = new JPanel();
		jpnav.setBorder(new LineBorder(Color.black, 2));

		c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0,10,0,10);  // padding
		
		jbprev = new JButton ("Previous");
		jbnext = new JButton ("Next");
		jpnav.add(jbprev, c);
		jpnav.add(new JLabel (" "));

		jpnav.add(jbnext, c);

		JPanel jpinfo = new JPanel();
	    jpinfo.setBorder(new BevelBorder(BevelBorder.LOWERED));

		jbinfo = new JLabel ("Path# " + "-");
		jpinfo.add(jbinfo);

		c.gridx = 0;
		c.gridy = 0;
		this.add(jauto, c);
		
		c.gridx = 1;
		c.gridy = 0;
		this.add (jpnav, c);

		c.gridx = 2;
		c.gridy = 0;
		this.add(jpinfo, c);

		jbprev.addActionListener(jbprevlistener);
		jbnext.addActionListener(jbnextlistener);

		jcbanimate.addItemListener(jcbanimatelistener);

		GraphPaths = new GraphPaths();
		Graph = GraphPaths.emptyGraph();
		
		jpgraphinfo = new JTextArea (3, 1);
		jpgraphinfo.setBorder(new LineBorder(Color.black, 1));
		jpgraphinfo.setEditable(false);
		jpgraphinfo.setFont(new Font("Arial", Font.PLAIN, 12));
		jpgraphinfo.setLineWrap(true);
		jpgraphinfo.setWrapStyleWord(true);
		jpgraphinfo.setText(" ");
		
		c.insets = new Insets(5,10,5,10);  // padding
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 1;
		this.add(jpgraphinfo,c);

		layout = new FRLayout<Object, Object>(Graph);		
		layout.setSize(new Dimension(550, 450));
		vv = new VisualizationViewer<Object, Object>(layout);
        vv.setBackground(Color.white);

		jsp = new GraphZoomScrollPane(vv);
		jsp.setBorder(new LineBorder(Color.black, 1));
		jsp.setPreferredSize(new Dimension (600, 500));
		
		c.insets = new Insets(5,10,5,10);  // padding
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 1;
		c.weighty = 1;
		this.add(jsp,c);
		
	}

	// idx = -1 indicates a plain graph
	public void paintGraph(int idx) {		
		Graph = GraphPaths.drawGraph(vv, idx);
		layout.setGraph(Graph);
		vv.setGraphLayout(layout);
		vv.repaint();

	}
	
	public String graphInfo (int idx) {
		return (GraphPaths.getGraphInfo(vv, idx)); 
	}
		
	ItemListener jcbanimatelistener= new ItemListener() {		
		ActionListener timerlistener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (pidx<0 || pidx>=GraphPaths.getMaxIdx()-1) 
					pidx=0;
				else
					pidx+=1;

				jbinfo.setText("Path# " + Integer.toString(pidx+1));     
				paintGraph(pidx);
				jpgraphinfo.setText(graphInfo(pidx));
				timer.restart();
			}				
		};

		Timer timer = new Timer(ANIMATION_SPEED, timerlistener);

		@Override
		public void itemStateChanged(ItemEvent e) {
			if(e.getStateChange() == ItemEvent.SELECTED) {
				jbprev.setEnabled(false);
				jbnext.setEnabled(false);
				timer.start();
			} else {
				timer.stop();
				jbprev.setEnabled(true);
				jbnext.setEnabled(true);
			}
		}	
	};

			
	ActionListener jbprevlistener= new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			if (pidx > 0) {
				pidx = (pidx<=0) ? 0 : pidx-1;
				jbinfo.setText("Path# " + Integer.toString(pidx+1));
				paintGraph(pidx);
				jpgraphinfo.setText(graphInfo(pidx));
			}
			//System.out.println("ARPB:"+GraphPaths.getMaxIdx());
		}
	};


	ActionListener jbnextlistener= new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			int max = GraphPaths.getMaxIdx();
			if (pidx < max-1) {
				pidx = (pidx<max-1) ? pidx+1 : max-1;
				jbinfo.setText("Path# " + Integer.toString(pidx+1));
				paintGraph(pidx);
				jpgraphinfo.setText(graphInfo(pidx));
			}
			//System.out.println("ARPC:"+GraphPaths.getMaxIdx());
		}
	};


}

class consolePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	JTextArea jta;
	JScrollPane jsp;
	JButton jbclear;
	GridBagConstraints c;

	public consolePanel() {
		this.setLayout(new GridBagLayout());
		this.setBorder(new TitledBorder(new LineBorder(Color.black, 2), "Console"));
		c = new GridBagConstraints();

		jta = new JTextArea (30, 60);
		jta.setEditable(false);
		jta.setFont(new Font("Arial", Font.PLAIN, 12));
		jta.setLineWrap(true);
		jta.setWrapStyleWord(true);
		jsp = new JScrollPane(jta);
		
		c.gridx = 0;
		c.gridy = 0;
		this.add(jsp, c);
		
		jbclear = new JButton("Clear");
		c.gridx = 0;
		c.gridy = 1;
		this.add(jbclear, c);
		jbclear.addActionListener(jbclearlistener);

		this.add(jsp);
		redirectSystemStreams();
	}
	
	ActionListener jbclearlistener= new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			jta.setText("");
		}
	};

	private void updateTextArea(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jta.append(text);
			}
		});
	}

	private void redirectSystemStreams() {
		OutputStream err = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				updateTextArea(String.valueOf((char) b));
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				updateTextArea(new String(b, off, len));
			}

			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}
		};

		System.setErr(new PrintStream(err, true));
	}

}

class textPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	JTextArea jta;
	JScrollPane jsp;
	JButton jbclear;
	GridBagConstraints c;

	public textPanel() {
		this.setLayout(new GridBagLayout());
		this.setBorder(new TitledBorder(new LineBorder(Color.black, 2), "Text"));
		c = new GridBagConstraints();

		jta = new JTextArea (30, 60);
		jta.setEditable(false);
		jta.setFont(new Font("Arial", Font.PLAIN, 12));
		jta.setLineWrap(true);
		jta.setWrapStyleWord(true);
		jsp = new JScrollPane(jta);

		c.gridx = 0;
		c.gridy = 0;
		this.add(jsp, c);
		
		jbclear = new JButton("Clear");
		c.gridx = 0;
		c.gridy = 1;
		this.add(jbclear, c);
		jbclear.addActionListener(jbclearlistener);

		redirectSystemStreams();
	}
	
	ActionListener jbclearlistener= new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			jta.setText("");
		}
	};


	private void updateTextArea(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jta.append(text);
			}
		});
	}

	private void redirectSystemStreams() {
		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				updateTextArea(String.valueOf((char) b));
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				updateTextArea(new String(b, off, len));
			}

			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}
		};

		System.setOut(new PrintStream(out, true));
	}

}


