package org.valkyriercp.form.binding.swing;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import org.valkyriercp.binding.form.FormModel;
import org.valkyriercp.form.binding.support.CustomBinding;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FileChooserBinding extends CustomBinding {
    private JTextField field;
    private JButton openChooserButton;
    private final boolean useFile;

    public FileChooserBinding(FormModel model, String path, Class<?> class1, boolean useFile) {
        super(model, path, class1);
        initTextField();
        this.useFile = useFile;
        initChooserButton();
    }

    private void initTextField() {
        field = new JTextField();
        field.setEditable(false);
    }

    private void initChooserButton() {
        openChooserButton = new JButton("...");
        openChooserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isEnabled()) {
                    String lastDirectory = (String) applicationConfig.applicationSession().getAttribute("filechooser_" + getFormModel().getId() + "_" + getProperty());
                    JFileChooser chooser;
                    if(lastDirectory == null)
                    {
                        chooser = new JFileChooser();
                    }
                    else
                    {
                        chooser = new JFileChooser(lastDirectory);
                    }
                    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    int result = chooser.showOpenDialog(null);
                    if (result == JFileChooser.CANCEL_OPTION)
                        return;
                    applicationConfig.applicationSession().setSessionAttribute("filechooser_" + getFormModel().getId() + "_" + getProperty(), chooser.getSelectedFile().getParent());
                    field.setText(chooser.getSelectedFile().getAbsolutePath());
                    updateValue();
                }
            }
        });
    }

    protected void valueModelChanged(Object newValue) {
        if (!useFile) {
            field.setText((String) newValue);
        } else {
            field.setText(((java.io.File) newValue).getAbsolutePath());
        }
        readOnlyChanged();
    }

    protected JComponent doBindControl() {
        if (!useFile && getValue() != null) {
            field.setText((String) getValue());
        } else if (useFile && getValue() != null) {
            field.setText(((java.io.File) getValue()).getAbsolutePath());
        } else {
            field.setText("");
        }
        FormLayout layout = new FormLayout(new ColumnSpec[] { ColumnSpec.decode("fill:10dlu:grow"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC,ColumnSpec.decode("fill:10dlu:nogrow")},
                new RowSpec[] {FormFactory.DEFAULT_ROWSPEC});
        JPanel panel = new JPanel(layout);
        panel.add(field, new CellConstraints(1, 1));
        panel.add(openChooserButton, new CellConstraints(3, 1));
        return panel;
    }

    private void updateValue() {
        if (useFile) {
            controlValueChanged(new java.io.File(field.getText()));
        } else {
            controlValueChanged(field.getText());
        }
    }

    protected void readOnlyChanged() {
        openChooserButton.setEnabled(isEnabled() && !isReadOnly());
    }

    protected void enabledChanged() {
        openChooserButton.setEnabled(isEnabled());
        readOnlyChanged();
    }
}