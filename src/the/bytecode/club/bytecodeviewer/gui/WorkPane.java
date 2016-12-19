package the.bytecode.club.bytecodeviewer.gui;

import org.objectweb.asm.tree.ClassNode;
import the.bytecode.club.bytecodeviewer.BytecodeViewer;
import the.bytecode.club.bytecodeviewer.FileChangeNotifier;
import the.bytecode.club.bytecodeviewer.xposedgenerator.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Comparator;
import eu.bibl.banalysis.asm.desc.OpcodeInfo;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;

/***************************************************************************
 * Bytecode Viewer (BCV) - Java & Android Reverse Engineering Suite        *
 * Copyright (C) 2014 Kalen 'Konloch' Kinloch - http://bytecodeviewer.com  *
 *                                                                         *
 * This program is free software: you can redistribute it and/or modify    *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation, either version 3 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>. *
 ***************************************************************************/

/**
 * The pane that contains all of the classes as tabs.
 * 
 * @author Konloch
 * @author WaterWolf
 * 
 */

public class WorkPane extends VisibleComponent implements ActionListener {

	private static final long serialVersionUID = 6542337997679487946L;

	FileChangeNotifier fcn;
	public JTabbedPane tabs;

	JPanel buttonPanel;
	JButton refreshClass;
	//Securify functionality
	JButton generateXposedClass;

	HashMap<String, Integer> workingOn = new HashMap<String, Integer>();

	public static int SyntaxFontHeight = 12;

	public WorkPane(final FileChangeNotifier fcn) {
		super("WorkPanel");
		setTitle("Work Space");

		this.tabs = new JTabbedPane();
		this.fcn = fcn;

		getContentPane().setLayout(new BorderLayout());

		getContentPane().add(tabs, BorderLayout.CENTER);

		buttonPanel = new JPanel(new FlowLayout());

		refreshClass = new JButton("Refresh");
		refreshClass.addActionListener(this);

		buttonPanel.add(refreshClass);
		
		//Xposed 
		generateXposedClass = new JButton("Generate Xposed Module");
		generateXposedClass.addActionListener(this);
		buttonPanel.add(generateXposedClass);

		buttonPanel.setVisible(false);

		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		tabs.addContainerListener(new ContainerListener() {

			@Override
			public void componentAdded(final ContainerEvent e) {
			}

			@Override
			public void componentRemoved(final ContainerEvent e) {
				final Component c = e.getChild();
				if (c instanceof ClassViewer) {
					ClassViewer cv = (ClassViewer) c;
					workingOn.remove(cv.container + "$" + cv.name);
				}
				if (c instanceof FileViewer) {
					FileViewer fv = (FileViewer) c;
					workingOn.remove(fv.container + "$" + fv.name);
				}
			}

		});
		tabs.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent arg0) {
				buttonPanel.setVisible(tabs.getSelectedIndex() != -1);
			}
		});

		this.setVisible(true);

	}

	int tabCount = 0;

	protected Component f;

	public void addWorkingFile(final String name, String container, final ClassNode cn) {
		String key = container + "$" + name;
		if (!workingOn.containsKey(key)) {
			final JPanel tabComp = new ClassViewer(name, container, cn);
			tabs.add(tabComp);
			final int tabCount = tabs.indexOfComponent(tabComp);
			workingOn.put(key, tabCount);
			tabs.setTabComponentAt(tabCount, new TabbedPane(name, tabs));
			tabs.setSelectedIndex(tabCount);
		} else {
			tabs.setSelectedIndex(workingOn.get(key));
		}
	}
	
	public void addFile(final String name, String container, byte[] contents) {
		if(contents == null) //a directory
			return;

		String key = container + "$" + name;
		if (!workingOn.containsKey(key)) {
			final Component tabComp = new FileViewer(name, container, contents);
			tabs.add(tabComp);
			final int tabCount = tabs.indexOfComponent(tabComp);
			workingOn.put(key, tabCount);
			tabs.setTabComponentAt(tabCount, new TabbedPane(name, tabs));
			tabs.setSelectedIndex(tabCount);
		} else {
			tabs.setSelectedIndex(workingOn.get(key));
		}
	}

	@Override
	public void openClassFile(final String name, String container, final ClassNode cn) {
		addWorkingFile(name, container, cn);
	}

	@Override
	public void openFile(final String name, String container, byte[] content) {
		addFile(name, container, content);
	}

	public Viewer getCurrentViewer() {
		return (Viewer) tabs.getSelectedComponent();
	}

	public java.awt.Component[] getLoadedViewers() {
		return tabs.getComponents();
	}
	
	@Override
	public void actionPerformed(final ActionEvent arg0) {
		Thread t = new Thread() {
			public void run() {
				if(BytecodeViewer.viewer.autoCompileOnRefresh.isSelected())
					try {
						if(!BytecodeViewer.compile(false))
							return;
					} catch(java.lang.NullPointerException e) {
						
					}
				final JButton src = (JButton) arg0.getSource();
				if (src == refreshClass) {
					final Component tabComp = tabs.getSelectedComponent();
					if (tabComp != null) {
						if(tabComp instanceof ClassViewer) {
							src.setEnabled(false);
							BytecodeViewer.viewer.setIcon(true);
							((ClassViewer) tabComp).startPaneUpdater(src);
							BytecodeViewer.viewer.setIcon(false);
						} else if(tabComp instanceof FileViewer) {
							src.setEnabled(false);
							BytecodeViewer.viewer.setIcon(true);
							((FileViewer) tabComp).refresh(src);
							BytecodeViewer.viewer.setIcon(false);
						}
					}
				}
				
				//Xposed Generator
				if (src == generateXposedClass){					
					//Get actual file class content
					final Component tabComp = tabs.getSelectedComponent();
					String className = tabComp.getName();					
					String containerName = BytecodeViewer.files.get(0).name;									
					ClassNode classnode = BytecodeViewer.getCurrentlyOpenedClassNode();
					if (tabComp != null) {			
						//Call XposedGenerator class
						XposedGenerator xposed = new XposedGenerator();
						xposed.ParseChosenFileContent(className,containerName,classnode);						
					}										
				}
			}
		};
		t.start();
	}

	public void resetWorkspace() {
		for (Component component : tabs.getComponents()) {
			((ClassViewer) component).reset();
		}
		tabs.removeAll();
		tabs.updateUI();
	} 

	 
}

