package org.valkyriercp.widget;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.valkyriercp.application.config.ApplicationObjectConfigurer;
import org.valkyriercp.component.Focussable;
import org.valkyriercp.util.DialogFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Form implementation for the Focussable interface.
 *
 * @author Jan Hoskens
 *
 */
@Configurable
public abstract class AbstractFocussableWidgetForm extends AbstractWidgetForm implements Focussable//, SecurityControllable
{

    public static final String UNSAVEDCHANGES_WARNING_ID = "unsavedchanges.warning";

    public static final String UNSAVEDCHANGES_HASERRORS_WARNING_ID = "unsavedchanges.haserrors.warning";

    private JComponent focusControl;

    @Autowired
    private ApplicationObjectConfigurer applicationObjectConfigurer;

    @Autowired
    private DialogFactory dialogFactory;

    private final Runnable focusRequestRunnable = new Runnable()
    {

        public void run()
        {
            if (focusControl != null)
                focusControl.requestFocusInWindow();
        }
    };

    /**
     * Override to do nothing. Superclass registers a default command, but we are using a different system to
     * define default commands.
     */
    @Override
    protected void handleEnabledChange(boolean enabled)
    {
    }

    /**
     * Registers the component that receives the focus when the form receives focus.
     *
     * @see #grabFocus
     */
    public void setFocusControl(JComponent field)
    {
        this.focusControl = field;
    }

    public void grabFocus()
    {
        if (this.focusControl != null)
            EventQueue.invokeLater(focusRequestRunnable);
    }

    public boolean canClose()
    {
        boolean userBreak = false;
        int answer = JOptionPane.NO_OPTION; // by default no save is required.

        // unless of course there are unsaved changes and we can commit (isAuthorized)
        if (this.getFormModel().isEnabled() && this.getFormModel().isDirty()
                && this.getCommitCommand().isAuthorized())
        { // then we ask the user to save the mess first: yes/no/cancel
            answer = dialogFactory.showWarningDialog(this.getControl(), UNSAVEDCHANGES_WARNING_ID,
                    JOptionPane.YES_NO_CANCEL_OPTION);

            switch (answer)
            {
                case JOptionPane.CANCEL_OPTION :
                    // backup the selection change so table and detail keep in sync
                    // gives problems (asks unsavedchanges twice)
                    userBreak = true;
                    break;
                case JOptionPane.YES_OPTION :
                    if (this.getFormModel().getHasErrors() == true)
                    {
                        dialogFactory.showWarningDialog(this.getControl(), UNSAVEDCHANGES_HASERRORS_WARNING_ID);
                        userBreak = true;
                        break;
                    }
                    this.getCommitCommand().execute();
                    break;
                case JOptionPane.NO_OPTION :
                {
                    this.revert(); // revert so no strange things happen (hopefully)
                    break;
                }
            }
        }

        return !userBreak;
    }
}


