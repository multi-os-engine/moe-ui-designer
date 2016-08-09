package org.moe.designer.uipreview;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.util.ProgressIndicatorBase;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
* @author Eugene.Kudelevsky
*/
class IOSPreviewProgressIndicator extends ProgressIndicatorBase {
  private final Object myLock = new Object();
  private final int myDelay;
  private @NotNull
  final IOSLayoutPreviewToolWindowForm myForm;

  IOSPreviewProgressIndicator(@NotNull IOSLayoutPreviewToolWindowForm form, int delay) {
    myDelay = delay;
    myForm = form;
  }

  @Override
  public void start() {
    super.start();
    UIUtil.invokeLaterIfNeeded(new Runnable() {
      @Override
      public void run() {
        final Timer timer = UIUtil.createNamedTimer("Android rendering progress timer", myDelay, new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            synchronized (myLock) {
              if (isRunning()) {
                myForm.getPreviewPanel().registerIndicator(IOSPreviewProgressIndicator.this);
              }
            }
          }
        });
        timer.setRepeats(false);
        timer.start();
      }
    });
  }

  @Override
  public void stop() {
    synchronized (myLock) {
      super.stop();
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        @Override
        public void run() {
          myForm.getPreviewPanel().unregisterIndicator(IOSPreviewProgressIndicator.this);
        }
      });
    }
  }
}
