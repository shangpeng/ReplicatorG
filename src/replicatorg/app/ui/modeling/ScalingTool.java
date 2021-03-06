package replicatorg.app.ui.modeling;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import java.lang.Double;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import replicatorg.app.Base;
import replicatorg.app.ui.modeling.PreviewPanel.DragMode;

public class ScalingTool extends Tool {

	public ScalingTool(ToolPanel parent) {
		super(parent);
	}

	@Override
	Icon getButtonIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	String getButtonName() {
		return "Scale";
	}

	// If isAbove is true, scale from the bottom of the object; if false, scale from the rough centroid
	boolean isOnPlatform = false;
//	double previousScale = 1;
	double scaleDragChange = 1;
	
	JFormattedTextField scaleFactor;
	@Override
	JPanel getControls() {
		JPanel p = new JPanel(new MigLayout("fillx,filly,gap 0"));
		JButton b;

		scaleFactor = new JFormattedTextField(Base.getLocalFormat());
		scaleFactor.setValue(1.0);
		
		p.add(scaleFactor,"growx");

		b = new JButton("Scale");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				double scale = ((Number)scaleFactor.getValue()).doubleValue();
				if(scale == 0.0)
				{
					JOptionPane.showConfirmDialog(null, "Cannot Scale by 0.0!!", "Scale", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
				}
				else
				{
				parent.getModel().scale(scale,parent.getModel().isOnPlatform());
				}
			}
		});
		p.add(b,"growx,wrap");
		
		b = createToolButton("inches->mm","images/center-object.png");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				parent.getModel().scale(25.4d,parent.getModel().isOnPlatform());
			}
		});
		p.add(b,"growx,wrap");

		b = createToolButton("mm->inches","images/center-object.png");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				parent.getModel().scale(1d/25.4d,parent.getModel().isOnPlatform());
			}
		});
		p.add(b,"growx,wrap");
		
		final JButton emBiggen = createToolButton("Fill Build Space!","");
		emBiggen.setToolTipText("Keith it! (Make the object as large as possible)");

		emBiggen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Double newScale = parent.getModel().scaleMax();
				if(newScale.isNaN())
				{
					JOptionPane.showConfirmDialog(null, "No Machine is Selected!  Cannot scale to max size!!", "Scale to max", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);

				}
				else
				{
					JOptionPane.showConfirmDialog(null, "Scaled by "+newScale.doubleValue(), "Scale to max", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});	
		
		
		//cute easter egg, but causes gui to resize in an annoying way..
//		emBiggen.addMouseListener(new java.awt.event.MouseAdapter() {
//			public void mouseEntered(java.awt.event.MouseEvent evt) {
//				emBiggen.setText("Keith It! Big!");
//			}
//			public void mouseExited(java.awt.event.MouseEvent evt) {
//				emBiggen.setText("Fill Build Space!");
//			}
//		});
		p.add(emBiggen,"growx,wrap");

		return p;
	}

	@Override
	public String getInstructions() {
		return Base.isMacOS()?
				"<html><body>Drag to scale object<br>Shift-drag to rotate view<br>Mouse wheel to zoom</body></html>":
				"<html><body>Left drag to scale object<br>Right drag to rotate view<br>Mouse wheel to zoom</body></html>";
	}

	@Override
	String getTitle() {
		return "Scale object";
	}
	
	public void mousePressed(MouseEvent e) {
		super.mousePressed(e);
		// Reset scale to current value (in case there have been undos, etc. since
		// last update)
		scaleDragChange = parent.getModel().model.getTransform().getScale();
		isOnPlatform = parent.getModel().isOnPlatform();
	}
	public void mouseReleased(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		if (startPoint == null) return;
		Point p = e.getPoint();
		DragMode mode = DragMode.NONE; 
		if (Base.isMacOS()) {
			if (button == MouseEvent.BUTTON1 && !e.isShiftDown()) { mode = DragMode.SCALE_OBJECT; }
		} else {
			if (button == MouseEvent.BUTTON1) { mode = DragMode.SCALE_OBJECT; }
		}
		double xd = (double)(p.x - startPoint.x);
		double yd = -(double)(p.y - startPoint.y);
		switch (mode) {
		case NONE:
			super.mouseDragged(e);
			break;
		case SCALE_OBJECT:
			scaleDragChange += (0.01*(xd+yd))*scaleDragChange;
			double currentScale = parent.getModel().model.getTransform().getScale();
			double targetScale = scaleDragChange/currentScale;
			parent.getModel().scale(targetScale, isOnPlatform);
			scaleFactor.setValue((double) ((int)(100*scaleDragChange))/100);
//			Base.logger.info("scaleDragChange="+scaleDragChange);
			break;
		}
		startPoint = p;
	}

}
