package crawler;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * The search web crawler that performs search on websites
 * @author olya-chuchuk
 */
public class SearchCrawler extends JFrame
{
  /**
   * Max URLs drop down values.
   */
  private static final String[] MAX_URLS =
    {"50", "100", "500", "1000"};

  /**
   * Cache of robot disallow lists.
   */
  private HashMap<String, ArrayList<String>> disallowListCache =
          new HashMap<String, ArrayList<String>>();

  /**
   * Search GUI controls.
   */
  private JTextField startTextField;
  private JComboBox maxComboBox;
  private JTextField logTextField;
  private JTextField searchTextField;
  private JCheckBox caseCheckBox;
  private JButton searchButton;
  private JRadioButton but1;
  private JRadioButton but2;
  private ButtonGroup butgr;

  // Search stats GUI controls.
  private JLabel crawlingLabel2;
  private JLabel crawledLabel2;
  private JLabel toCrawlLabel2;
  private JProgressBar progressBar;
  private JLabel matchesLabel2;

  // Table listing search matches.
  private JTable table;

  // Flag for whether or not crawling is underway.
  private boolean crawling;

  // Matches log file print writer.
  private PrintWriter logFileWriter;

  /**
   * Constructor for Search Web Crawler.
   */
  private SearchCrawler()
  {
    // Set application title.
    setTitle("Search Crawler");

    // Set window size.
    setSize(600, 600);

    // Handle window closing events.
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        actionExit();
      }
    });

    // Set up file menu.
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);
    JMenuItem fileExitMenuItem = new JMenuItem("Exit",
      KeyEvent.VK_X);
    fileExitMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionExit();
      }
    });
    fileMenu.add(fileExitMenuItem);
    menuBar.add(fileMenu);
    setJMenuBar(menuBar);

    // Set up search panel.
    JPanel searchPanel = new JPanel();
    GridBagConstraints constraints;
    GridBagLayout layout = new GridBagLayout();
    searchPanel.setLayout(layout);

    JLabel startLabel = new JLabel("Start URL:");
    constraints = new GridBagConstraints();
    constraints.anchor = GridBagConstraints.EAST;
    constraints.insets = new Insets(5, 5, 0, 0);
    layout.setConstraints(startLabel, constraints);
    searchPanel.add(startLabel);

    startTextField = new JTextField();
    constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.insets = new Insets(5, 5, 0, 5);
    layout.setConstraints(startTextField, constraints);
    searchPanel.add(startTextField);

    JLabel maxLabel = new JLabel("Max URLs to Crawl:");
    constraints = new GridBagConstraints();
    constraints.anchor = GridBagConstraints.EAST;
    constraints.insets = new Insets(5, 5, 0, 0);
    layout.setConstraints(maxLabel, constraints);
    searchPanel.add(maxLabel);

    maxComboBox = new JComboBox(MAX_URLS);
    maxComboBox.setEditable(true);
    constraints = new GridBagConstraints();
    constraints.insets = new Insets(5, 5, 0, 0);
    layout.setConstraints(maxComboBox, constraints);
    searchPanel.add(maxComboBox);
    
    but1 = new JRadioButton("BFS ");
    but2 = new JRadioButton("Adaptive Search ");
    but1.setSelected(true);
    butgr = new ButtonGroup();
    butgr.add(but1);
    butgr.add(but2);
    constraints = new GridBagConstraints();
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.insets = new Insets(0, 10, 0, 0);
    layout.setConstraints(but1, constraints);
    searchPanel.add(but1);
    constraints = new GridBagConstraints();
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.insets = new Insets(0, 10, 0, 0);
    layout.setConstraints(but2, constraints);
    searchPanel.add(but2);

    JLabel blankLabel = new JLabel();
    constraints = new GridBagConstraints();
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    layout.setConstraints(blankLabel, constraints);
    searchPanel.add(blankLabel);

    JLabel logLabel = new JLabel("Matches Log File:");
    constraints = new GridBagConstraints();
    constraints.anchor = GridBagConstraints.EAST;
    constraints.insets = new Insets(5, 5, 0, 0);
    layout.setConstraints(logLabel, constraints);
    searchPanel.add(logLabel);

    String file =
      System.getProperty("user.dir") +
      System.getProperty("file.separator") +
      "crawler.log";
    logTextField = new JTextField(file);
    constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.insets = new Insets(5, 5, 0, 5);
    layout.setConstraints(logTextField, constraints);
    searchPanel.add(logTextField);

    JLabel searchLabel = new JLabel("Search String:");
    constraints = new GridBagConstraints();
    constraints.anchor = GridBagConstraints.EAST;
    constraints.insets = new Insets(5, 5, 0, 0);
    layout.setConstraints(searchLabel, constraints);
    searchPanel.add(searchLabel);

    searchTextField = new JTextField();
    constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.insets = new Insets(5, 5, 0, 0);
    constraints.gridwidth= 2;
    constraints.weightx = 1.0d;
    layout.setConstraints(searchTextField, constraints);
    searchPanel.add(searchTextField);

    caseCheckBox = new JCheckBox("Case Sensitive");
    constraints = new GridBagConstraints();
    constraints.insets = new Insets(5, 5, 0, 5);
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    layout.setConstraints(caseCheckBox, constraints);
    searchPanel.add(caseCheckBox);

    searchButton = new JButton("Search");
    searchButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionSearch();
      }
    });
    constraints = new GridBagConstraints();
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.insets = new Insets(5, 5, 5, 5);
    layout.setConstraints(searchButton, constraints);
    searchPanel.add(searchButton);

    JSeparator separator = new JSeparator();
    constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.insets = new Insets(5, 5, 5, 5);
    layout.setConstraints(separator, constraints);
    searchPanel.add(separator);

    JLabel crawlingLabel1 = new JLabel("Crawling:");
    constraints = new GridBagConstraints();
    constraints.anchor = GridBagConstraints.EAST;
    constraints.insets = new Insets(5, 5, 0, 0);
    layout.setConstraints(crawlingLabel1, constraints);
    searchPanel.add(crawlingLabel1);

    crawlingLabel2 = new JLabel();
    crawlingLabel2.setFont(
      crawlingLabel2.getFont().deriveFont(Font.PLAIN));
    constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.insets = new Insets(5, 5, 0, 5);
    layout.setConstraints(crawlingLabel2, constraints);
    searchPanel.add(crawlingLabel2);

    JLabel crawledLabel1 = new JLabel("Crawled URLs:");
    constraints = new GridBagConstraints();
    constraints.anchor = GridBagConstraints.EAST;
    constraints.insets = new Insets(5, 5, 0, 0);
    layout.setConstraints(crawledLabel1, constraints);
    searchPanel.add(crawledLabel1);

    crawledLabel2 = new JLabel();
    crawledLabel2.setFont(
      crawledLabel2.getFont().deriveFont(Font.PLAIN));
    constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.insets = new Insets(5, 5, 0, 5);
    layout.setConstraints(crawledLabel2, constraints);
    searchPanel.add(crawledLabel2);

    JLabel toCrawlLabel1 = new JLabel("URLs to Crawl:");
    constraints = new GridBagConstraints();
    constraints.anchor = GridBagConstraints.EAST;
    constraints.insets = new Insets(5, 5, 0, 0);
    layout.setConstraints(toCrawlLabel1, constraints);
    searchPanel.add(toCrawlLabel1);

    toCrawlLabel2 = new JLabel();
    toCrawlLabel2.setFont(
      toCrawlLabel2.getFont().deriveFont(Font.PLAIN));
    constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.insets = new Insets(5, 5, 0, 5);
    layout.setConstraints(toCrawlLabel2, constraints);
    searchPanel.add(toCrawlLabel2);

    JLabel progressLabel = new JLabel("Crawling Progress:");
    constraints = new GridBagConstraints();
    constraints.anchor = GridBagConstraints.EAST;
    constraints.insets = new Insets(5, 5, 0, 0);
    layout.setConstraints(progressLabel, constraints);
    searchPanel.add(progressLabel);

    progressBar = new JProgressBar();
    progressBar.setMinimum(0);
    progressBar.setStringPainted(true);
    constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.insets = new Insets(5, 5, 0, 5);
    layout.setConstraints(progressBar, constraints);
    searchPanel.add(progressBar);

    JLabel matchesLabel1 = new JLabel("Search Matches:");
    constraints = new GridBagConstraints();
    constraints.anchor = GridBagConstraints.EAST;
    constraints.insets = new Insets(5, 5, 10, 0);
    layout.setConstraints(matchesLabel1, constraints);
    searchPanel.add(matchesLabel1);

    matchesLabel2 = new JLabel();
    matchesLabel2.setFont(
      matchesLabel2.getFont().deriveFont(Font.PLAIN));
    constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.insets = new Insets(5, 5, 10, 5);
    layout.setConstraints(matchesLabel2, constraints);
    searchPanel.add(matchesLabel2);

    // Set up matches table.
    table =
      new JTable(new DefaultTableModel(new Object[][]{},
        new String[]{"URL"}) {
      public boolean isCellEditable(int row, int column)
      {
        return false;
      }
    });

    // Set up matches panel.
    JPanel matchesPanel = new JPanel();
    matchesPanel.setBorder(
      BorderFactory.createTitledBorder("Matches"));
    matchesPanel.setLayout(new BorderLayout());
    matchesPanel.add(new JScrollPane(table),
      BorderLayout.CENTER);

    // Add panels to display.
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(searchPanel, BorderLayout.NORTH);
    getContentPane().add(matchesPanel, BorderLayout.CENTER);
  }

  // Exit this program.
  private void actionExit() {
    System.exit(0);
  }

  /**
   * Handle search/stop button being clicked.
   */
  private void actionSearch() {
    // If stop button clicked, turn crawling flag off.
    if (crawling) {
      crawling = false;
      return;
    }

    ArrayList<String> errorList = new ArrayList<String>();

    // Validate that start URL has been entered.
    String startUrl = startTextField.getText().trim();
    if (startUrl.length() < 1) {
      errorList.add("Missing Start URL.");
    } else {
        // add / to the end
        if(startUrl.charAt(startUrl.length()-1) != '/')
        {
      	  startUrl = startUrl + "/";
        }
        // Verify start URL.
        if (WebTools.verifyUrl(startUrl) == null) {
          errorList.add("Invalid Start URL.");
        }
    }
    
    // Validate that max URLs is either empty or is a number.
    int maxUrls = -1;
    String max = ((String) maxComboBox.getSelectedItem()).trim();
    if (max.length() > 0) {
      try {
        maxUrls = Integer.parseInt(max);
      } catch (NumberFormatException e) {
      }
      if (maxUrls < 1) {
        errorList.add("Invalid Max URLs value.");
      }
    }

    // Validate that matches log file has been entered.
    String logFile = logTextField.getText().trim();
    if (logFile.length() < 1) {
      errorList.add("Missing Matches Log File.");
    }

    // Validate that search string has been entered.
    String searchString = searchTextField.getText().trim();
    if (searchString.length() < 1) {
      errorList.add("Missing Search String.");
    }

    // Show errors, if any, and return.
    if (errorList.size() > 0) {
      StringBuffer message = new StringBuffer();

      // Concatenate errors into single message.
      for (int i = 0; i < errorList.size(); i++) {
        message.append(errorList.get(i));
        if (i + 1 < errorList.size()) {
          message.append("\n");
        }
      }

      showError(message.toString());
      return;
    }

    // Remove "www" from start URL if present.
    startUrl = WebTools.removeWwwFromUrl(startUrl); 

    // Start the search crawler.
    search(logFile, startUrl, maxUrls, searchString);
  }

  /**
   * Performs the actual search
   * 
   * @param logFile
   * @param startUrl
   * @param maxUrls
   * @param searchString
   */
  private void search(final String logFile, final String startUrl,
          final int maxUrls, final String searchString) {
    // Start the search in a new thread.
    Thread thread = new Thread(new Runnable() {
      public void run() {

          // Open matches log file.
          try {
            logFileWriter = new PrintWriter(new FileWriter(logFile));
          } catch (Exception e) {
            showError("Unable to open matches log file.");
            
            return;
          }
        // Show hour glass cursor while crawling is under way.
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // Disable search controls.
        startTextField.setEnabled(false);
        maxComboBox.setEnabled(false);
        logTextField.setEnabled(false);
        searchTextField.setEnabled(false);
        caseCheckBox.setEnabled(false);

        // Switch search button to "Stop."
        searchButton.setText("Stop");

        // Reset stats.
        table.setModel(new DefaultTableModel(new Object[][]{},
          new String[]{"URL"}) {
          public boolean isCellEditable(int row, int column)
          {
            return false;
          }
        });
        updateStats(startUrl, 0, 0, maxUrls); 


        // Turn crawling flag on.
        crawling = true;

        // Perform the actual crawling.
        if(but1.isSelected())
        {
        	crawl(startUrl, maxUrls,
        	          searchString, caseCheckBox.isSelected());
        }
        else
        {
        	adaptiveCrawlR2(startUrl, maxUrls,
        			searchString, caseCheckBox.isSelected());
        }

        // Turn crawling flag off.
        crawling = false;

        // Close matches log file.
        try {
          logFileWriter.close();
        } catch (Exception e) {
          showError("Unable to close matches log file.");
        }

        // Mark search as done.
        crawlingLabel2.setText("Done");

        // Enable search controls.
        startTextField.setEnabled(true);
        maxComboBox.setEnabled(true);
        logTextField.setEnabled(true);
        searchTextField.setEnabled(true);
        caseCheckBox.setEnabled(true);
        
        // Switch search button back to "Search."
        searchButton.setText("Search");

        // Return to default cursor.
        setCursor(Cursor.getDefaultCursor());

        // Show message if search string not found.
        if (table.getRowCount() == 0) {
          JOptionPane.showMessageDialog(SearchCrawler.this,
            "Your Search String was not found. Please try another.",
            "Search String Not Found",
            JOptionPane.WARNING_MESSAGE);
        }
      }
    });
    thread.start();
  }

  /**
   * Show dialog box with error message.
   * 
   * @param message
   */
  private void showError(String message) {
    JOptionPane.showMessageDialog(this, message, "Error",
      JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Update crawling stats.
   * 
   * @param crawling
   * @param crawled
   * @param toCrawl
   * @param maxUrls
   */
  private void updateStats(
    String crawling, int crawled, int toCrawl, int maxUrls)
  {
    crawlingLabel2.setText(crawling);
    crawledLabel2.setText("" + crawled);
    toCrawlLabel2.setText("" + toCrawl);

    // Update progress bar.
    if (maxUrls == -1) {
      progressBar.setMaximum(crawled + toCrawl);
    } else {
      progressBar.setMaximum(maxUrls);
    }
    progressBar.setValue(crawled);

    matchesLabel2.setText("" + table.getRowCount());
  }

  /**
   * Add match to matches table and log file.
   * 
   * @param url
   */
  private void addMatch(String url) {
    // Add URL to matches table.
    DefaultTableModel model =
      (DefaultTableModel) table.getModel();
    model.addRow(new Object[]{url});

    // Add URL to matches log file.
    try {
      logFileWriter.println(url);
    } catch (Exception e) {
      showError("Unable to log match.");
    }
  }


  

  




  

  /**
   * Perform the actual crawling, searching for the search string.
   * Uses algorithm BFS.
   * 
   * @param startUrl
   * @param maxUrls
   * @param searchString
   * @param caseSensitive
   */
    private void crawl(String startUrl, int maxUrls,
            String searchString, boolean caseSensitive) {
    // Setup crawl lists.
    HashSet<String> crawledList = new HashSet<String>();
    LinkedHashSet<String> toCrawlList = new LinkedHashSet<String>();

    // Add start URL to the to crawl list.
    toCrawlList.add(startUrl);

    /* Perform actual crawling by looping
       through the to crawl list. */
    while (crawling && toCrawlList.size() > 0)
    {
      /* Check to see if the max URL count has
          been reached, if it was specified.*/
      if (maxUrls != -1) {
        if (crawledList.size() == maxUrls) {
          break;
        }
      }

      // Get URL at bottom of the list.
      String url = (String) toCrawlList.iterator().next();

      // Remove URL from the to crawl list.
      toCrawlList.remove(url);

      // Convert string url to URL object.
      URL verifiedUrl = WebTools.verifyUrl(url);

      // Skip URL if robots are not allowed to access it.
      if (!WebTools.isRobotAllowed(verifiedUrl, disallowListCache)) {
        continue;
      }

      // Update crawling stats.
      updateStats(url, crawledList.size(), toCrawlList.size(),
        maxUrls);

      // Add page to the crawled list.
      crawledList.add(url);

      // Download the page at the given url.
      String pageContents = WebTools.downloadPage(verifiedUrl);
     // System.out.println(pageContents);

      /* If the page was downloaded successfully, retrieve all of its
         links and then see if it contains the search string. */
      if (pageContents != null && pageContents.length() > 0)
      {
        // Retrieve list of valid links from page.
        ArrayList links =
          WebTools.retrieveLinks(verifiedUrl, pageContents, crawledList);

        // Add links to the to crawl list.
        toCrawlList.addAll(links);

        /* Check if search string is present in
           page and if so record a match. */
        if (WebTools.searchStringMatches(pageContents, searchString,
             caseSensitive))
        {
          addMatch(url);
        }
      }

      // Update crawling stats.
      updateStats(url, crawledList.size(), toCrawlList.size(),
        maxUrls);
    }
  }
  
  /**
   * Performs crawling for the algorithm R2
   * 
   * @param startUrl
   * @param maxUrls
   * @param searchString
   * @param caseSensitive
   */
    private void adaptiveCrawlR2(String startUrl, int maxUrls,
            String searchString, boolean caseSensitive) {
	  	// Setup crawl lists.
	    HashSet<String> crawledList = new HashSet<String>();
	    HashMap<String,LinkedHashSet<String>> toCrawlList = new HashMap<String,LinkedHashSet<String>>();
	    HashMap<String,Integer> crawledAtHost = new HashMap<String,Integer>();
	    HashMap<String,Integer> foundAtHost = new HashMap<String,Integer>();
	    HashMap<String,Double> GittinsIndex = new HashMap<String,Double>();
	    double maxGittins = 0;
	    int toCrawlSize = 0;
	    
	    // Add start URL to the to crawl list.
	    String host = WebTools.verifyUrl(startUrl).getHost();
	    System.out.println("Start host " + host);
	    if(toCrawlList.containsKey(host))
	    {
	    	toCrawlList.get(host).add(startUrl);
	    }
	    else
	    {
	    	toCrawlList.put(host, new LinkedHashSet<String>());
	    	toCrawlList.get(host).add(startUrl);
	    }
	    toCrawlSize++;
	    
	    int maxForEach = 5;
	    int hostNumber = toCrawlList.size();
	    System.out.println("Number of hosts " + hostNumber);
	    
	    while(crawling && toCrawlList.size() > 0 && (maxUrls == -1 || crawledList.size() < maxUrls))
	    {
	    	for(String host_:toCrawlList.keySet())
	    	{
	    		if(crawledAtHost.containsKey(host_))
	    		{
	    			double ind = foundAtHost.get(host_);
	    			if(crawledAtHost.get(host_) == 0)
	    			{
	    				ind /= crawledAtHost.get(host_);
		    			GittinsIndex.put(host_,ind );
		    			maxGittins = Math.max(maxGittins,ind );
	    			}
	    		}
	    		else
	    		{
	    			crawledAtHost.put(host_, 0);
	    			GittinsIndex.put(host_, (double) 0);
	    			foundAtHost.put(host_,0);	    			
	    		}
	    		System.out.println("Host " + host_ + "Gittins " + GittinsIndex.get(host_));
	    	}
	    	LinkedHashSet<String> toCrawlToAddList = new LinkedHashSet<String>();
	    	int remainsNew = 5;
	    	for(LinkedHashSet toCrawl:toCrawlList.values())
	    	{
	    		host = WebTools.verifyUrl((String)toCrawl.iterator().next()).getHost();
	    		double index = GittinsIndex.get(host);
	    		int crawled = crawledAtHost.get(host);
	    		if(crawled != 0 && index < maxGittins)
	    		{
	    			System.out.println("Not maximum host " + host);
	    			continue;
	    		}
	    		if(crawled == 0)
	    		{
	    			if(remainsNew == 0)
	    			{
	    				continue;
	    			}
	    			else
	    			{
	    				remainsNew--;
	    			}
	    		}
	    		System.out.println("Stayed host " + host);
	    		int remains = maxForEach;
	    		while(crawling && remains > 0 && toCrawl.size() > 0 && (maxUrls == -1 || crawledList.size() < maxUrls))	 
	    		{
	    			remains--;
	    			System.out.println("Remains " + remains);
	    			String url = (String) toCrawl.iterator().next();
	    			System.out.println("URL " + url);
	    			crawledAtHost.put(host, crawledAtHost.get(host) + 1);
	    			toCrawl.remove(url);
	    		    URL verifiedUrl = WebTools.verifyUrl(url);
	    		    if (!WebTools.isRobotAllowed(verifiedUrl, disallowListCache)) 
	    		    {
	    		    	System.out.println("Not Robot Allowed");
	    		    	continue;
	    		    }
	    		    updateStats(url, crawledList.size(), toCrawlSize,
	    		        maxUrls);
	    		    
	    		    // Add page to the crawled list.
	    		    crawledList.add(url);

	    		    // Download the page at the given url.
	    		    String pageContents = WebTools.downloadPage(verifiedUrl);

	    		    /* If the page was downloaded successfully, retrieve all of its
	    		     links and then see if it contains the search string. */
	    		    if (pageContents != null && pageContents.length() > 0)
	    		    {
	    		    	// Retrieve list of valid links from page.
	    		        ArrayList<String> links =
	    		          WebTools.retrieveLinks(verifiedUrl, pageContents, crawledList);

	    		        // Add links to the to crawl list.
	    		        toCrawlSize += links.size();
	    		        for(int i = 0;i<links.size();++i)
	    		        {
	    		        	String newHost = WebTools.verifyUrl(links.get(i)).getHost();
	    		        	if(toCrawlList.get(newHost) != null)
	    		        	{
	    		        		toCrawlList.get(newHost).add(links.get(i));
	    		        	}
	    		        	else
	    		        	{
	    		        		toCrawlToAddList.add(links.get(i));
	    		        	}
	    		        }

	    		        /* Check if search string is present in
	    		           page and if so record a match. */
	    		        if (WebTools.searchStringMatches(pageContents, searchString,
	    		             caseSensitive))
	    		        {
	    		          addMatch(url);
	    		          foundAtHost.put(host,foundAtHost.get(host) + 1);
	    		        }
	    		    }
	    		    updateStats(url,crawledList.size(),toCrawlSize,maxUrls);
	    		}
	    		System.out.println("Crawled at host " + crawledAtHost.get(host));
	    		System.out.println("Found at host " + foundAtHost.get(host));
	    	}
	    	for(String h : toCrawlToAddList)
	    	{
	    		String newHost = WebTools.verifyUrl(h).getHost();
	    		if(toCrawlList.get(newHost) != null)
	        	{
	        		toCrawlList.get(newHost).add(h);
	        	}
	        	else
	        	{
	        		toCrawlList.put(newHost, new LinkedHashSet<String>());
	        		toCrawlList.get(newHost).add(h);
	        	}
	    	}
	    	LinkedList<String> toRemove = new LinkedList<String>();
	    	for(String h:toCrawlList.keySet())
	    	{
	    		if(toCrawlList.get(h).size() == 0)
	    		{
	    			toRemove.add(h);
	    		}
	    	}
	    	for(String h : toRemove)
	    	{
	    		toCrawlList.remove(h);
	    	}
	    }
  }

  /**
   *  Run the Search Crawler.
   *  
   * @param args
   */
  public static void main(String[] args) {
    SearchCrawler crawler = new SearchCrawler();
    crawler.setVisible(true);
  }
}
