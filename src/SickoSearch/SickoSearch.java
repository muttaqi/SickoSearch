/*
Muttaqi Islam, Markus Berglund
16/01/2019
SickoSearch searches for clothing products across multiple sites
 */

package SickoSearch;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.host.Element;
import com.gargoylesoftware.htmlunit.javascript.host.event.KeyboardEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.logging.Level;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javafx.scene.input.KeyCode;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public class SickoSearch extends javax.swing.JFrame {
    
    static ArrayList<String> brands = new ArrayList<>();
    static ArrayList<String> defaultSites = new ArrayList<>();
    static ProductTableModel productModel = new ProductTableModel();
    static FavouriteTableModel favModel = new FavouriteTableModel();
    public static WebClient client;
    
    static ArrayList<Product> products = new ArrayList<>();
    static ArrayList<Product> favProducts = new ArrayList<>();
    
    static ArrayList<String> searches = new ArrayList<>();
    
    static AutoSuggestor brandSuggestor;
    static AutoSuggestor searchSuggestor;

    /**
     * Creates new form SuperSearchUI
     */
    public SickoSearch() {
        initComponents();
        
        //set product table model
        tblProduct.setModel(productModel);
        
        //rendering buttons into the product table
        TableCellRenderer btnRenderer = new JTableButtonRenderer();
        tblProduct.getColumn("Favourite").setCellRenderer(btnRenderer);
        tblProduct.getColumn("Delete").setCellRenderer(btnRenderer);
        tblProduct.addMouseListener(new JTableButtonMouseListener(tblProduct));
        
        // adjusting table columns
        TableColumnModel tcm1 = tblProduct.getColumnModel();
        tcm1.getColumn(0).setPreferredWidth(125);
        tcm1.getColumn(1).setPreferredWidth(200);
        tcm1.getColumn(2).setPreferredWidth(75);
        tcm1.getColumn(3).setPreferredWidth(75);
        tcm1.getColumn(4).setPreferredWidth(75);
        
        //same for favourite models
        tblFav.setModel(favModel);
        
        tblFav.getColumn("Delete").setCellRenderer(btnRenderer);
        tblFav.addMouseListener(new JTableButtonMouseListener(tblFav));
        
        TableColumnModel tcm2 = tblFav.getColumnModel();
        tcm2.getColumn(0).setPreferredWidth(125);
        tcm2.getColumn(1).setPreferredWidth(225);
        tcm2.getColumn(2).setPreferredWidth(75);
        tcm2.getColumn(3).setPreferredWidth(75);

        //prepares the htmlunit client so as to minimize errors and maximize results
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF); 
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
            client = new WebClient(BrowserVersion.BEST_SUPPORTED);
            client.getOptions().setThrowExceptionOnScriptError(false);
            client.setJavaScriptTimeout(10000);
            client.getOptions().setJavaScriptEnabled(true);
            client.getOptions().setThrowExceptionOnScriptError(false);
            client.getOptions().setThrowExceptionOnFailingStatusCode(false);
            client.getOptions().setCssEnabled(false);
            client.setAjaxController(new NicelyResynchronizingAjaxController());
            client.getOptions().setTimeout(10000);
            client.getOptions().setUseInsecureSSL(true);
            client.getOptions().setRedirectEnabled(true);
            client.setCssErrorHandler(new SilentCssErrorHandler());   
        
            //read in all information from text files
        try {
            
            BufferedReader br = new BufferedReader(new FileReader(
                    "src/SickoSearch/BRANDS.txt"));
            
            String in = "";
            while ((in = br.readLine()) != null) {
                
                brands.add(in);
            }
            
            br = new BufferedReader(new FileReader(
                    "src/SickoSearch/DEFAULTSITES.txt"));
            
            in = "";
            while ((in = br.readLine()) != null) {
                
                defaultSites.add(in);
            }
            
            br = new BufferedReader(new FileReader(
                    "src/SickoSearch/FAVOURITEPRODUCTS.txt"));
            
            in = "";
            while ((in = br.readLine()) != null) {
                
                favProducts.add(new Product(br.readLine(), br.readLine(), in));
            }
            
            br = new BufferedReader(new FileReader(
                    "src/SickoSearch/SEARCHES.txt"));
            
            in = "";
            while ((in = br.readLine()) != null) {
                
                searches.add(in);
            }
        } catch (IOException e) {System.out.println(e);}
        
        //load favourites into table
        for (Product p : favProducts) {
            
            favModel.addRow(new String[]{p.getName(), p.getLink(), p.getCost()});
        }
        
        //autosuggestor for brands
        brandSuggestor = new AutoSuggestor(txtBrand, this, null, Color.WHITE.brighter(), Color.BLUE, Color.RED, 0.75f) {
            @Override
            boolean wordTyped(String typedWord) {

                //create list for dictionary this in your case might be done via calling a method which queries db and returns results as arraylist

                setDictionary(brands);

                return super.wordTyped(typedWord);//now call super to check for any matches against newest dictionary
            }
        };
        
        //sort the searches alphabetically to allow for easier selection from suggestions
        sortAlphabetically(searches);
        
        searchSuggestor = new AutoSuggestor(txtSearch, this, null, Color.WHITE.brighter(), Color.BLUE, Color.RED, 0.75f) {
            @Override
            boolean wordTyped(String typedWord) {

                //create list for dictionary this in your case might be done via calling a method which queries db and returns results as arraylist

                setDictionary(searches);

                return super.wordTyped(typedWord);//now call super to check for any matches against newest dictionary
            }
        };
        
        //upon closing, favourite products and searches should be written to the file
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                
                try {
                    
                    BufferedWriter bw = new BufferedWriter(new FileWriter("src/SickoSearch/FAVOURITEPRODUCTS.txt"));

                    for (int i = 0; i < favProducts.size(); i ++) {
                        
                        Product p = favProducts.get(i);
                        
                        if (i == 0) {
                            
                            bw.write(p.getName());
                            bw.write("\n" + p.getLink());
                            bw.write("\n" + p.getCost());
                        }
                        
                        else {
                            
                            bw.write("\n" + p.getName());
                            bw.write("\n" + p.getLink());
                            bw.write("\n" + p.getCost());
                        }
                    }
                    
                    bw = new BufferedWriter(new FileWriter("src/SickoSearch/SEARCHES.txt"));

                    for (int i = 0; i < searches.size(); i ++) {
                        
                        String s = searches.get(i);
                        
                        if (i == 0) {
                            
                            bw.write (s);
                        }
                        
                        else {
                        
                            bw.write("\n" + s);
                        }
                    }
                    
                    bw.close();
                } catch (IOException ioe) {System.out.println(ioe);}
                
                System.exit(0);
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tblProduct = new javax.swing.JTable();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblFav = new javax.swing.JTable();
        lblOut = new javax.swing.JLabel();
        lblPrompt = new javax.swing.JLabel();
        lblFav = new javax.swing.JLabel();
        txtBrand = new javax.swing.JTextField();
        lblPrompt1 = new javax.swing.JLabel();
        lblError = new javax.swing.JLabel();
        progSearch = new javax.swing.JProgressBar();
        lblSearch = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });

        tblProduct.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Product", "Link", "Price", ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tblProduct.setColumnSelectionAllowed(true);
        tblProduct.setRowHeight(22);
        tblProduct.getTableHeader().setResizingAllowed(false);
        jScrollPane1.setViewportView(tblProduct);

        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtSearchKeyPressed(evt);
            }
        });

        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        tblFav.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Product", "Link", "Price", ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tblFav.setColumnSelectionAllowed(true);
        tblFav.getTableHeader().setResizingAllowed(false);
        jScrollPane2.setViewportView(tblFav);

        lblPrompt.setText("Enter all keywords for product, separated by commas (eg. \"blue, eqt, shoe\")");

        lblFav.setText("Favourites:");

        txtBrand.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtBrandKeyPressed(evt);
            }
        });

        lblPrompt1.setText("Enter brand (eg. \"mastermind\")");
        lblPrompt1.setToolTipText("");

        lblError.setForeground(new java.awt.Color(255, 0, 0));

        progSearch.setForeground(new java.awt.Color(0, 153, 255));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(btnSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtSearch)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblFav)
                            .addComponent(lblPrompt)
                            .addComponent(lblPrompt1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(progSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(lblSearch)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(txtBrand))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(506, 506, 506)
                        .addComponent(lblOut, javax.swing.GroupLayout.PREFERRED_SIZE, 365, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblError, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblPrompt1)
                        .addGap(18, 18, 18)
                        .addComponent(txtBrand, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblPrompt)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(progSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(26, 26, 26)
                        .addComponent(lblFav)
                        .addGap(1, 1, 1)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 473, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblError, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblOut)
                .addGap(0, 0, 0))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        // TODO add your handling code here:
            
        String search = txtSearch.getText();
        String brand = txtBrand.getText();
        
        //make sure of good input
        if (!search.equals("") && !brand.equals("")) {
        
            //check if already added
            boolean alreadyAdded = false;
            for (String s : searches) {

                if (s.equals(search)) {

                    alreadyAdded = true;
                }
            }
            
            if (!alreadyAdded) {
            
                searches.add(search);
                
                sortAlphabetically(searches);
            }
            
        //disable button and run search on new thread to allow for live updating to user
            btnSearch.setEnabled(false);
            new SiteSearch(search, brand).start();
        }
        
        //no input, output error
        else {
            
            lblError.setText("That didn't seem to work. Try again.");
        }
    }//GEN-LAST:event_btnSearchActionPerformed

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_formKeyPressed

    private void txtBrandKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBrandKeyPressed
        // TODO add your handling code here:
        
        if (evt.getKeyChar() == '\n') {
        
            //hide the brand suggestor
            brandSuggestor.hide();
        }
    }//GEN-LAST:event_txtBrandKeyPressed

    private void txtSearchKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearchKeyPressed
        // TODO add your handling code here:
        if (evt.getKeyChar() == '\n') {
        
            //hide the search suggestor
            searchSuggestor.hide();

            //START SEARCH (SAME THING AS CLICKING BTNSEARCH)
            String search = txtSearch.getText();
            String brand = txtBrand.getText();

            //make sure of good input
            if (!search.equals("") && !brand.equals("")) {

                //check if already added
                boolean alreadyAdded = false;
                for (String s : searches) {

                    if (s.equals(search)) {

                        alreadyAdded = true;
                    }
                }

                if (!alreadyAdded) {

                    searches.add(search);

                    sortAlphabetically(searches);
                }

            //disable button and run search on new thread to allow for live updating to user
                btnSearch.setEnabled(false);
                new SiteSearch(search, brand).start();
            }

            //no input, output error
            else {

                lblError.setText("That didn't seem to work. Try again.");
            }
        }
    }//GEN-LAST:event_txtSearchKeyPressed
                              
    /**
     * Handles all searching
     */
    class SiteSearch extends Thread {
        
        private String search;
        private String brand;
        
        public SiteSearch (String search, String brand) {
            
            this.search = search;
            this.brand = brand;
        }
        
        public void run() {
            
            lblError.setText("");
            
            //for all sites
            for (int i = 0; i < defaultSites.size(); i ++) {

                //set progress bar
                progSearch.setMaximum(defaultSites.size());
                progSearch.setValue(i + 1);
                
                //open site
                String site = defaultSites.get(i);

                //inform user that site is being searched
                String[] fragments = site.split("\\.");

                String simplifiedLink = fragments[0] + "." + fragments[1] + "." + fragments[2].split("/")[0];
                lblSearch.setText("Searching " + simplifiedLink);

                //prepare search url
                String[] keys = search.split(", ");
                String searchKey = brand + "+" + search.replace(", ", "+");
                
                try {

                    //access search url
                    HtmlPage page = client.getPage(site.replace("SEARCHKEY", searchKey));

                    //get products from search tag
                    List<Product> newProducts = getProductTags(page, brand, keys);

                    //update on table
                    for (Product p : newProducts) {

                        productModel.addRow(new String[]{p.getName(), p.getLink(), p.getCost()});
                        tblProduct.setModel(productModel);
                        
                        products.addAll(newProducts);
                    }
                } 
                catch (IOException e) {e.printStackTrace();} 
                catch (NoSuchElementException nsee) {nsee.printStackTrace();}
            }

            //if nothing was outputted, likely an error or bad input
            if (productModel.getRowCount() == 0) {

                lblError.setText("That didn't seem to work. Try again.");
            }
            
            //reset progress bar and error message
            progSearch.setValue(0);
            lblSearch.setText("");
            
            btnSearch.setEnabled(true);
        }
    }
    
    /**
     * searches the search url for products
     * @param page
     * @param brand
     * @param keys
     * @return
     * @throws IOException
     * @throws NoSuchElementException 
     */
    public List<Product> getProductTags(HtmlPage page, String brand, String[] keys) throws IOException, NoSuchElementException {
        
        String initialLink = page.getBaseURI();
        boolean isProduct;
        
        List<Product> products = new ArrayList<>();
        
        String link = "";
        String cost = "";
        String name = "";
        
        List<DomElement> imgs = null;
        
        //wait for page to load
        try {
        
            Thread.sleep(5000);
        } catch (InterruptedException e) {System.out.println(e);}
        
        //get img items
        for (int i = 0; i < 20 && !(imgs != null && imgs.size() > 0); i ++) {

            imgs = page.getElementsByTagName("img");
        }
        
        //this must be checked, because this method is not always reliable due to html and javascript variability
        if (!(imgs != null && imgs.size() > 0)) {
            
            lblError.setText(initialLink + " didn't load properly");
        }
        
        //for each image
        for (DomElement de : imgs) {
            
            isProduct = true;
            
            //go up hierarchy levels sort of recursively until div is reached
            while (!de.getTagName().equals("a") || de.getTagName().equals("div")) {
             
                //if top-level page is reached, stop
                if (de.getParentNode().getClass().getSimpleName().equals("HtmlPage")) {
                    
                    break;
                }
                
                else {
                
                    de = (DomElement) (de.getParentNode());
                }
            }

            isProduct = false;
            //check if link contains any key words
            for (String s : keys) {

                //if so, flag as product
                if (de.getAttribute("href").toLowerCase().contains(s)) {

                    isProduct = true;
                }
            }
            
            //if product, get link
            if (isProduct) {

                link = de.getAttribute("href");
                
                //attempt to get cost from product link
                try {
                    
                    page = client.getPage(link);

                    Optional<DomElement> potentialCostTag = null;
                    for (int i = 0; i < 20 && !(potentialCostTag != null && potentialCostTag.isPresent()); i ++) {

                        //find tags with $, note this does not work with foreign currencies yet
                        potentialCostTag = StreamSupport
                                .stream(page.getElementsByTagName("span").spliterator(), false)
                                .filter(x -> x.getTextContent().contains("$"))
                                .findFirst();
                    }
                    
                    if (potentialCostTag != null && potentialCostTag.isPresent()) {

                        cost = potentialCostTag
                                .get()
                                .getTextContent();
                    }
                    
                    //get name from initial page
                    name = de.getTextContent();

                    //remove spaces from values
                    name = name.replaceAll("\\s+", "");
                    cost = cost.replaceAll("\\s+", "");

                    //default name if not found from brand and keys
                    if (name.equals("") || name == null) {

                        name = brand;

                        for (String s : keys) {

                            name += " " + s;
                        }
                    }

                    //check if already added
                    boolean alreadyAdded = false;
                    for (Product p : products) {

                        if (p.getLink().equals(link)) {

                            alreadyAdded = true;
                        }
                    }
                    
                    if (!alreadyAdded) {

                        //add to products list to be returned
                        products.add(new Product(link, cost, name));
                    }
                } catch (java.net.MalformedURLException mue) {System.out.println("SS 559 " + mue);;}
                    catch (IllegalStateException ise) {System.out.println("SS 478 " + ise);}
            }
        }
        
        return products;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SickoSearch.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SickoSearch.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SickoSearch.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SickoSearch.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SickoSearch().setVisible(true);
            }
        });
    }
    
    //main code from use syb0rg at https://stackoverflow.com/questions/14186955/create-a-autocompleting-textbox-in-java-with-a-dropdown-list
    //adjusted for our use
    class AutoSuggestor {

        private final JTextField textField;
        private final Window container;
        private JPanel suggestionsPanel;
        private JWindow autoSuggestionPopUpWindow;
        private String typedWord;
        private final ArrayList<String> dictionary = new ArrayList<>();
        private int currentIndexOfSpace, tW, tH;
        private DocumentListener documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent de) {
                checkForAndShowSuggestions();
            }

            @Override
            public void removeUpdate(DocumentEvent de) {
                checkForAndShowSuggestions();
            }

            @Override
            public void changedUpdate(DocumentEvent de) {
                checkForAndShowSuggestions();
            }
        };
        private final Color suggestionsTextColor;
        private final Color suggestionFocusedColor;

        public AutoSuggestor(JTextField textField, Window mainWindow, ArrayList<String> words, Color popUpBackground, Color textColor, Color suggestionFocusedColor, float opacity) {
            this.textField = textField;
            this.suggestionsTextColor = textColor;
            this.container = mainWindow;
            this.suggestionFocusedColor = suggestionFocusedColor;
            this.textField.getDocument().addDocumentListener(documentListener);

            setDictionary(words);

            typedWord = "";
            currentIndexOfSpace = 0;
            tW = 0;
            tH = 0;

            autoSuggestionPopUpWindow = new JWindow(mainWindow);
            autoSuggestionPopUpWindow.setOpacity(opacity);

            suggestionsPanel = new JPanel();
            suggestionsPanel.setLayout(new GridLayout(0, 1));
            suggestionsPanel.setBackground(popUpBackground);

            addKeyBindingToRequestFocusInPopUpWindow();
        }

        private void addKeyBindingToRequestFocusInPopUpWindow() {
            textField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true), "Down released");
            textField.getActionMap().put("Down released", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent ae) {//focuses the first label on popwindow
                    for (int i = 0; i < suggestionsPanel.getComponentCount(); i++) {
                        if (suggestionsPanel.getComponent(i) instanceof SuggestionLabel) {
                            ((SuggestionLabel) suggestionsPanel.getComponent(i)).setFocused(true);
                            autoSuggestionPopUpWindow.toFront();
                            autoSuggestionPopUpWindow.requestFocusInWindow();
                            suggestionsPanel.requestFocusInWindow();
                            suggestionsPanel.getComponent(i).requestFocusInWindow();
                            break;
                        }
                    }
                }
            });
            suggestionsPanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true), "Down released");
            suggestionsPanel.getActionMap().put("Down released", new AbstractAction() {
                int lastFocusableIndex = 0;

                @Override
                public void actionPerformed(ActionEvent ae) {//allows scrolling of labels in pop window (I know very hacky for now :))

                    ArrayList<SuggestionLabel> sls = getAddedSuggestionLabels();
                    int max = sls.size();

                    if (max > 1) {//more than 1 suggestion
                        for (int i = 0; i < max; i++) {
                            SuggestionLabel sl = sls.get(i);
                            if (sl.isFocused()) {
                                if (lastFocusableIndex == max - 1) {
                                    lastFocusableIndex = 0;
                                    sl.setFocused(false);
                                    autoSuggestionPopUpWindow.setVisible(false);
                                    setFocusToTextField();
                                    checkForAndShowSuggestions();//fire method as if document listener change occured and fired it

                                } else {
                                    sl.setFocused(false);
                                    lastFocusableIndex = i;
                                }
                            } else if (lastFocusableIndex <= i) {
                                if (i < max) {
                                    sl.setFocused(true);
                                    autoSuggestionPopUpWindow.toFront();
                                    autoSuggestionPopUpWindow.requestFocusInWindow();
                                    suggestionsPanel.requestFocusInWindow();
                                    suggestionsPanel.getComponent(i).requestFocusInWindow();
                                    lastFocusableIndex = i;
                                    break;
                                }
                            }
                        }
                    } else {//only a single suggestion was given
                        autoSuggestionPopUpWindow.setVisible(false);
                        setFocusToTextField();
                        checkForAndShowSuggestions();//fire method as if document listener change occured and fired it
                    }
                }
            });
        }

        private void setFocusToTextField() {
            container.toFront();
            container.requestFocusInWindow();
            textField.requestFocusInWindow();
        }

        public ArrayList<SuggestionLabel> getAddedSuggestionLabels() {
            ArrayList<SuggestionLabel> sls = new ArrayList<>();
            for (int i = 0; i < suggestionsPanel.getComponentCount(); i++) {
                if (suggestionsPanel.getComponent(i) instanceof SuggestionLabel) {
                    SuggestionLabel sl = (SuggestionLabel) suggestionsPanel.getComponent(i);
                    sls.add(sl);
                }
            }
            return sls;
        }

        private void checkForAndShowSuggestions() {
            typedWord = getCurrentlyTypedWord();

            suggestionsPanel.removeAll();//remove previos words/jlabels that were added

            //used to calcualte size of JWindow as new Jlabels are added
            tW = 0;
            tH = 0;

            boolean added = wordTyped(typedWord);

            if (!added) {
                if (autoSuggestionPopUpWindow.isVisible()) {
                    autoSuggestionPopUpWindow.setVisible(false);
                }
            } else {
                showPopUpWindow();
                setFocusToTextField();
            }
        }

        protected void addWordToSuggestions(String word) {
            SuggestionLabel suggestionLabel = new SuggestionLabel(word, suggestionFocusedColor, suggestionsTextColor, this);

            calculatePopUpWindowSize(suggestionLabel);

            suggestionsPanel.add(suggestionLabel);
        }

        public String getCurrentlyTypedWord() {//get newest word after last white spaceif any or the first word if no white spaces
            String text = textField.getText();
            String wordBeingTyped = "";
            if (text.contains(" ")) {
                int tmp = text.lastIndexOf(" ");
                if (tmp >= currentIndexOfSpace) {
                    currentIndexOfSpace = tmp;
                    wordBeingTyped = text.substring(text.lastIndexOf(" "));
                }
            } else {
                wordBeingTyped = text;
            }
            return wordBeingTyped.trim();
        }

        private void calculatePopUpWindowSize(JLabel label) {
            //so we can size the JWindow correctly
            if (tW < label.getPreferredSize().width) {
                tW = label.getPreferredSize().width;
            }
            tH += label.getPreferredSize().height;
        }

        private void showPopUpWindow() {
            autoSuggestionPopUpWindow.getContentPane().add(suggestionsPanel);
            autoSuggestionPopUpWindow.setMinimumSize(new Dimension(textField.getWidth(), 30));
            autoSuggestionPopUpWindow.setSize(tW, tH);
            autoSuggestionPopUpWindow.setVisible(true);

            int windowX = 0;
            int windowY = 0;

            windowX = container.getX() + textField.getX() + 5;
            if (suggestionsPanel.getHeight() > autoSuggestionPopUpWindow.getMinimumSize().height) {
                windowY = container.getY() + textField.getY() + textField.getHeight() + autoSuggestionPopUpWindow.getMinimumSize().height;
            } else {
                windowY = container.getY() + textField.getY() + textField.getHeight() + autoSuggestionPopUpWindow.getHeight();
            }

            autoSuggestionPopUpWindow.setLocation(windowX, windowY);
            autoSuggestionPopUpWindow.setMinimumSize(new Dimension(textField.getWidth(), 30));
            autoSuggestionPopUpWindow.revalidate();
            autoSuggestionPopUpWindow.repaint();

        }

        public void setDictionary(ArrayList<String> words) {
            dictionary.clear();
            if (words == null) {
                return;//so we can call constructor with null value for dictionary without exception thrown
            }
            for (String word : words) {
                dictionary.add(word);
            }
        }

        public JWindow getAutoSuggestionPopUpWindow() {
            return autoSuggestionPopUpWindow;
        }

        public Window getContainer() {
            return container;
        }

        public JTextField getTextField() {
            return textField;
        }

        public void addToDictionary(String word) {
            dictionary.add(word);
        }

        //custom code
        boolean wordTyped(String typedWord) {
            
            if (!autoSuggestionPopUpWindow.isVisible()) {
                
                autoSuggestionPopUpWindow.setVisible(true);
            }

            if (typedWord.isEmpty()) {
                return false;
            }

            boolean suggestionAdded = false;

            //custom code
            for (String word : dictionary) {
                boolean fullymatches = true;
                for (int i = 0; i < typedWord.length(); i++) {
                    if (i < word.length() && !typedWord.toLowerCase().startsWith(String.valueOf(word.toLowerCase().charAt(i)), i)) {//check for match
                        fullymatches = false;
                        break;
                    }
                }
                if (fullymatches) {
                    addWordToSuggestions(word);
                    suggestionAdded = true;
                }
            }
            return suggestionAdded;
        }
        
        //custom code
        public void hide() {
            
            autoSuggestionPopUpWindow.setVisible(false);
        }
    }

    class SuggestionLabel extends JLabel {

        private boolean focused = false;
        private final JWindow autoSuggestionsPopUpWindow;
        private final JTextField textField;
        private final AutoSuggestor autoSuggestor;
        private Color suggestionsTextColor, suggestionBorderColor;

        public SuggestionLabel(String string, final Color borderColor, Color suggestionsTextColor, AutoSuggestor autoSuggestor) {
            super(string);

            this.suggestionsTextColor = suggestionsTextColor;
            this.autoSuggestor = autoSuggestor;
            this.textField = autoSuggestor.getTextField();
            this.suggestionBorderColor = borderColor;
            this.autoSuggestionsPopUpWindow = autoSuggestor.getAutoSuggestionPopUpWindow();

            initComponent();
        }

        private void initComponent() {
            setFocusable(true);
            setForeground(suggestionsTextColor);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent me) {
                    super.mouseClicked(me);

                    replaceWithSuggestedText();

                    autoSuggestionsPopUpWindow.setVisible(false);
                }
            });

            getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), "Enter released");
            getActionMap().put("Enter released", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    replaceWithSuggestedText();
                    autoSuggestionsPopUpWindow.setVisible(false);
                }
            });
        }

        public void setFocused(boolean focused) {
            if (focused) {
                setBorder(new LineBorder(suggestionBorderColor));
            } else {
                setBorder(null);
            }
            repaint();
            this.focused = focused;
        }

        public boolean isFocused() {
            return focused;
        }

        private void replaceWithSuggestedText() {
            String suggestedWord = getText();
            String text = textField.getText();
            String typedWord = autoSuggestor.getCurrentlyTypedWord();
            String t = text.substring(0, text.lastIndexOf(typedWord));
            String tmp = t + text.substring(text.lastIndexOf(typedWord)).replace(typedWord, suggestedWord);
            textField.setText(tmp);
        }
    }
    
    /**
     * custom models to allow for buttons
     */
    public static class ProductTableModel extends DefaultTableModel {
        private static final long serialVersionUID = 1L;
        private static final String[] COLUMN_NAMES = new String[] {"Product", "Link", "Price", "Favourite", "Delete"};
        private static final Class<?>[] COLUMN_TYPES = new Class<?>[] {String.class, String.class, String.class, JButton.class,  JButton.class};

        @Override public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override public String getColumnName(int columnIndex) {
            return COLUMN_NAMES[columnIndex];
        }

        @Override public Class<?> getColumnClass(int columnIndex) {
            return COLUMN_TYPES[columnIndex];
        }

        @Override public Object getValueAt(final int rowIndex, final int columnIndex) {
            switch (columnIndex) {
                case 0: return super.getValueAt(rowIndex, columnIndex);
                case 1: return super.getValueAt(rowIndex, columnIndex);
                case 2: return super.getValueAt(rowIndex, columnIndex);
                    //listener for clicking
                case 3: final JButton button1 = new JButton(COLUMN_NAMES[columnIndex]);
                        button1.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent arg0) {
                                
                                Product p = products.get(rowIndex);
                                
                                //delete from product table and add to favourites
                                productModel.removeRow(rowIndex);
                                favModel.addRow(new String[]{p.getName(), p.getLink(), p.getCost()});
                                
                                favProducts.add(p);
                                products.remove(rowIndex);
                            }
                        });
                        return button1;
                case 4: final JButton button2 = new JButton(COLUMN_NAMES[columnIndex]);
                        button2.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent arg0) {
                                
                                //delete from table
                                productModel.removeRow(rowIndex);
                                products.remove(rowIndex);
                            }
                        });
                        return button2;
                default: return "Error";
            }
        }   
    }
    
    public static class FavouriteTableModel extends DefaultTableModel {
        private static final long serialVersionUID = 1L;
        private static final String[] COLUMN_NAMES = new String[] {"Product", "Link", "Price", "Delete"};
        private static final Class<?>[] COLUMN_TYPES = new Class<?>[] {String.class, String.class, String.class,  JButton.class};

        @Override public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override public String getColumnName(int columnIndex) {
            return COLUMN_NAMES[columnIndex];
        }

        @Override public Class<?> getColumnClass(int columnIndex) {
            return COLUMN_TYPES[columnIndex];
        }

        @Override public Object getValueAt(final int rowIndex, final int columnIndex) {
            switch (columnIndex) {
                case 0: return super.getValueAt(rowIndex, columnIndex);
                case 1: return super.getValueAt(rowIndex, columnIndex);
                case 2: return super.getValueAt(rowIndex, columnIndex);
                    //listener for clicking
                case 3: final JButton button1 = new JButton(COLUMN_NAMES[columnIndex]);
                        button1.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent arg0) {
                                
                                //delete from table
                                favModel.removeRow(rowIndex);
                                favProducts.remove(rowIndex);
                            }
                        });
                        return button1;
                default: return "Error";
            }
        }   
    }
    
    /**
     * basic handling of clicking buttons
     */
    private static class JTableButtonMouseListener extends MouseAdapter {
        private final JTable table;
        
        public JTableButtonMouseListener(JTable table) {
            this.table = table;
        }

        public void mouseClicked(MouseEvent e) {
            int column = table.getColumnModel().getColumnIndexAtX(e.getX()); // get the coloum of the button
            int row    = e.getY()/table.getRowHeight(); //get the row of the button

                    /*Checking the row or column is valid or not*/
            if (row < table.getRowCount() && row >= 0 && column < table.getColumnCount() && column >= 0) {
                Object value = table.getValueAt(row, column);
                if (value instanceof JButton) {
                    /*perform a click event*/
                    ((JButton)value).doClick();
                }
            }
        }
    }

    /**
     * basic rendering of buttons
     */
    private static class JTableButtonRenderer implements TableCellRenderer {        
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JButton button = (JButton)value;
            return button;  
        }
    }
    
    /**
     * basic linear sorting
     * @param list 
     */
    public void sortAlphabetically(List<String> list) {
        
        boolean sorted = false;
        
        while (!sorted) {
            
            sorted = true;
            
            for (int i = 0; i < list.size(); i ++) {
                
                if (i != list.size() - 1) {
                    
                    String s1 = list.get(i);
                    String s2 = list.get(i + 1);
                    
                    //alphabetically compare two strings
                    switch (alphabeticallyCompare(s1.toLowerCase(), s2.toLowerCase(), 0)) {
                        
                        //1 mean s1 should be after s2, so switch
                        case 1:
                            list.set(i, s2);
                            list.set(i + 1, s1);
                            
                            sorted = false;
                            break;
                    }
                }
            }
        }
    }
    
    /**
     * recursively compare two strings alphabetically
     * @param s1
     * @param s2
     * @param i
     * @return 0 for s1 before s2, 1 for opposite
     */
    public int alphabeticallyCompare(String s1, String s2, int i) {
        
        //if beyond string length, return shortest string
        if (i >= s1.length()) {
            
            return 0;
        }
        
        else if (i >= s2.length()) {
            
            return 1;
        }
        
        //compare char values and return respective string
        else if (s1.charAt(i) > s2.charAt(i)) {
            
            return 1;
        }
        
        else if (s1.charAt(i) < s2.charAt(i)) {
            
            return 0;
        }
        
        //if the same, go on to next string
        else {
            
            return alphabeticallyCompare(s1, s2, i + 1);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSearch;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblError;
    private javax.swing.JLabel lblFav;
    private javax.swing.JLabel lblOut;
    private javax.swing.JLabel lblPrompt;
    private javax.swing.JLabel lblPrompt1;
    private javax.swing.JLabel lblSearch;
    private javax.swing.JProgressBar progSearch;
    private javax.swing.JTable tblFav;
    private javax.swing.JTable tblProduct;
    private javax.swing.JTextField txtBrand;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
