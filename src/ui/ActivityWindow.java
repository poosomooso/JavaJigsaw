package ui;

import reflection.Problem;
import snippets.ArraySnippets;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.IOException;
import java.util.ArrayList;

// adapted from the TestDnD class
public class ActivityWindow extends JPanel{

    private JList<TransferableSnippet> list;
    private ArrayList<String> code;

    public ActivityWindow(Problem p) {
        code = new ArrayList<>();
        setLayout(new BorderLayout());
        list = new JList<>();
        DefaultListModel<TransferableSnippet> model = new DefaultListModel<>();
        for (String s : ArraySnippets.LEVEL1) {
            model.addElement(new TransferableSnippet(s));
        }
        list.setModel(model);
        setUpDnD(p);
    }

    private void setUpDnD(Problem p) {
        JLabel desc = new JLabel(p.getProblemDescription());
        add(desc, BorderLayout.NORTH);

        // snippets
        add(new JScrollPane(list), BorderLayout.WEST);

        DragGestureRecognizer dgr = DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
                list,
                DnDConstants.ACTION_COPY,
                new DragGestureHandler(list));

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;

        // method signature
        JLabel sig = new JLabel(p.getMethodSignature());
        gbc.gridy = 0;
        panel.add(sig, gbc);

        JLabel openBracket = new JLabel("{");
        gbc.gridy++;
        panel.add(openBracket, gbc);


        // dnd panel
        JPanel dndPanel = new JPanel(new GridBagLayout());
        gbc.gridy++;
        gbc.insets = new Insets(0, 40, 0, 0);
        panel.add(dndPanel, gbc);

        JLabel closeBracket = new JLabel("}");
        gbc.gridy++;
        gbc.insets = new Insets(0,0,0,0);
        panel.add(closeBracket, gbc);

        JButton submit = new JButton("Submit");
        submit.addActionListener(new SubmitActionListener(p, code));
        gbc.gridy++;
        panel.add(submit, gbc);

        JButton restart = new JButton("Restart");
        restart.addActionListener(new RestartActionListener(dndPanel, code));
        gbc.gridy++;
        panel.add(restart, gbc);

        add(panel);

        DropTarget dt = new DropTarget(
                panel,
                DnDConstants.ACTION_COPY,
                new DropTargetHandler(dndPanel),
                true);
    }

    protected class DragGestureHandler implements DragGestureListener {

        private JList<TransferableSnippet> list;

        public DragGestureHandler(JList<TransferableSnippet> list) {
            this.list = list;
        }

        @Override
        public void dragGestureRecognized(DragGestureEvent dge) {
            TransferableSnippet selectedValue = list.getSelectedValue();
            DragSource ds = dge.getDragSource();
            ds.startDrag(
                    dge,
                    null,
                    selectedValue,
                    new DragSourceHandler());


        }

    }

    protected class DragSourceHandler implements DragSourceListener {

        public void dragEnter(DragSourceDragEvent dsde) {
        }

        public void dragOver(DragSourceDragEvent dsde) {
        }

        public void dropActionChanged(DragSourceDragEvent dsde) {
        }

        public void dragExit(DragSourceEvent dse) {
        }

        public void dragDropEnd(DragSourceDropEvent dsde) {

            System.out.println("Drag ended...");

        }

    }

    protected class DropTargetHandler implements DropTargetListener {

        private JPanel panel;

        public DropTargetHandler(JPanel panel) {
            this.panel = panel;
        }

        public void dragEnter(DropTargetDragEvent dtde) {
            if (dtde.getTransferable().isDataFlavorSupported(TransferableSnippet.DATA_FLAVOR)) {
                System.out.println("Accept...");
                dtde.acceptDrag(DnDConstants.ACTION_COPY);
            } else {
                System.out.println("Drag...");
                dtde.rejectDrag();
            }
        }

        public void dragOver(DropTargetDragEvent dtde) {
        }

        public void dropActionChanged(DropTargetDragEvent dtde) {
        }

        public void dragExit(DropTargetEvent dte) {
        }

        public void drop(DropTargetDropEvent dtde) {
            System.out.println("Dropped...");
            if (dtde.getTransferable().isDataFlavorSupported(TransferableSnippet.DATA_FLAVOR)) {
                Transferable t = dtde.getTransferable();
                if (t.isDataFlavorSupported(TransferableSnippet.DATA_FLAVOR)) {
                    try {
                        Object transferData = t.getTransferData(TransferableSnippet.DATA_FLAVOR);
                        if (transferData instanceof String snippet) {
                            dtde.acceptDrop(DnDConstants.ACTION_COPY);
                            panel.add(new JLabel(snippet));
                            panel.revalidate();
                            panel.repaint();
                            code.add(snippet);
                        } else {
                            dtde.rejectDrop();
                        }
                    } catch (UnsupportedFlavorException | IOException ex) {
                        ex.printStackTrace();
                        dtde.rejectDrop();
                    }
                } else {
                    dtde.rejectDrop();
                }
            }
        }

    }
}

