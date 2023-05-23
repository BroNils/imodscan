/*
  This file is a part of Angry IP Scanner source code,
  see http://www.angryip.org/ for more information.
  Licensed under GPLv2.
 */
package net.azib.ipscan.gui;

import net.azib.ipscan.config.Labels;
import net.azib.ipscan.core.values.WebExtensionMatcher;
import net.azib.ipscan.gui.util.LayoutHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.*;

import java.util.HashMap;
import java.util.Map;

public class WebExtensionMatcherDialog extends AbstractModalDialog {
	public enum DialogType {
		NEW, EDIT
	}

	private String title;
	private WebExtensionMatcher tempData;

	private Text nameText;
	private Text matcherText;
	// private Text ifMatch;
	private DialogType dialogType;
	private int currentIndex;

	public WebExtensionMatcherDialog(WebExtensionMatcher temporary) {
		super();
		this.tempData = temporary;
		this.title = Labels.getLabel("dialog.webExtension.title");
		this.dialogType = DialogType.NEW;
		this.currentIndex = -1;
	}

	public WebExtensionMatcherDialog(WebExtensionMatcher temporary, String title) {
		super();
		this.tempData = temporary;
		this.title = title;
		this.dialogType = DialogType.NEW;
		this.currentIndex = -1;
	}

	public void setDialogType(DialogType dialogType) {
		this.dialogType = dialogType;
	}

	public void setCurrentIndex(int index) {
		if (index >= tempData.size()) {
			throw new IllegalArgumentException("Index out of bounds");
		}

		this.currentIndex = index;
	}

	@Override
	protected void populateShell() {
		shell = new Shell(Display.getCurrent().getActiveShell(), SWT.DIALOG_TRIM);
		shell.setText(this.title);
		shell.setLayout(LayoutHelper.formLayout(10, 10, 5));

		populateForm();

		Button okButton = new Button(shell, SWT.NONE);
		okButton.setText(Labels.getLabel("button.OK"));

		Button cancelButton = new Button(shell, SWT.NONE);
		cancelButton.setText(Labels.getLabel("button.cancel"));

		positionButtonsInFormLayout(okButton, cancelButton, matcherText);

		okButton.addListener(SWT.Selection, e -> {
			save();
			close();
		});
		cancelButton.addListener(SWT.Selection, e -> close());

		shell.pack();
	}

	public void populateForm() {
		//Combo predefinedCombo = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
		//predefinedCombo.add(Labels.getLabel("fetcher.portText.custom"));
		//predefinedCombo.add("Web detect");
		//predefinedCombo.add("SMTP detect");
		//predefinedCombo.select(0);
		//predefinedCombo.setLayoutData(LayoutHelper.formData(null, new FormAttachment(100), new FormAttachment(0), null));

		Label nameLabel = new Label(shell, SWT.NONE);
		nameLabel.setText(Labels.getLabel("dialog.webExtension.name"));
		nameText = new Text(shell, SWT.BORDER);
		nameText.setLayoutData(LayoutHelper.formData(
				new FormAttachment(0),
				new FormAttachment(100),
				new FormAttachment(nameLabel),
				null
		));
		if (this.dialogType == DialogType.EDIT) {
			nameText.setText(tempData.get(currentIndex).get("name"));
		}

		Label matcherLabel = new Label(shell, SWT.NONE);
		matcherLabel.setText(Labels.getLabel("dialog.webExtension.matcher"));
		matcherLabel.setLayoutData(LayoutHelper.formData(
				new FormAttachment(0),
				null,
				new FormAttachment(nameText),
				null
		));
		matcherText = new Text(shell, SWT.BORDER);
		matcherText.setLayoutData(LayoutHelper.formData(
				new FormAttachment(0),
				new FormAttachment(nameText, 0, SWT.RIGHT),
				new FormAttachment(matcherLabel),
				null
		));
		if (this.dialogType == DialogType.EDIT) {
			matcherText.setText(tempData.get(currentIndex).get("matcher"));
		}

//		Label ifMatchLabel = new Label(shell, SWT.NONE);
//		ifMatchLabel.setText(Labels.getLabel("text.fetcher.portText.replace"));
//		ifMatchLabel.setLayoutData(LayoutHelper.formData(
//				new FormAttachment(0),
//				null,
//				new FormAttachment(matcherText),
//				null
//		));
//		extractGroup = new Text(shell, SWT.BORDER);
//		extractGroup.setText("$1");
//		extractGroup.setLayoutData(LayoutHelper.formData(new FormAttachment(0), new FormAttachment(textToSend, 0, SWT.RIGHT), new FormAttachment(replaceLabel), null));
	}

	void save() {
		Map<String, String> map = new HashMap<>();
		map.put("name", nameText.getText());
		map.put("matcher", matcherText.getText());

		if (this.dialogType == DialogType.NEW) {
			tempData.add(map);
		}
		if (this.dialogType == DialogType.EDIT) {
			tempData.set(currentIndex, map);
		}

		if (!nameText.getText().isEmpty() && !matcherText.getText().isEmpty()) {
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.CLOSE);
			messageBox.setText(this.title);
			String label = this.dialogType == DialogType.NEW ? "dialog.webExtension.add.success" : "dialog.webExtension.edit.success";
			messageBox.setMessage(nameText.getText() + " " + Labels.getLabel(label));
			messageBox.open();
		}
	}
}
