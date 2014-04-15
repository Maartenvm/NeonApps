package nl.esciencecenter.neon.examples.viaAppia;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import nl.esciencecenter.neon.swing.GoggleSwing;

/* Copyright 2013 Netherlands eScience Center
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Example {@link ESightInterfacePanel} implementation. Currently holds no more
 * than a logo, but could be used for all kinds of swing interface elements.
 * 
 * @author Maarten van Meersbergen <m.van.meersbergen@esciencecenter.nl>
 * 
 */
public class ViaAppiaInterfaceWindow extends JPanel {
    // A serialVersionUID is 'needed' because we extend JPanel.
    private static final long      serialVersionUID = 1L;

    // Global (singleton) settings instance.
    private final ViaAppiaSettings settings         = ViaAppiaSettings.getInstance();

    /**
     * Basic constructor for ESightExampleInterfaceWindow.
     */
    public ViaAppiaInterfaceWindow() {
        // Set the swing layout type for this JPanel.
        setLayout(new BorderLayout(0, 0));

        // Make the menu bar
        final JMenuBar menuBar1 = new JMenuBar();
        menuBar1.setLayout(new BoxLayout(menuBar1, BoxLayout.X_AXIS));

        final JMenu file = new JMenu("File");
        final JMenuItem open = new JMenuItem("Open");
        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                final File[] files = openFile();
                handleFiles(files);
            }
        });
        file.add(open);
        menuBar1.add(file);
        menuBar1.add(Box.createHorizontalGlue());

        // Make a menu bar
        final JMenuBar menuBar2 = new JMenuBar();

        // Create a swing-enabled image.
        ImageIcon nlescIcon = GoggleSwing.createResizedImageIcon("images/ESCIENCE_logo.jpg", "eScienceCenter Logo",
                200, 20);
        JLabel nlesclogo = new JLabel(nlescIcon);

        // Add the image to the menu bar, but create some glue around it so it
        // doesn't automatically resize the window to minimum size.
        menuBar2.add(Box.createHorizontalGlue());
        menuBar2.add(nlesclogo);
        menuBar2.add(Box.createHorizontalGlue());

        // Add the menubar to a container, so we can apply a boxlayout. (not
        // strictly necessary, but a demonstration of what swing could do)
        Container menuContainer = new Container();
        menuContainer.setLayout(new BoxLayout(menuContainer, BoxLayout.Y_AXIS));
        menuContainer.add(menuBar1);
        menuContainer.add(menuBar2);

        // Add the menu container to the JPanel
        add(menuContainer, BorderLayout.NORTH);
        setVisible(true);
    }

    private File[] openFile() {
        final JFileChooser fileChooser = new JFileChooser("D:/Via Appia");

        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(true);
        final int result = fileChooser.showOpenDialog(this);

        // user clicked Cancel button on dialog
        if (result == JFileChooser.CANCEL_OPTION) {
            return null;
        } else {
            return fileChooser.getSelectedFiles();
        }
    }

    private void handleFiles(File[] files) {
        boolean accept = true;
        for (File thisFile : files) {
            if (!isAcceptableFile(thisFile, new String[] { ".las" })) {
                accept = false;
            }
        }

        if (accept) {
            settings.setFiles(files);
        } else {
            final JOptionPane pane = new JOptionPane();
            pane.setMessage("Tried to open invalid file type.");
            final JDialog dialog = pane.createDialog("Alert");
            dialog.setVisible(true);
        }
    }

    /**
     * Check whether the file's extension is acceptable.
     * 
     * @param file
     *            The file to check.
     * @param accExts
     *            The list of acceptable extensions.
     * @return True if the file's extension is present in the list of acceptable
     *         extensions.
     */
    public static boolean isAcceptableFile(File file, String[] accExts) {
        final String path = file.getParent();
        final String name = file.getName();
        final String fullPath = path + name;
        final String[] ext = fullPath.split("[.]");

        boolean result = false;
        for (int i = 0; i < accExts.length; i++) {
            if (ext[ext.length - 1].compareTo(accExts[i]) != 0) {
                result = true;
            }
        }

        return result;
    }
}
