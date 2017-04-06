package me.tnsi.molesim;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.logging.*;

import com.electronwill.toml.*;

public class MoleView extends JFrame implements ActionListener {

    private int moleCount;
    private int moleLimit;
    private int gridSize;
    private int popTime;
    private int hideTime;
    private int molesLeft;

    private Semaphore moleLimiter;
    private Thread[][] moles;

    private JPanel gridPanel;
    private JPanel infoPanel;

    private JLabel infoLabel;

    private JButton[][] btnGrid;

    private Image imgMoleDirt;
    private Image imgMoleHole;
    private Image imgMolePop;

    private final static Logger LOGGER = Logger.getLogger(MoleView.class.getName());


    /*
     * Functionality occuring in the constructor
     */
    public MoleView() {
        // Loads configuration from a config.toml file
        try {
            File file = new File("config.toml");
            Map<String, Object> readData = Toml.read(file);
            moleCount = (Integer) readData.get("mole_count");
            moleLimit = (Integer) readData.get("mole_limit");
            gridSize = (Integer) readData.get("grid_size");
            popTime = (Integer) readData.get("pop_time");
            hideTime = (Integer) readData.get("hide_time");
            molesLeft = moleCount;
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE, "Configuration error! ", e);
            System.exit(1);
        }

        // Load Assets
        try {
            imgMoleDirt = ImageIO.read(new File("mole_dirt.png"));
            imgMoleHole = ImageIO.read(new File("mole_hole.png"));
            imgMolePop = ImageIO.read(new File("mole_pop.png"));
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to load assets.", e);
            System.exit(1);
        }

        // Apple Specific
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.app.menu.about.name", "Mole Sim");

        // Frame Creation
        JFrame frame = new JFrame("Mole Sim");
        frame.setPreferredSize(new Dimension(640, 480));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Setup Grid Panel
        gridPanel = new JPanel(new GridLayout(gridSize, gridSize));

        // Init Buttons
        btnGrid = new JButton[gridSize][gridSize];
        for(int i = 0; i < gridSize; i++) {
            for(int j = 0; j < gridSize; j++) {
                btnGrid[i][j] = new JButton();
                btnGrid[i][j].setIcon(new ImageIcon(imgMoleDirt));
                btnGrid[i][j].addActionListener(this);
                btnGrid[i][j].setName("dirt");
                gridPanel.add(btnGrid[i][j]);
            }
        }

        infoPanel = new JPanel();
        infoLabel = new JLabel("Click a Mole to Whack it.");
        infoPanel.add(infoLabel);


        frame.add(gridPanel, BorderLayout.CENTER);
        frame.add(infoPanel, BorderLayout.SOUTH);
        frame.pack();
        frame.revalidate();
        frame.setVisible(true);

        // Create Moles
        moleLimiter = new Semaphore(moleLimit);
        moles = new Thread[gridSize][gridSize];

        /*
         * Spawns moles at random locations that are not occupied by a hole or pop.
         */
        Random rng = new Random();
        while(true) {
            if(molesLeft <= 0) {
                break;
            }
            int rndX = rng.nextInt((gridSize - 1) + 1);
            int rndY = rng.nextInt((gridSize - 1) + 1);

            if(btnGrid[rndX][rndY].getName().equals("dirt")) {
                // Can Fill Dirt
                molesLeft--;
                btnGrid[rndX][rndY].setName("hole");
                btnGrid[rndX][rndY].setIcon(new ImageIcon(imgMoleHole));
                btnGrid[rndX][rndY].revalidate();
                moles[rndX][rndY] = new Thread(new Mole(btnGrid[rndX][rndY], popTime, hideTime, moleLimiter));
                moles[rndX][rndY].start();
            }

        }

    }

    public void actionPerformed(ActionEvent event) {

        JButton btnEvent = (JButton) event.getSource();
        if(btnEvent.getName().equals("dirt")) { }
        if(btnEvent.getName().equals("hole")) { }
        /*
         * This condition occurs when the Mole is 'popped'.
         */
        if(btnEvent.getName().equals("pop")) {
            for(int i = 0; i < gridSize; i++) {
                for(int j = 0; j < gridSize; j++) {
                    if(btnEvent == btnGrid[i][j]) {
                        // This matching condition gets the right i and j indices
                        btnEvent.setName("dirt");
                        btnEvent.setIcon(new ImageIcon(imgMoleDirt));
                        btnEvent.revalidate();
                        moles[i][j].interrupt();
                        // TODO Generate a new mole, for neverending sim
                    }
                }
            }

        }
    }
}
