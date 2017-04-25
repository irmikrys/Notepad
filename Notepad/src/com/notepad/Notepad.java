package com.notepad;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.StringTokenizer;

/**
 * Created on 26.11.2016.
 */
public class Notepad extends JPanel {


    private JTextPane textPane;
    private JScrollPane scrollPane;
    private JPanel labelsPanel = new JPanel();
    private JLabel label1, label2;
    private JButton buttonNewFile, buttonOpenFile, buttonSaveFile, buttonSaveAs;
    private JButton buttonCopy, buttonPaste, buttonCut;
    private JButton buttonBold, buttonItalic;
    private Action newAct, openAct, saveAct, saveAsAct;
    private Action copyAct, pasteAct, cutAct, boldAct, italicAct;
    private boolean fileSaved;
    private int cursor;

    private JFrame frame;
    private JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
    private String currentFile = JOptionPane.showInputDialog("File Name:");;
    private boolean changed = false;

    private int chars, words, lines, position;

    /****************** Konstruktor ********************/
    public Notepad() {
        super(new BorderLayout());

        setPreferredSize(new Dimension(800, 600));
        setMinimumSize(new Dimension(500, 550));

        textPane = new JTextPane();
        textPane.setFont(new Font("Trebuchet MS", Font.PLAIN, 14));
        textPane.setEditable(true);
        textPane.addKeyListener(keyPr);

        scrollPane = new JScrollPane(textPane,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);

        //Stworzenie akcji do menu i paska narzedzi

        newAct = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new NewAct()).start();
            }
        };

        openAct = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new OpenAct()).start();
            }
        };

        saveAct = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new SaveAct()).start();
            }
        };

        saveAct.setEnabled(false);

        saveAsAct = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new SaveAsAct()).start();
            }
        };

        saveAsAct.setEnabled(false);

        boldAct = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int info;
                Font font = textPane.getFont();
                if (buttonBold.isSelected()) {
                    if (font.getStyle() == Font.BOLD)
                        info = Font.PLAIN;
                    else
                        info = Font.ITALIC;
                } else {
                    if (font.getStyle() == Font.PLAIN)
                        info = Font.BOLD;
                    else
                        info = Font.BOLD + Font.ITALIC;
                }
                new Thread(new BoldAct(font.getName(), font.getSize(), info, buttonBold, textPane)).start();
            }
        };

        italicAct = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int info;
                Font font = textPane.getFont();
                if (buttonItalic.isSelected()) {
                    if (font.getStyle() == Font.ITALIC)
                        info = Font.PLAIN;
                    else
                        info = Font.BOLD;
                } else {
                    if (font.getStyle() == Font.PLAIN)
                        info = Font.ITALIC;
                    else
                        info = Font.ITALIC + Font.BOLD;
                }
                new Thread(new ItalicAct(font.getName(), font.getSize(), info, buttonItalic, textPane)).start();
            }
        };

        /*
        newAct =   new NewAct( "New", "New File");
        openAct = new OpenAct("Open...", "Open File");
        saveAct =  new SaveAct( "Save", "Save File");
        saveAsAct =  new SaveAsAct( "Save As...", "Save File As...");

        saveAct.setEnabled(false);
        saveAsAct.setEnabled(false);

        boldAct =  new BoldAct( "Bold", "Bold Text");
        italicAct =  new ItalicAct( "Italic", "Italic Text");
        underlineAct =  new UnderlineAct( "Underline", "Underline Text");
        */
        fileSaved = false;

        //Mapowanie
        ActionMap m = textPane.getActionMap();
        cutAct = m.get(DefaultEditorKit.cutAction);
        copyAct = m.get(DefaultEditorKit.copyAction);
        pasteAct = m.get(DefaultEditorKit.pasteAction);

        textPane.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                cursor = e.getDot();
                //SwingUtilities.invokeLater(new CursorPosition());
                new Thread(new CursorPosition()).start();
            }
        });


        //Ustawianie labela do zliczania znakow, slow, linii
        add(labelsPanel, BorderLayout.SOUTH);
        label1 = new JLabel("Chars: " + chars + "    Words: " + words + "    Lines: " + lines);
        label2 = new JLabel("   Cursor Position: " + position);
        labelsPanel.add(label1);
        labelsPanel.add(label2);

        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Utworzenie i ustawienie menu i zawartosci panelu
        frame.setJMenuBar(this.createMenuBar());
        this.createToolBar();
        //this.setOpaque(true);
        frame.setContentPane(this);
        frame.setTitle(currentFile);

        //Wyswietlenie okna
        frame.pack();
        frame.setVisible(true);
    }

    /**************** Zwraca ikone ****************/
    protected static ImageIcon icon(String imageName) {
        String imgLocation = imageName + ".png";
        java.net.URL imageURL = Notepad.class.getResource(imgLocation);
        return new ImageIcon(imageURL);
    }

    /************ Tworzy Pasek Menu ******************/
    public JMenuBar createMenuBar() {
        JMenuItem menuItem = null;

        //Nowy pasek menu
        JMenuBar menuBar = new JMenuBar();

        //Pierwsze glowne menu
        JMenu mainMenu = new JMenu("File");

        //Drugie menu edycji
        JMenu editMenu = new JMenu("Edit");

        Action[] actions = {newAct, openAct, saveAct, saveAsAct};
        for (int i = 0; i < actions.length; i++) {
            menuItem = new JMenuItem(actions[i]);
            menuItem.setIcon(null);
            switch(i) {
                case 0: menuItem.setText("New File"); break;
                case 1: menuItem.setText("Open File..."); break;
                case 2: menuItem.setText("Save File"); break;
                case 3: menuItem.setText("Save File As..."); break;
            }
            mainMenu.add(menuItem);
        }

        Action[] edits = {copyAct, pasteAct, cutAct, boldAct, italicAct/*, underlineAct*/};
        for (int i = 0; i < edits.length; i++) {
            menuItem = new JMenuItem(edits[i]);
            menuItem.setIcon(null);
            switch(i) {
                case 0: menuItem.setText("Copy"); break;
                case 1: menuItem.setText("Paste"); break;
                case 2: menuItem.setText("Cut"); break;
                case 3: menuItem.setText("Bold"); break;
                case 4: menuItem.setText("Italic"); break;
                //case 5: menuItem.setText("Underline"); break;
            }
            editMenu.add(menuItem);
        }

        //Dodanie menu do paska menu
        menuBar.add(mainMenu);
        menuBar.add(editMenu);
        return menuBar;
    }

    /************* Tworzenie ikon na pasku narzedzi u gory ***************/
    public void createToolBar() {
        JButton button = null;

        //Pasek narzedzi
        JToolBar toolBar = new JToolBar();
        add(toolBar, BorderLayout.PAGE_START);

        //Przycisk new file
        buttonNewFile = new JButton(newAct);
        buttonNewFile.setIcon(icon("New"));
        buttonNewFile.setText(null);
        toolBar.add(buttonNewFile);

        //Przycisk open file
        buttonOpenFile = new JButton(openAct);
        buttonOpenFile.setIcon(icon("Open"));
        buttonOpenFile.setText(null);
        toolBar.add(buttonOpenFile);

        //Przycisk save file
        buttonSaveFile = new JButton(saveAct);
        buttonSaveFile.setIcon(icon("Save"));
        buttonSaveFile.setText(null);
        toolBar.add(buttonSaveFile);

        //Przycisk save file as
        buttonSaveAs = new JButton(saveAsAct);
        buttonSaveAs.setIcon(icon("SaveAs"));
        buttonSaveAs.setText(null);
        toolBar.add(buttonSaveAs);

        //////////////////////////////

        //Przycisk copy
        buttonCopy = new JButton(copyAct);
        buttonCopy.setIcon(icon("Copy"));
        buttonCopy.setText(null);
        toolBar.add(buttonCopy);

        //Przycisk paste
        buttonPaste = new JButton(pasteAct);
        buttonPaste.setIcon(icon("Paste"));
        buttonPaste.setText(null);
        toolBar.add(buttonPaste);

        //Przycisk cut
        buttonCut = new JButton(cutAct);
        buttonCut.setIcon(icon("Cut"));
        buttonCut.setText(null);
        toolBar.add(buttonCut);

        //Przycisk bold
        buttonBold = new JButton(boldAct);
        buttonBold.setIcon(icon("Bold"));
        buttonBold.setText(null);
        toolBar.add(buttonBold);

        //Przycisk italic
        buttonItalic = new JButton(italicAct);
        buttonItalic.setIcon(icon("Italic"));
        buttonItalic.setText(null);
        toolBar.add(buttonItalic);

    }

    /********************** Akcje glowne *********************/

    public class NewAct implements Runnable {
        public void run() {
            save();
            textPane.setText(null);
            String current = JOptionPane.showInputDialog("File Name:");
            currentFile = current;
            //currentFile = "Untitled";
            frame.setTitle(currentFile);
            changed = false;
            saveAct.setEnabled(false);
            saveAsAct.setEnabled(false);
            //buttonBold.setSelected(false);
            //buttonItalic.setSelected(false);
        }
    }

    public class OpenAct implements Runnable {
        public void run(){
            save();
            //textPane.setFont(new Font(textPane.getFont().getName(), textPane.getFont().getSize(), Font.PLAIN));
            //buttonBold.setSelected(false);
            //buttonItalic.setSelected(false);
            if(fileChooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION) {
                readInFile(fileChooser.getSelectedFile().getAbsolutePath());
            }
            saveAsAct.setEnabled(true);
        }
    }

    public class SaveAct implements Runnable {
        public void run(){
            if(!currentFile.equals("Untitled"))
                saveFile(currentFile);
            else
                saveFileAs();
        }
    }

    public class SaveAsAct implements Runnable {
        public void run(){
            saveFileAs();
        }
    }

    /******************* Akcje do edytowania *********************/

    public class BoldAct implements Runnable {
        private String name;
        private int info;
        private int size;
        private JButton b;
        private JTextPane textArea;
        BoldAct(String name, int size, int info, JButton b, JTextPane textArea) {
            this.name = name;
            this.info = info;
            this.size = size;
            this.b = b;
            this.textArea = textArea;
        }
        @Override
        public void run() {
            if (b.isSelected())
                b.setSelected(false);
            else
                b.setSelected(true);

            textArea.setFont(new Font(name, info, size));
        }

    }

    public class ItalicAct implements Runnable {
        private String name;
        private int info;
        private int size;
        private JButton b;
        private JTextPane textArea;
        ItalicAct(String name, int size, int info, JButton b, JTextPane textArea) {
            this.name = name;
            this.info = info;
            this.size = size;
            this.b = b;
            this.textArea = textArea;
        }
        @Override
        public void run() {
            if (b.isSelected())
                b.setSelected(false);
            else
                b.setSelected(true);

            textArea.setFont(new Font(name, info, size));
        }
    }

    public class CursorPosition implements Runnable{
        @Override
        public void run() {
            cursor = textPane.getCaretPosition();
            label2.setText("    Cursor Position: " + cursor);
        }
    }


    /**************** Potrzebne metody do akcji ******************/
    private void saveFileAs() {
        if(fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
            fileSaved = false;
            saveFile(fileChooser.getSelectedFile().getAbsolutePath());
    }

    private void save() {
        if(changed) {
            if(JOptionPane.showConfirmDialog(this, "Would you like to save " + currentFile + " ?","Save",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                saveFile(currentFile);
        }
    }

    private void readInFile(String fileName) {
            try (BufferedReader brFile = new BufferedReader(new FileReader(fileName))) {
                String l;
                StringBuilder sb = new StringBuilder();
                while ((l = brFile.readLine()) != null) {
                    //l = brFile.readLine();
                    sb.append(l + "\n");
                }
                textPane.setText(sb.toString());
                currentFile = fileName;
                frame.setTitle(currentFile);
                changed = false;
                fileSaved = true;
            }
            catch(IOException e) {

            }
    }

    private void saveFile(String fileName) {


        if(!fileSaved){
            fileName = fileName + ".txt";
        }

        try (BufferedWriter fileOut = new BufferedWriter(new FileWriter(fileName))) {
            textPane.write(fileOut);
            currentFile = fileName;
            frame.setTitle(currentFile);
            changed = false;
            saveAct.setEnabled(false);
            fileSaved = true;
            fileOut.close();
        }
        catch(IOException e) {
        }
    }


    /************ Czy zostalo cos napisane *********************/
    private KeyListener keyPr = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            changed = true;
            String s = textPane.getText();
            StringTokenizer st = new StringTokenizer(s);
            chars = s.length();
            words = st.countTokens();
            lines = s.split("\n").length;
            label1.setText("Chars: " + chars + "    Words: " + words + "    Lines: " + lines);
            saveAct.setEnabled(true);
            saveAsAct.setEnabled(true);
        }
    };

    /*****************Stworzenie i wyswietlenie GUI************/
    public static void main(String[] args) {
        //Nowy wątek
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Notepad();
            }
        });
    }

}

