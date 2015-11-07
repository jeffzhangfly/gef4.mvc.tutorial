package gef4.mvc.tutorial.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.gef4.fx.nodes.FXGeometryNode;
import org.eclipse.gef4.geometry.planar.Point;
import org.eclipse.gef4.geometry.planar.Rectangle;
import org.eclipse.gef4.geometry.planar.RoundedRectangle;
import org.eclipse.gef4.mvc.fx.parts.AbstractFXContentPart;
import org.eclipse.gef4.mvc.models.FocusModel;

import gef4.mvc.tutorial.model.TextNode;
import gef4.mvc.tutorial.policies.ChangeTextNodeTextOperation;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class TextNodePart extends AbstractFXContentPart<StackPane> implements PropertyChangeListener {

	private Text text;
	private FXGeometryNode<RoundedRectangle> fxRoundedRectNode;

	private boolean isEditing = false;
	private TextField editText;

	@Override
	protected void doActivate() {
		super.doActivate();
		getContent().addPropertyChangeListener(this);

		getViewer()
			.getAdapter(FocusModel.class)
			.addPropertyChangeListener( this::handleFocusModelUpdate );
	
	}

	@Override
	protected void doDeactivate() {
		getContent().removePropertyChangeListener(this);

		getViewer()
			.getAdapter(FocusModel.class)
			.removePropertyChangeListener( this::handleFocusModelUpdate );
		
		super.doDeactivate();
	}

	
	@Override
	public TextNode getContent() {
		return (TextNode)super.getContent();
	}

	@Override
	protected StackPane createVisual() {
		StackPane group = new StackPane();
		text = new Text();
		fxRoundedRectNode = new FXGeometryNode<>();
		editText = new TextField();
		
		editText.setManaged(false);
		editText.setVisible(false);
		
		group.getChildren().add(fxRoundedRectNode);
		group.getChildren().add(text);
		group.getChildren().add(editText);
		
		
		return group;
	}

	@Override
	protected void doRefreshVisual(StackPane visual) {
		TextNode model = getContent();
		
		Font font = Font.font("Monospace", FontWeight.BOLD, 50 );
		Color textColor = Color.BLACK;
		int textStrokeWidth = 2;
		
		
		text.setText( model.getText() );
		text.setFont( font );
		text.setFill(textColor);
		text.setStrokeWidth(textStrokeWidth);

		// measure size
		Bounds textBounds = msrText(model.getText(), font, textStrokeWidth );

		Rectangle bounds = new Rectangle( 
				model.getPosition().x, model.getPosition().y, 
				textBounds.getWidth() + textBounds.getHeight(), textBounds.getHeight() * 1.5 );

		visual.setTranslateX(model.getPosition().x);
		visual.setTranslateY(model.getPosition().y);
		
		// the rounded rectangle
		RoundedRectangle roundRect = new RoundedRectangle( bounds, 10, 10 );
		fxRoundedRectNode.setGeometry(roundRect);
		fxRoundedRectNode.setFill( model.getColor() );
		fxRoundedRectNode.setStroke( Color.BLACK );
		fxRoundedRectNode.setStrokeWidth(2);
		fxRoundedRectNode.toBack();

		text.toFront();
		
		editText.toFront();
		editText.setPrefWidth(bounds.getWidth());
			

	}
	
	private void handleFocusModelUpdate(PropertyChangeEvent evt) {
		// when focus goes away, cancel editing
		if( evt.getNewValue() != this  ){
			editModeEnd(false);
		}
	}
	

	private Bounds msrText(String string, Font font, int textStrokeWidth) {
		Text msrText = new Text(string);
		msrText.setFont( font );
		msrText.setStrokeWidth(textStrokeWidth);

		new Scene(new Group(msrText));
		Bounds textBounds = msrText.getLayoutBounds();
		return textBounds;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if( evt.getSource() == getContent() ){
			refreshVisual();
		}
	}

	public void translate( double x, double y ){
		Point pos = getContent().getPosition();
		getContent().setPosition( new Point( pos.x + x, pos.y + y ) );
	}
	
	public void editModeStart() {
		if (isEditing) {
			return;
		}
			
		
		isEditing = true;
		setVisualsForEditing();
		
		editText.setText(text.getText());
		editText.requestFocus();
		refreshVisual();
	}
	
	public void editModeEnd( boolean commit ) {
		if (!isEditing) {
			return;
		}
		if( commit ){
			String newText = editText.getText();
			text.setText(newText);
			ChangeTextNodeTextOperation op = new ChangeTextNodeTextOperation(this, getContent().getText(), newText);
			try {
				getViewer().getDomain().getOperationHistory().execute(op, null, null);
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		isEditing = false;
		setVisualsForEditing();
	}

	private void setVisualsForEditing(){
		editText.setManaged(isEditing);
		editText.setVisible(isEditing);
		text.setManaged(!isEditing);
		text.setVisible(!isEditing);
		
	}
	public boolean isEditing() {
		return isEditing;
	}

}
