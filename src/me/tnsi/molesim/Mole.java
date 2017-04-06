package me.tnsi.molesim;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Mole Thread, Swaps the Moles between Hole and Pop
 */
public class Mole implements Runnable {

    private final static Logger LOGGER = Logger.getLogger(Mole.class.getName());

    private JButton btnEntity;
    private int popTime;
    private int hideTime;
    private Semaphore moleLimit;

    private Image imgMoleDirt;
    private Image imgMoleHole;
    private Image imgMolePop;

    public Mole(JButton btnImport, int pTime, int hTime, Semaphore limiter) {
        this.btnEntity = btnImport;
        this.popTime = pTime;
        this.hideTime = hTime;
        this.moleLimit = limiter;

        try {
            imgMoleDirt = ImageIO.read(new File("mole_dirt.png"));
            imgMoleHole = ImageIO.read(new File("mole_hole.png"));
            imgMolePop = ImageIO.read(new File("mole_pop.png"));
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to load assets.", e);
            System.exit(1);
        }
    }

    public void run() {
        while(true) {
            if(btnEntity.getName().equals("hole")) {
                // This is a hole
                if(moleLimit.tryAcquire()) {
                    try {
                        Thread.sleep(hideTime);
                        btnEntity.setName("pop");
                        btnEntity.setIcon(new ImageIcon(imgMolePop));
                        btnEntity.revalidate();
                        continue;
                    } catch (InterruptedException e) {
                        System.out.println("Interrupted during pop_time");
                        btnEntity.setName("pop");
                        btnEntity.setIcon(new ImageIcon(imgMolePop));
                        btnEntity.revalidate();
                        continue;
                    }
                }
            }
            if(btnEntity.getName().equals("pop")) {
                try {
                    Thread.sleep(popTime);
                    btnEntity.setName("hole");
                    btnEntity.setIcon(new ImageIcon(imgMoleHole));
                    btnEntity.revalidate();
                    moleLimit.release();
                    continue;
                } catch(InterruptedException e) {
                    // If the location is set to dirt externally, this mole is done
                    if(btnEntity.getName().equals("dirt")) {
                        moleLimit.release();
                        break;
                    }
                    btnEntity.setName("hole");
                    btnEntity.setIcon(new ImageIcon(imgMoleHole));
                    btnEntity.revalidate();
                    System.out.println("Holed!");
                    moleLimit.release();
                    continue;
                }
            }
        }
    }
}
