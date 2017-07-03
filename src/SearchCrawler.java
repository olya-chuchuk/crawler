import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.table.*;

// The Search Web Crawler
public class SearchCrawler extends JFrame
{
  // Max URLs drop down values.
  private static final String[] MAX_URLS =
    {"50", "100", "500", "1000"};

  // Cache of robot disallow lists.
  private HashMap<String, ArrayList<String>> disallowListCache = new HashMap<String, ArrayList<String>>();

  // Search GUI controls.
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

  // Constructor for Search Web Crawler.
  public SearchCrawler()
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

  // Handle search/stop button being clicked.
  private void actionSearch() {
    // If stop button clicked, turn crawling flag off.
    if (crawling) {
      crawling = false;
      return;
    }

    ArrayList<String> errorList = new ArrayList<String>();

    // Validate that start URL has been entered.
    String startUrl = startTextField.getText().trim();
    // add / to the end
    if(startUrl.charAt(startUrl.length()-1) != '/')
    {
  	  startUrl = startUrl + "/";
    }
    if (startUrl.length() < 1) {
      errorList.add("Missing Start URL.");
    }
    // Verify start URL.
    else if (verifyUrl(startUrl) == null) {
      errorList.add("Invalid Start URL.");
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
    startUrl = removeWwwFromUrl(startUrl); 

    // Start the search crawler.
    search(logFile, startUrl, maxUrls, searchString);
  }

  private void search(final String logFile, final String startUrl,
    final int maxUrls, final String searchString)
  {
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

  // Show dialog box with error message.
  private void showError(String message) {
    JOptionPane.showMessageDialog(this, message, "Error",
      JOptionPane.ERROR_MESSAGE);
  }

  // Update crawling stats.
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

  // Add match to matches table and log file.
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

  // Verify URL format.
  private URL verifyUrl(String url) {
    // Only allow HTTP URLs.
    if (!url.toLowerCase().startsWith("http://"))
      return null;

    // Verify format of URL.
    URL verifiedUrl = null;
    try {
      verifiedUrl = new URL(url);
    } catch (Exception e) {
      return null;
    }

    return verifiedUrl;
  }

  // Check if robot is allowed to access the given URL.
  private boolean isRobotAllowed(URL urlToCheck) {
    String host = urlToCheck.getHost().toLowerCase();

    // Retrieve host's disallow list from cache.
    ArrayList<String> disallowList =
      (ArrayList) disallowListCache.get(host);

    // If list is not in the cache, download and cache it.
    if (disallowList == null) {
      disallowList = new ArrayList<String>();

      try {
        URL robotsFileUrl =
          new URL("http://" + host + "/robots.txt");

        // Open connection to robot file URL for reading.
        BufferedReader reader =
          new BufferedReader(new InputStreamReader(
            robotsFileUrl.openStream()));

        // Read robot file, creating list of disallowed paths.
        String line;
        while ((line = reader.readLine()) != null) {
        	//System.out.println(line);
          if (line.indexOf("Disallow:") == 0) {
            String disallowPath =
              line.substring("Disallow:".length());

            // Check disallow path for comments and remove if present.
            int commentIndex = disallowPath.indexOf("#");
            if (commentIndex != - 1) {
              disallowPath =
                disallowPath.substring(0, commentIndex);
            }

            // Remove leading or trailing spaces from disallow path.
            disallowPath = disallowPath.trim();

            // Add disallow path to list.
            if(disallowPath.length() != 0)
            {
            	disallowList.add(disallowPath);
            }
          }
        }

        // Add new disallow list to cache.
        disallowListCache.put(host, disallowList);
      }
      catch (Exception e) {
        /* Assume robot is allowed since an exception
           is thrown if the robot file doesn't exist. */
        return true;
      }
    }

    /* Loop through disallow list to see if the
       crawling is allowed for the given URL. */
    String file = urlToCheck.getFile();
    for (int i = 0; i < disallowList.size(); i++) {
      String disallow = (String) disallowList.get(i);
      if (file.startsWith(disallow)) {
        return false;
      }
    }

    return true;
  }

  // Download page at given URL.
  private String downloadPage(URL pageUrl) {
     try {
        // Open connection to URL for reading.
        BufferedReader reader =
          new BufferedReader(new InputStreamReader(
            pageUrl.openStream(),"UTF-8"));

        // Read page into buffer.
        String line;
        StringBuffer pageBuffer = new StringBuffer();
        while ((line = reader.readLine()) != null) {
        	/*Matcher m = p.matcher(line);
        	while(m.find())
        	{
        		pageBuffer.append(m.group(1));
        	}*/
          pageBuffer.append(line);
        }
        return pageBuffer.toString();
     } catch (Exception e) {
     }

     return null;
  }

  // Remove leading "www" from a URL's host if present.
  private String removeWwwFromUrl(String url) {
    int index = url.indexOf("://www.");
    if (index != -1) {
      return url.substring(0, index + 3) +
        url.substring(index + 7);
    }

    return (url);
  }

  // Parse through page contents and retrieve links.
  private ArrayList<String> retrieveLinks(
    URL pageUrl, String pageContents, HashSet<String> crawledList)
  {
    // Compile link matching pattern.
    Pattern p =
      Pattern.compile("<a\\s+href\\s*=\\s*\"?(.*?)[\"|>]", // for example <a href="http://mi.unicyb.kiev.ua/ru/">
        Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(pageContents);

    // Create list of link matches.
    ArrayList<String> linkList = new ArrayList<String>();
    while (m.find()) {
      String link = m.group(1).trim();

      // Skip empty links.
      if (link.length() < 1) {
        continue;
      }

      // Skip links that are just page anchors.
      if (link.charAt(0) == '#') {
        continue;
      }

      // Skip mailto links.
      if (link.indexOf("mailto:") != -1) {
        continue;
      }

      // Skip JavaScript links.
      if (link.toLowerCase().indexOf("javascript") != -1) {
        continue;
      }

      // Prefix absolute and relative URLs if necessary.
      if (link.indexOf("://") == -1) {
        // Handle absolute URLs.
        if (link.charAt(0) == '/') {
          link = "http://" + pageUrl.getHost() + link;
        // Handle relative URLs.
        } else {
          String file = pageUrl.getFile();
          if (file.indexOf('/') == -1) {
            link = "http://" + pageUrl.getHost() + "/" + link;
          } else {
            String path =
              file.substring(0, file.lastIndexOf('/') + 1);
            link = "http://" + pageUrl.getHost() + path + link;
          }
        }
      }

      // Remove anchors from link.
      int index = link.indexOf('#');
      if (index != -1) {
        link = link.substring(0, index);
      }

      // Remove leading "www" from URL's host if present.
      link = removeWwwFromUrl(link);
      
      link = link.trim();
      
      // add / to the end
      if(link.charAt(link.length()-1) != '/')
      {
    	  link = link + "/";
      }

      // Verify link and skip if invalid.
      URL verifiedLink = verifyUrl(link);
      if (verifiedLink == null) {
        continue;
      }

      // Skip link if it has already been crawled.
      if (crawledList.contains(link)) {
        continue;
      }

      // Add link to list.
      linkList.add(link);
    }

    return (linkList);
  }

  /* Determine whether or not search string is
     matched in the given page contents. */
  private boolean searchStringMatches(
    String pageContents, String searchString,
    boolean caseSensitive)
  {
    String searchContents = pageContents;

    /* If case sensitive search, lowercase
       page contents for comparison. */
    if (!caseSensitive) {
      searchContents = pageContents.toLowerCase();
    }

    // Split search string into individual terms.
    Pattern p = Pattern.compile("[\\s]+");
    String[] terms = p.split(searchString);

    // Check to see if each term matches.
    for (int i = 0; i < terms.length; i++) {
      if (caseSensitive) {
        if (searchContents.indexOf(terms[i]) == -1) {
          return false;
        }
      } else {
        if (searchContents.indexOf(terms[i].toLowerCase()) == -1) {
          return false;
        }
      }
    }

    return true;
  }

  // Perform the actual crawling, searching for the search string.
  public void crawl(
    String startUrl, int maxUrls,
    String searchString, boolean caseSensitive)
  {
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
      URL verifiedUrl = verifyUrl(url);

      // Skip URL if robots are not allowed to access it.
      if (!isRobotAllowed(verifiedUrl)) {
        continue;
      }

      // Update crawling stats.
      updateStats(url, crawledList.size(), toCrawlList.size(),
        maxUrls);

      // Add page to the crawled list.
      crawledList.add(url);

      // Download the page at the given url.
      String pageContents = downloadPage(verifiedUrl);
     // System.out.println(pageContents);

      /* If the page was downloaded successfully, retrieve all of its
         links and then see if it contains the search string. */
      if (pageContents != null && pageContents.length() > 0)
      {
        // Retrieve list of valid links from page.
        ArrayList links =
          retrieveLinks(verifiedUrl, pageContents, crawledList);

        // Add links to the to crawl list.
        toCrawlList.addAll(links);

        /* Check if search string is present in
           page and if so record a match. */
        if (searchStringMatches(pageContents, searchString,
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
  
  // Crawling for the algorithm R2
  public void adaptiveCrawlR2(String startUrl, int maxUrls, 
  String searchString, boolean caseSensitive)
  {
	  	// Setup crawl lists.
	    HashSet<String> crawledList = new HashSet<String>();
	    HashMap<String,LinkedHashSet<String>> toCrawlList = new HashMap<String,LinkedHashSet<String>>();
	    HashMap<String,Integer> crawledAtHost = new HashMap<String,Integer>();
	    HashMap<String,Integer> foundAtHost = new HashMap<String,Integer>();
	    HashMap<String,Double> GittinsIndex = new HashMap<String,Double>();
	    double maxGittins = 0;
	    int toCrawlSize = 0;
	    
	    // Add start URL to the to crawl list.
	    String host = verifyUrl(startUrl).getHost();
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
	    		host = verifyUrl((String)toCrawl.iterator().next()).getHost();
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
	    		    URL verifiedUrl = verifyUrl(url);
	    		    if (!isRobotAllowed(verifiedUrl)) 
	    		    {
	    		    	System.out.println("Not Robot Allowed");
	    		    	continue;
	    		    }
	    		    updateStats(url, crawledList.size(), toCrawlSize,
	    		        maxUrls);
	    		    
	    		    // Add page to the crawled list.
	    		    crawledList.add(url);

	    		    // Download the page at the given url.
	    		    String pageContents = downloadPage(verifiedUrl);

	    		    /* If the page was downloaded successfully, retrieve all of its
	    		     links and then see if it contains the search string. */
	    		    if (pageContents != null && pageContents.length() > 0)
	    		    {
	    		    	// Retrieve list of valid links from page.
	    		        ArrayList<String> links =
	    		          retrieveLinks(verifiedUrl, pageContents, crawledList);

	    		        // Add links to the to crawl list.
	    		        toCrawlSize += links.size();
	    		        for(int i = 0;i<links.size();++i)
	    		        {
	    		        	String newHost = verifyUrl(links.get(i)).getHost();
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
	    		        if (searchStringMatches(pageContents, searchString,
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
	    		String newHost = verifyUrl(h).getHost();
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

  // Run the Search Crawler.
public static void main(String[] args) {
    SearchCrawler crawler = new SearchCrawler();
    crawler.show();
  }
}
